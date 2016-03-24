(ns ring_web.core
	(:require	[clojure.string :as str]
				[clojure.data.csv :as csv]
				[clojure.java.io :as io]
				[clojure.pprint :as pp]
				[compojure.route :as route]
				[clj-dns.core :as dns]
				[hiccup.core :as hiccup]))

(defn mira-ip [ip]
	(let [ret (try
              (dns/reverse-dns-lookup ip)
              (catch java.net.UnknownHostException _ "None"))]
    ret))

(defn mostrar_sshlog []
	(hiccup/html
		[:html {}
			[:head
				[:title "sshlog"]
				[:style "table, th, td {border: 1px solid black; border-collapse: collapse; padding: 0.3em;}"]]
			[:body
				[:h2 "SSH connnection attempts"]
				(into	
				[:table {:cellspacing 40}
					[:tr [:td "Date"] [:td "User"] [:td "IP"] [:td "Hostname"]]]
				(for [line (csv/read-csv (io/reader "static/sshlog.log"))]
					[:tr (for [entry line] [:td entry])]
				))
			]
		]))
			   
(defn mostrar_request [req]
	(hiccup/html 
		[:html {}
			[:head {} [:title {} "request"]]
			[:body {} [:pre {} (with-out-str (pp/pprint req))]]
		]))

(defn handler [request]
	{:status 200
	:headers {"Content-Type" "text/html"}
	:body 
		(cond
			(= (get request :uri) "/sshlog") 	(mostrar_sshlog)
			(= (get request :uri) "/request") 	(mostrar_request request)
			:else 								(slurp "static/index.html")
		)
	})
