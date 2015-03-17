(ns com.lemondronor.leaflet-gorilla
  ""
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [gorilla-renderable.core :as render]
            [selmer.parser :as selmer]))

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
  (LeafletView. points (apply hash-map opts)))


(def default-options
  {:width 400
   :height 400
   :leaflet-js-url "http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"
   :leaflet-css-url "http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css"
   :tile-layer-url "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
   :color "steelblue"
   :opacity 1.0})


(def content-template
  "<div>
<div id='{{map-id}}' style='height: {{height}}px; width: {{width}}px;'></div>
<script type='text/javascript'>
$(function () {
  $('<link>')
    .attr('rel', 'stylesheet')
    .attr('href', '{{leaflet-css-url}}')
    .appendTo('head');
  $('<script>')
    .attr('src', '{{leaflet-js-url}}')
    .appendTo('head');
  setTimeout(function() {
    var map = L.map('{{map-id}}')
    L.tileLayer('{{tile-layer-url}}')
        .addTo(map);
    var geoJson = L.geoJson(
      {{geojson}},
      {style: {'color': '{{color}}',
               'opacity': {{opacity}}}});
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
    (let [values (merge default-options
                        (:opts self)
                        {:map-id (uuid)
                         :geojson [:safe (geojson (:points self))]})
          html (selmer/render content-template values)]
      {:type :html
       :content html
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
  (->> "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.csv"
       fetch-url-lines
       rest
       (map #(string/split % #","))))


(map/leaflet-view (map (fn [e] [(e 1) (e 2)]) earthquakes))

)
