(ns com.lemondronor.leaflet-gorilla
  "A renderer for Gorilla REPL that creates maps with leaflet."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [gorilla-renderable.core :as render]
            [selmer.parser :as selmer]))


(defn- uuid [] (str (java.util.UUID/randomUUID)))


;; We use [lat lon], but GeoJSON uses [lon lat].
(defn- transpose-coord [[lat lon]]
  [lon lat])


(defmulti geojson-for-geodesc :type)


(defmethod geojson-for-geodesc :geojson [geodesc]
  (json/read-str (:desc geodesc)))


(defn- multipoint-feature [coords]
  {:type :Feature
   :geometry {:type :MultiPoint
               :coordinates (map transpose-coord coords)}})


(defmethod geojson-for-geodesc :points [geodesc]
  (multipoint-feature (:desc geodesc)))


(defn- linestring-feature [coords]
  {:type :Feature
   :geometry {:type :LineString
               :coordinates (map transpose-coord coords)}})


(defmethod geojson-for-geodesc :line [geodesc]
  (linestring-feature (:desc geodesc)))


(defn- polygon-feature [coords-arrays]
  {:type :Feature
   :geometry {:type :Polygon
               :coordinates (map #(map transpose-coord %) coords-arrays)}})


(defmethod geojson-for-geodesc :polygon [geodesc]
  (polygon-feature (:desc geodesc)))


(defrecord LeafletView [geodescs opts])


(defn- parse-args [args]
  (loop [args args
         geodescs []
         options {}]
   (if (not (seq args))
     [geodescs options]
     (let [arg (first args)
           rstargs (next args)]
       (if (keyword? arg)
         (if (seq rstargs)
           (recur (next rstargs)
                  geodescs
                  (assoc options arg (first rstargs)))
           (throw (Exception. (str "No value specified for option " arg))))
         (recur rstargs
                (conj geodescs arg)
                options))))))


(defn canonicalize-geodesc [default-type g]
  (let [type-desig (first g)
        desc (second g)
        canon (if (keyword? type-desig)
                {:type type-desig :desc desc}
                {:type default-type :desc g})]
    canon))


(defn geo
  "Plots geometries on a map."
  [& args]
  (let [[geometries opts] (parse-args args)]
    (LeafletView. (map #(canonicalize-geodesc :points %) geometries) opts)))


;; For backwards compatibility.
(defn leaflet
  "Plots geometries on a map."
  [& args]
  (apply geo args))


(defn geojson
  "Plots geometries on a map."
  [& args]
  (let [[geodescs opts] (parse-args args)]
    (LeafletView. (map #(canonicalize-geodesc :geojson %) geodescs) opts)))


(def default-options
  {:width 400
   :height 400
   :leaflet-js-url "http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"
   :leaflet-css-url "http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css"
   :tile-layer-url "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
   :color "steelblue"
   :opacity 1.0})


;; Might as well use an ID for the CSS that theoretically some other
;; renderer could reference.
(def leaflet-css-tag-id "leaflet-css")


;; This, unfortunately, is a lot of javascript.
;;
;; First it loads the Leaflet CSS if it hasn't already been loaded.
;;
;; The complicated part is properly loading the Leaflet javascript. If
;; it hasn't already been loaded, we check to see if it is currently
;; being loaded. If it is being loaded, we add our map creation
;; function to a queue of callbacks. If it isn't being loaded, we
;; start loading it. When it's finally loaded we call all the
;; functions in the callback queue.
;;
;; If the Leaflet javascript has already been fully loaded, we just
;; create our map.

(def content-template
  "<div>
<div id='{{map-id}}' style='height: {{height}}px; width: {{width}}px;'></div>
<script type='text/javascript'>
$(function () {
  var cachedScript = function(url, options) {
    // Allow user to set any option except for dataType, cache, and url
    options = $.extend( options || {}, {
      dataType: 'script',
      cache: true,
      url: url
    });

    // Use $.ajax() since it is more flexible than $.getScript
    // Return the jqXHR object so we can chain callbacks
    return jQuery.ajax(options);
  };
  var createMap = function() {
    var map = L.map('{{map-id}}')
    L.tileLayer('{{tile-layer-url}}')
        .addTo(map);
    var geoJsons = {{geojsons}};
    var bounds = null;
    for (var i = 0; i < geoJsons.length; i++) {
      var geoJson = L.geoJson(
      geoJsons[i],
      {style: {'color': '{{color}}',
               'opacity': {{opacity}}}});
      geoJson.addTo(map);
      console.log('Added GeoJSON:');
      console.log(geoJsons[i]);
      if (!bounds) {
        bounds = geoJson.getBounds();
      } else {
        bounds.extend(geoJson.getBounds());
      }
    }
    if ({{view}}) {
      map.setView.apply(map, {{view}});
    } else {
      console.log('setting bounds to ' + bounds);
      console.log(bounds);
      map.fitBounds(bounds);
    }
  };
  if (!document.getElementById('{{css-tag-id}}')) {
    $('<link>')
      .attr('rel', 'stylesheet')
      .attr('href', '{{leaflet-css-url}}')
      .attr('id', '{{css-tag-id}}')
      .appendTo('head');
  }
  if (!window.leafletJsLoaded) {
    if (!window.leafletJsIsLoading) {
      window.leafletJsLoadedCallbacks = [createMap];
      window.leafletJsIsLoading = true;
      cachedScript('{{leaflet-js-url}}')
        .done(function() {
          window.leafletJsIsLoading = false;
          window.leafletJsLoaded = true;
          _.each(window.leafletJsLoadedCallbacks, function(cb) { cb(); });
          window.leafletJsLoadedCallbacks = [];
        })
        .fail(function() { console.log('failed'); });
    } else {
      window.leafletJsLoadedCallbacks.push(createMap);
    }
  } else {
    createMap();
  }
});
</script>
</div>")


;; Implement the Gorilla renderable protocol.
(extend-type LeafletView
  render/Renderable
  (render [self]
    (let [geodescs (:geodescs self)
          opts (:opts self)
          values (merge default-options
                        opts
                        {:css-tag-id leaflet-css-tag-id
                         :map-id (uuid)
                         :view (json/write-str (:view opts))
                         :geojsons [:safe (json/write-str (map geojson-for-geodesc geodescs))]})
          html (selmer/render content-template values)]
      {:type :html
       :content html
       :value (pr-str self)})))
