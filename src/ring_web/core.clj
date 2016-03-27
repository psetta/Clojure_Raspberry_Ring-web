(ns ring_web.core
	(:require	[clojure.string :as str]
				[clojure.data.csv :as csv]
				[clojure.java.io :as io]
				[clojure.pprint :as pp]
				[compojure.route :as route]
				[clj-dns.core :as dns]
				[hiccup.core :as hiccup]
				[clojure.string :as string]
				[ring.middleware.cookies :as cookies]
				[ring.middleware.defaults :refer :all]
	))

(def web_name "http://psetta.no-ip.org")

(defn generar_web [titulo estilo contido] 
	(hiccup/html
		[:html {}
			[:head
				[:title titulo]
				[:meta {:name "UTF-8"}]
				[:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
				[:link {:rel "stylesheet" :type "text/css" :href "estilo.css"}]
				;[:link {:rel "icon" :href "favicon.ico" :type "image/x-icon"}]
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
								[:td [:a {:href "sshlog"} "SSH Log"]]
								[:td [:a {:href "request"} "Request"]]
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
			[:h2 "SSH connection attempts"]
			[:form {:action "." :method "POST"}
				[:input {:type "password" :name "passwd"}]
			]
		]
	))

(def estilo_mostrar_ssh
	"	#ssh {text-align: center;}
		#ssh table {margin: 0 auto; border: 0.2em solid #DDD5FB;}
		#ssh table th {width: 5em; border: 0.1em solid lightgray; background-color: #F0F0F0}
		#ssh table td {border: 0.1em solid #F0F0F0; padding: 0.2em}
	")

(def mostrar_ssh_index2
	(list
		[:h2 "SSH connection attempts"]
	))

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

(defn redirect [url]
	{:status 302
	:headers {"Location" url}
	})

(defn file-response [url]
	{:status 200
	:headers {}
	:body (io/file "url")
	})

(defn handler [request]
	(def uri (get request :uri))
	(def posibles (list "/" "/sshlog" "/estilo.css" "/request"))
	(if (some #{uri} posibles)
			{:status 200
			:headers {"Content-Type" "text/html"}
			:body 
				(cond
					(and 	(= (get (get request :form-params) "passwd") "velolog")
								(= (get request :request-method) :post))
										(generar_web "sshlog" estilo_mostrar_ssh  (mostrar_sshlog))
					(= uri "/")
										(generar_web "index" "" "")
					(= uri "/sshlog")
										(generar_web "sshlog" estilo_mostrar_ssh  mostrar_ssh_login)
					(= uri "/estilo.css")
										(slurp (str "static" uri))
					(= uri "/request")
										(generar_web "request" "#request h2 {text-align: center;}"
																	(mostrar_request request))
				)
			}
			(redirect "http://psetta.no-ip.org")
	))
			
