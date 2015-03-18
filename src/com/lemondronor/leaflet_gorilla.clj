(ns com.lemondronor.leaflet-gorilla
  "A renderer for Gorilla REPL that creates maps with leaflet."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [gorilla-renderable.core :as render]
            [selmer.parser :as selmer]))

(set! *warn-on-reflection* true)


(defn- uuid [] (str (java.util.UUID/randomUUID)))


;; We use [lat lon], but GeoJSON uses [lon lat].
(defn- transpose-coord [[lat lon]]
  [lon lat])


(defn multipoint-feature [coords]
  {"type" "Feature"
   "geometry" {"type" "MultiPoint"
               "coordinates" (map transpose-coord coords)}})


(defn linestring-feature [coords]
  {"type" "Feature"
   "geometry" {"type" "LineString"
               "coordinates" (map transpose-coord coords)}})


(defn polygon-feature [coords-arrays]
  {"type" "Feature"
   "geometry" {"type" "Polygon"
               "coordinates" (map #(map transpose-coord %) coords-arrays)}})


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
    (map geojson-feature geometries)}))


(defrecord LeafletView [points opts])


(defn- parse-args [args]
  (loop [args args
         geometries []
         options {}]
   (if (not (seq args))
     [geometries options]
     (let [arg (first args)
           rstargs (next args)]
       (if (keyword? arg)
         (if (seq rstargs)
           (recur (next rstargs)
                  geometries
                  (assoc options arg (first rstargs)))
           (throw (Exception. (str "No value specified for option " arg))))
         (recur rstargs
                (conj geometries arg)
                options))))))


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


(defonce js-tag-id (uuid))
(defonce css-tag-id (uuid))


(def content-template
  "<div>
<div id='{{map-id}}' style='height: {{height}}px; width: {{width}}px;'></div>
<script type='text/javascript'>
$(function () {
  var createMap = function() {
    var map = L.map('{{map-id}}')
    L.tileLayer('{{tile-layer-url}}')
        .addTo(map);
    var geoJson = L.geoJson(
      {{geojson}},
      {style: {'color': '{{color}}',
               'opacity': {{opacity}}}});
    geoJson.addTo(map);
    map.fitBounds(geoJson.getBounds());
  };
  if (!document.getElementById('{{css-tag-id}}')) {
    $('<link>')
      .attr('rel', 'stylesheet')
      .attr('href', '{{leaflet-css-url}}')
      .attr('id', '{{css-tag-id}}')
      .appendTo('head');
  }
  if (!document.getElementById('{{js-tag-id}}')) {
    var jsTag = $('<script>');
    jsTag.appendTo('head');
    jsTag.attr('onload',
               /* Not sure why we need to use setTimeout :( */
               function() {setTimeout(createMap, 100)});
    jsTag.attr('id', '{{js-tag-id}}');
    jsTag.attr('src', '{{leaflet-js-url}}')
    console.log('woo');
  } else {
    createMap();
  }
});
</script>
</div>")


(extend-type LeafletView
  render/Renderable
  (render [self]
    (let [values (merge default-options
                        (:opts self)
                        {:js-tag-id js-tag-id
                         :css-tag-id css-tag-id
                         :map-id (uuid)
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
