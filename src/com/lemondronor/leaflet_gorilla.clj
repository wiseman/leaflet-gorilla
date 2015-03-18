(ns com.lemondronor.leaflet-gorilla
  ""
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [gorilla-renderable.core :as render]
            [selmer.parser :as selmer]))

(set! *warn-on-reflection* true)


(defn- uuid [] (str (java.util.UUID/randomUUID)))


(defn transpose-coord [[lat lon]]
  [lon lat])


(defn multipoint-feature [coords]
  {
   "type" "Feature"
   "geometry" {
               "type" "MultiPoint"
               "coordinates" (map transpose-coord coords)
               }
   })


(defn linestring-feature [coords]
  {
   "type" "Feature"
   "geometry" {
               "type" "LineString"
               "coordinates" (map transpose-coord coords)
               }
   })


(defn polygon-feature [coords-arrays]
  {
   "type" "Feature"
   "geometry" {
               "type" "Polygon"
               "coordinates" (map #(map transpose-coord %) coords-arrays)
               }
   })


(defn geojson-feature [geodesc]
  (let [type-desig (first geodesc)
        coords (second geodesc)]
    (case type-desig
      :points (multipoint-feature coords)
      :line (linestring-feature coords)
      :polygon (polygon-feature coords))))


(defn geojson [& geometries]
  (json/write-str
   {"features"
    (map geojson-feature geometries)
    }))


(defrecord LeafletView [points opts])

(defn leaflet [points & opts]
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


(lg/leaflet (map (fn [e] [(e 1) (e 2)]) earthquakes))

(def oakland-alpr
  (->> "https://www.eff.org/files/2015/01/20/oakland_pd_alpr.csv"
       fetch-url-lines
       rest
       (map #(string/split % #","))))

(lg/leaflet (map (fn [r] [(r 2) (r 3)]) oakland-alpr))

)
