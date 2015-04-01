(ns com.lemondronor.leaflet-gorilla-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [com.lemondronor.leaflet-gorilla :as lg]
            [gorilla-renderable.core :as render]))


(defmacro testable-privates [namespace & symbols]
  (let [defs (map (fn [s] `(def ~s (ns-resolve '~namespace '~s))) symbols)]
    `(do ~@defs)))


(testable-privates
 com.lemondronor.leaflet-gorilla
 geojson-for-geometries geojson-feature geojson-features parse-args)


(deftest test-parse-args
  (testing "1 geometry, no options"
    (let [[geometries options] (parse-args '([[1 2] [2 3]]))]
      (is (= '([[1 2] [2 3]]) geometries))
      (is (= {} options))))
  (testing "2 geometries, no options"
    (let [[geometries options] (parse-args '([[1 2] [2 3]] [[4 5]]))]
      (is (= '([[1 2] [2 3]] [[4 5]]) geometries))
      (is (= {} options))))
  (testing "1 geometry, one option"
    (let [[geometries options] (parse-args '([[1 2] [2 3]] :width 400))]
      (is (= '([[1 2] [2 3]]) geometries))
      (is (= {:width 400} options))))
  (testing "2 geometries, 2 options"
    (let [[geometries options] (parse-args '([[1 2] [2 3]] [[4 5]] :width 400 :height 400))]
      (is (= '([[1 2] [2 3]] [[4 5]]) geometries))
      (is (= {:width 400 :height 400} options))))
  (testing "no geometries, 1 option"
    (let [[geometries options] (parse-args '(:width 400 :height 400))]
      (is (= '() geometries))
      (is (= {:width 400 :height 400} options))))
  (testing "2 geometries, broken option"
    (is (thrown? Exception (parse-args '([[1 2] [2 3]] [[4 5]] :width))))))


(deftest test-geometry
  (testing "1 implicit point geometry, no options"
    (let [v (lg/geo [[1 2] [2 3]])]
      (is (= '({:type :points :desc [[1 2] [2 3]]}) (:geodescs v)))
      (is (= {} (:opts v)))))
  (testing "2 implicit point geometries, no options"
    (let [v (lg/geo [[1 2] [2 3]] [[4 5]])]
      (is (= '({:type :points :desc [[1 2] [2 3]]}
               {:type :points :desc [[4 5]]})
             (:geodescs v)))
      (is (= {} (:opts v)))))
  (testing "1 implicit geometry, one option"
    (let [v (lg/geo '[[1 2] [2 3]] :width 400)]
      (is (= '({:type :points :desc [[1 2] [2 3]]}) (:geodescs v)))
      (is (= {:width 400} (:opts v)))))
  (testing "2 implicit point geometries, 2 options"
    (let [v (lg/geo [[1 2] [2 3]] [[4 5]] :width 400 :height 400)]
      (is (= '({:type :points :desc [[1 2] [2 3]]}
               {:type :points :desc [[4 5]]})
             (:geodescs v)))
      (is (= {:width 400 :height 400} (:opts v)))))
  (testing "explicit point geometry"
    (let [v (lg/geo [:points [[1 2]]])]
      (is (= '({:type :points :desc [[1 2]]})
             (:geodescs v)))))
  (testing "no geometries, 1 option"
    (let [v (lg/geo :width 400 :height 400)]
      (is (= '() (:geodescs v)))
      (is (= {:width 400 :height 400} (:opts v)))))
  (testing "2 geometries, broken option"
    (is (thrown? Exception (lg/geo [[1 2] [2 3]] [[4 5]] :width)))))


(deftest test-geojson-for-geodesc
  (testing "points"
    (is (= {"type" "Feature",
            "geometry" {"type" "MultiPoint", "coordinates" [[2 1] [4 3]]}}
           (json/read-str
            (lg/geojson-for-geodesc {:type :points :desc [[1 2] [3 4]]}))))))


(deftest test-render
  ;; Just testing that we don't crash for now.
  (testing "rendering implicit points"
    (let [v (render/render (lg/geo [[1 2]]))]
      (is (= :html (:type v)))))
  (testing "rendering explicit points"
    (let [v (render/render (lg/geo [:points [[1 2]]]))]
      (is (= :html (:type v)))))
  (testing "rendering line"
    (let [v (render/render (lg/geo [:line [[1 2] [3 4]]]))]
      (is (= :html (:type v)))))
  (testing "rendering polygon with no holes"
    (let [v (render/render
             (lg/geo [:polygon [[[1 2] [3 4] [4 5]]]]))]
      (is (= :html (:type v)))))
  (testing "rendering polygon with 1 hole"
    (let [v (render/render
             (lg/geo
              [:polygon [[[1 2] [3 4] [4 5]] [[7 8] [9 10] [11 12]]]]))]
      (is (= :html (:type v))))))
