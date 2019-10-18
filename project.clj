(defproject pinkgorilla.ui.leaflet "0.2.4-SNAPSHOT"
  :description "A renderer for PinkGorilla REPL that renders geo data using Leaflet maps."
  :url "https://github.com/pink-gorilla/pinkgorilla-leaflet"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  
  ;:deploy-repositories [["releases" :clojars]]
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :username "pinkgorillawb"
                             :sign-releases false}]]
    
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [selmer "0.8.9"]
                 ;[org.clojars.deas/gorilla-renderable "2.1.0"] ;PinkGorilla Renderable
                 [pinkgorilla.ui.gorilla-renderable "1.0.1"] ;PinkGorilla Renderable
                 ]
  :profiles
  {:dev
   {:dependencies [[org.clojure/java.jdbc "0.4.1"]
                   [postgresql "9.3-1102.jdbc41"]]
    :plugins [
              [lein-cloverage "1.0.2"]
              ;[lein-gorilla "0.3.4"
              ]
    }})
