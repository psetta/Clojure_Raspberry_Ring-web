(ns ring_web.core
	(:require	[clojure.string :as str]
				[clojure.data.csv :as csv]
				[clojure.java.io :as io]
				[clojure.pprint :as pp]
				[compojure.route :as route]
				[clj-dns.core :as dns]
				[hiccup.core :as hiccup]
				[clojure.string :as string]
				[ring.middleware.session :as session]
				[oren.core :as oren]
	))

(def web_name "http://psetta.no-ip.org")

(defn generar_web [priv refresh titulo estilo contido] 
	(hiccup/html
		[:html {}
			[:head
				[:title titulo]
				[:meta {:name "UTF-8"}]
				[:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
				(when refresh
					[:meta {:http-equiv "refresh" :content "1;URL='http://psetta-clojure.ga'"}])
				[:link {:rel "stylesheet" :type "text/css" :href "estilo.css"}]
				[:link {:rel "icon" :href "favicon.ico" :type "image/x-icon"}]
				[:style estilo]
			]
			[:body
				[:div {:id "frame"}
					[:a {:href "http://psetta-clojure.ga"} [:div {:id "titulo"} "Psetta Maxima"]]
					[:div {:id "indice"}
						[:table
							[:tr
								(when priv
									(into
										[:div {:id "priv_links"}]
										(for [x [
												[:td {:class "normal"} [:a {:href "."} "OS Info"]]
												[:td {:class "normal"} [:a {:href "sshlog"} "SSH Log"]]
												[:td {:class "normal"} [:a {:href "request"} "Request"]]
												]
											] x)
									))
								[:td {:class "normal"} [:a {:href "https://github.com/psetta" :target "_blank"} "Github"]]
							]
						]
					]
					[:br][:hr][:br]
					(into
						[:div {:id "contido"}]
						(for [x contido] x)
					)
				]
			]
		]
	))

(defn mira-ip [ip]
	(let [ret (try
   	          (dns/reverse-dns-lookup ip)
              (catch java.net.UnknownHostException _ "none"))]
    ret))

(def mostrar_login
	(list
		[:div {:id "login"}
			[:h2 "Login"]
			[:form {:action "." :method "POST"}
				[:input {:type "password" :name "passwd"}]
			]
		]
	))

(def estilo_mostrar_ssh
	"	#ssh {text-align: center;}
		#ssh table {margin: 0 auto; border: 0.2em solid #9CE5FF; padding: 0.1em;}
		#ssh table th {border: 0.1em solid lightgray; background-color: #F0F0F0; margin: 1em; padding: 0.3em;}
		#ssh table td {border: 0.1em solid #F0F0F0; padding: 0.3em; margin: 1em;  max-width: 15em;
						min-width: 3em; overflow: hidden; text-overflow: ellipsis;}
		#ssh table tr:hover {background-color: #F0F0F0;}
		#ssh table td:hover {background-color: #BAE6F7; overflow: visible;}
	")

(defn mostrar_sshlog []
	(list
		[:div {:id "ssh"}
			[:h2 "SSH connection attempts"]
			(into	
				[:table {}
					[:tr {} [:th "Date"] [:th "User"] [:th "IP"] [:th "City"] [:th "Country"] [:th "Hostname"]]]
				(if (.exists (clojure.java.io/as-file "static/sshlog.log"))
					(for [line (csv/read-csv (io/reader "static/sshlog.log"))]
						[:tr (for [entry line] [:td entry])]
					)
					[]
				)
			)
		]
	))
	
;; Colhe un diccionario e transformao en táboa
(defn fai-taboa [todo]
  (cond
    (map? todo) [:table {:border 1}
                 (for [[k v] (reverse todo)]
                   [:tr
                    [:td k]
                    [:td
                     (if (= k :edid) (str v)
                         (fai-taboa v))]])]
    (or (vector? todo)
        (seq? todo)) [:table {:border 1}
                      (for [x todo]
                        [:tr [:td (fai-taboa x)]])]
    :else (str todo)))
			   
(defn mostrar_request [req]
	(list
		[:div {:id "request"}
			[:h2 "Request"]
			(fai-taboa req)
		]))

(defn mostrar_server_info []
	(def server_info (oren/all))
	(list 
		[:div {:id "server_info"}
			(fai-taboa server_info)
		]))

(defn redirect [url]
	{:status 302
	:headers {"Location" url}
	})

(defn file-response [tipo url]
	{:status 200
	:headers {"Content-Type" tipo}
	:body (io/file url)
	})

(defn web-page [cont]
	{:status 200
	:headers {"Content-Type" "text/html"}
	:body cont
	})

(defn engadir-sesion [response id]
	(assoc response :session id))

(def contrasinal "abrete")

(defn cargar_pagina_indicada [uri request]
	(def si
		(if (float? (request :session)) true false))
	(cond
		(= uri "/") 
			(cond
				si
					(web-page (generar_web
							si
							false
							"psetta"
							 "#server_info {width:45em; margin: 0 auto;}
							  #server_info table {border: 0.1em solid #9CE5FF; padding: 0.1em;}"
							(mostrar_server_info)))
					(= ((request :form-params) "passwd") contrasinal)
							(let 	[web (web-page (generar_web 
										true
										true
										"psetta" 
										"#contido {text-align: center;}" 
										"Sesión Iniciada"))]
										(engadir-sesion web (rand)))
					:else
						(web-page (generar_web 
							si
							false
							"login"
							"#login {text-align: center;}" 
							mostrar_login))
				)
			(= uri "/sshlog")
					(web-page (generar_web 
						si
						false
						"psetta" 
						estilo_mostrar_ssh  
						(mostrar_sshlog)))
			(= uri "/request")
					(web-page (generar_web
						si
						false
						"psetta"
						"#request h2 {text-align: center;}
						 #request {width:50em; margin: 0 auto;}
						 #request table {border: 0.1em solid #9CE5FF; padding: 0.1em;}"
						(mostrar_request request)))
			(= uri "/estilo.css")
					(file-response "text/css" (str "static" uri))
			(= uri "/favicon.ico")
					(file-response "image/x-icon" (str "static" uri))
		))

(defn handler [request]
	(def uri (request :uri))
	(if (float? (request :session))
		
			(def posibles ["/" "/sshlog" "/request" "/estilo.css" "/favicon.ico"])
			(def posibles ["/" "/estilo.css" "/favicon.ico"])
	)
	(if (some #{uri} posibles)
		(cargar_pagina_indicada uri request)
		(redirect "http://psetta-clojure.ga")
	))

(def app
	(-> #'handler
			(session/wrap-session)
	))
			
