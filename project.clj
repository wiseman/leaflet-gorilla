(defproject com.lemondronor.leaflet-gorilla "0.1.3-SNAPSHOT"
  :description "A renderer for Gorilla REPL that renders geo data using Leaflet maps."
  :url "https://github.com/wiseman/leaflet-gorilla"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[gorilla-renderable "1.0.0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [selmer "0.8.2"]]
  :profiles
  {:dev
   {:dependencies [[org.clojure/java.jdbc "0.3.6"]
                   [postgresql "9.3-1102.jdbc41"]]
    :plugins [[lein-cloverage "1.0.2"]
              [lein-gorilla "0.3.4"]]}})
