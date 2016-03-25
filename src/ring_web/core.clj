(ns ring_web.core
	(:require	[clojure.string :as str]
				[clojure.data.csv :as csv]
				[clojure.java.io :as io]
				[clojure.pprint :as pp]
				[compojure.route :as route]
				[clj-dns.core :as dns]
				[hiccup.core :as hiccup]
				[clojure.string :as string]
				))

(def web_name "http://psetta.no-ip.org")

;(def index (string/replace (slurp "static/index.html") #"\n|\t" ""))
;(def end_index "</div></body></html>")

(defn generar_web [titulo estilo contido] 
	(hiccup/html
		[:html {}
			[:head
				[:title titulo]
				[:meta {:name "UTF-8"}]
				[:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
				[:link {:rel "stylesheet" :type "text/css" :href "estilo.css"}]
				[:style estilo]
			]
			[:body
				[:div {:id "frame"}
					[:div {:id "titulo"} "Psetta Maxima"]
					[:hr][:br]
					[:div {:id "indice"}
						[:table
							[:tr
								[:td [:a {:href "http://pseta.no-ip.org"} [:p "Inicio"]]]
								[:td [:a {:href "sshlog"} [:p "SSH Log"]]]
								[:td [:a {:href "https://github.com/psetta"} [:p "Github"]]]]]
					]
					[:br][:hr][:br]
					contido
					]]]))

;(defn mira-ip [ip]
;	(let [ret (try
;              (dns/reverse-dns-lookup ip)
;              (catch java.net.UnknownHostException _ "none"))]
;    ret))

(def mostrar_sshlog_index
	"	<div id=\"ssh\">
			<h2>SSH connection attempts</h2>
			<form action=\".\" method=\"POST\">
				<input type=\"password\" name=\"passwd\"
		</div>
	")

(def mostrar_ssh_index2
		[:h2 "SSH connection attempts"]
)

;(defn mostrar_sshlog []
;	(hiccup/html
;		[:html {}
;			[:head
;				[:title "sshlog"]
;				[:style "table, th, td {border: 1px solid black; border-collapse: collapse; padding: 0.3em;}"]]
;			[:body
;				[:h2 "SSH connnection attempts"]
;				(into	
;				[:table {:cellspacing 40}
;					[:tr {:bgcolor "#E0E0E0"} [:th "Date"] [:th "User"] [:th "IP"] [:th "Hostname"]]]
;				(for [line (csv/read-csv (io/reader "static/sshlog.log"))]
;					[:tr (for [entry line] [:td entry])]
;				))
;			]
;		]))
			   
;(defn mostrar_request [req]
;	(str 
;		(hiccup/html 
;			[:body {} [:pre {} (with-out-str (pp/pprint req))]]
;	)))

(defn handler [request]
	(def uri (get request :uri))
	(def posibles (list "/" "/sshlog" "/estilo.css" "/request"))
	(if (some #{uri} posibles)
			{:status 200
			:headers {"Content-Type" "text/html"}
			:body 
				(cond
					;(and 	(= (get (get request :form-params) "passwd") "velolog")
					;			(= (get request :request-method) :post))
					;					(str index (mostrar_sshlog) end_index)
					(= uri "/")
										(generar_web "index" "" "")
					(= uri "/sshlog") 		
										(generar_web "sshlog" "#ssh {text-align: center;}"  mostrar_ssh_index2)
					(= uri "/estilo.css")
										(slurp (str "static" uri))
					;(= uri "/request")
					;					(generar_web "request" "estilo" mostrar_request)
				)
			}
			{:status 302
			:headers {"Location" "http://psetta.no-ip.org"}
			:text "Go back to index"
			}
			))
			
