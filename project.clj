(defproject com.lemondronor.leaflet-gorilla "0.1.0-SNAPSHOT"
  :description "A renderer for Gorilla REPL that generates Leaflet maps."
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
   {:plugins [[lein-gorilla "0.3.4"]]}})
