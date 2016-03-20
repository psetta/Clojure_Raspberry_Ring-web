(ns ring_web.core
  (:require	[clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
			[hiccup.core :as hiccup]))
			
(defn sshlog [] 
(str 
	"<head>
		<title>sshlog</title>
		<style>table, th, td {border: 1px solid black;border-collapse: collapse; padding: 0.3em;}</style>
	</head>"
	"<body>"
		"<h2>Attempts ssh connections</h2>"
		"<table cellspacing=40>"
		"<tr><td>Date</td><td>User</td><td>IP</td><td>hostname</td></tr>"
		(apply str
			(map #(str "\n<tr><td>" (str/replace % #"," "<td>") "</tr>")
				(str/split (slurp "static/sshlog.log") #"\n")))
			"\n</table>"
			"</body>"
		)
)

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body 
		(cond
			(= (get request :uri) "/sshlog")
					(sshlog)
			(= (get request :uri) "/request") 
					(hiccup/html 
						[:html {}
							[:head {} [:title {} "request"]]
							[:body {} [:pre {} (with-out-str (pp/pprint request))]]
						]
					)
			:else 
					(slurp "static/index.html")
		)
   })