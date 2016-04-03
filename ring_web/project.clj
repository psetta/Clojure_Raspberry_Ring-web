(defproject ring_web "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [com.brweber2/clj-dns "0.0.2"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [sigmund "0.1.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler ring_web.core/app
         :port 8080
         :auto-refresh? true})

