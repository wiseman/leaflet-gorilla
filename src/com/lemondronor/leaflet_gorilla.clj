(ns com.lemondronor.leaflet-gorilla
  ""
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [gorilla-renderable.core :as render]))

(set! *warn-on-reflection* true)


(defn- uuid [] (str (java.util.UUID/randomUUID)))


(defn point-geometry [lat lon]
  {
   "geometry" {
               "coordinates" [lon lat]
               "type" "Point"
               },
   "type" "Feature"
   })


(defrecord LeafletView [points opts])

(defn geojson [points]
  (json/write-str
   {"features"
    (map
     #(let [[lat lon] %]
        (point-geometry lat lon))
     points)
    }))


(defn leaflet-view [points & opts]
  (LeafletView. points opts))


;; <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
;;  <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>

(def content-template
  "<div>
<div id='map' style='height: %spx; width: %spx;'></div>
<script type='text/javascript'>
$(function () {
  $('<link>')
    .attr('rel', 'stylesheet')
    .attr('href', 'http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css')
    .appendTo('head');
  $('<script>')
    .attr('src', 'http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js')
    .appendTo('head');
  setTimeout(function() {
    var map = L.map('map')
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png')
        .addTo(map);
    var geoJson = L.geoJson(
      %s,
      {style: function() { return '%s'; }});
    geoJson.addTo(map);
    map.fitBounds(geoJson.getBounds());
  },
  3000);
});
</script>
</div>")


(extend-type LeafletView
  render/Renderable
  (render [self]
    (let [opts (:opts self)]
      {:type :html
       :content (format
                 content-template
                 (get opts :height 480)
                 (get opts :width 640)
                 (geojson (:points self))
                 (or (:color self) "steelblue"))
       :value (pr-str self)})))


(comment
(ns gentle-shelter
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [com.lemondronor.leaflet-gorilla :as map]
   [gorilla-plot.core :as plot]))

(defn fetch-url-lines[address]
  (with-open [stream (.openStream (java.net.URL. address))]
    (let  [buf (java.io.BufferedReader.
                (java.io.InputStreamReader. stream))]
      (line-seq buf))))

(def earthquakes
  (->> "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/1.0_day.csv"
       fetch-url-lines
       rest
       (map #(string/split % #","))))


(map/leaflet-view (map (fn [e] [(e 1) (e 2)]) earthquakes))
)
