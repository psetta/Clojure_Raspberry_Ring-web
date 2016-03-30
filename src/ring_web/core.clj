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
				[sigmund.core :as sig]
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
					[:meta {:http-equiv "refresh" :content "1;URL='http://psetta.no-ip.org'"}])
				[:link {:rel "stylesheet" :type "text/css" :href "estilo.css"}]
				[:link {:rel "icon" :href "favicon.ico" :type "image/x-icon"}]
				[:style estilo]
			]
			[:body
				[:div {:id "frame"}
					[:div {:id "titulo"} "Psetta Maxima"]
					[:hr][:br]
					[:div {:id "indice"}
						[:table
							[:tr
								[:td [:a {:href "http://psetta.no-ip.org"} "Inicio"]]
								(when priv
									(into
										[:div {:id "priv_links"}]
										(for [x [
														[:td [:a {:href "sshlog"} "SSH Log"]]
														[:td [:a {:href "request"} "Request"]]
														]] x)
									))
								[:td [:a {:href "https://github.com/psetta"} "Github"]]
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

(def mostrar_ssh_login
	(list
		[:div {:id "ssh"}
			[:h2 "Login"]
			[:form {:action "." :method "POST"}
				[:input {:type "password" :name "passwd"}]
			]
		]
	))

(def estilo_mostrar_ssh
	"	#ssh {text-align: center;}
		#ssh table {margin: 0 auto; border: 0.2em solid #DDD5FB;}
		#ssh table th {width: 5.5em; border: 0.1em solid lightgray; background-color: #F0F0F0}
		#ssh table td {border: 0.1em solid #F0F0F0; padding: 0.1em}
	")

(defn mostrar_sshlog []
	(list
		[:div {:id "ssh"}
			[:h2 "SSH connection attempts"]
			(into	
				[:table {}
					[:tr {} [:th "Date"] [:th "User"] [:th "IP"] [:th "Hostname"]]]
				(for [line (csv/read-csv (io/reader "static/sshlog.log"))]
					[:tr (for [entry line] [:td entry])]
				)
			)
		]
	))
			   
(defn mostrar_request [req]
	(list
		[:div {:id "request"}
			[:h2 "Request"]
			[:pre {} (with-out-str (pp/pprint req))]
		]
	))

(defn mostrar_server_info []
	(list
		[:div {:id "server_info"}
			[:h2 "Server Info"]
			[:h3 "Sistema Operativo"]
			[:pre {} (with-out-str (pp/pprint (sig/os)))]
			[:h3 "Memoria"]
			[:pre {} (with-out-str (pp/pprint (sig/os-memory)))]
			[:h3 "CPU"]
			[:pre {} (with-out-str (pp/pprint (sig/cpu-usage)))]
			[:h3 "Disco"]
			[:pre {} (with-out-str (pp/pprint (sig/fs-usage "/")))]
		]
	))

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

(def sesion_iniciadas [])

(defn generar_sesion []
	(def id (rand))
	(if (not (some #{id} sesion_iniciadas))
			id
			(recur)))

(defn engadir-sesion [response id]
	(def sesion_iniciadas (cons id sesion_iniciadas))
	(assoc response :session id))

(def contrasinal "queroentrar")

(defn cargar_pagina_indicada [uri request]
	(def si
		(if (some #{(request :session)} sesion_iniciadas)
				true
				false))
	(cond
			(= uri "/") 
				(cond
					si
							(web-page (generar_web
												si
												false
												"psetta"
												"#contido h2 {text-align: center;}"
												(mostrar_server_info)))
					(= ((request :form-params) "passwd") contrasinal)
							(let 	[web (web-page (generar_web 
												true
												true
												"psetta" 
												"#contido {text-align: center;}" 
												"Sesión Iniciada"))]
										(engadir-sesion web (generar_sesion)))
					:else
							(web-page (generar_web 
												si
												false
												"login"
												estilo_mostrar_ssh 
												mostrar_ssh_login))
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
										"#request h2 {text-align: center;}"
										(mostrar_request request)))
			(= uri "/estilo.css")
					(file-response "text/css" (str "static" uri))
			(= uri "/favicon.ico")
					(file-response "image/x-icon" (str "static" uri))
		))

(defn handler [request]
	(def uri (request :uri))
	(if (some #{(request :session)} sesion_iniciadas)
			(def posibles ["/" "/sshlog" "/request" "/estilo.css" "/favicon.ico"])
			(def posibles ["/" "/estilo.css" "/favicon.ico"])
	)
	(if (some #{uri} posibles)
		(cargar_pagina_indicada uri request)
		(redirect "http://psetta.no-ip.org")
	))

(def app
	(-> #'handler
			(session/wrap-session)
	))
			
