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
 geojson geojson-feature geojson-features parse-args)


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


(deftest test-leaflet
  (testing "1 geometry, no options"
    (let [v (lg/leaflet [[1 2] [2 3]])]
      (is (= '([[1 2] [2 3]]) (:geometries v)))
      (is (= {} (:opts v)))))
  (testing "2 geometries, no options"
    (let [v (lg/leaflet [[1 2] [2 3]] [[4 5]])]
      (is (= '([[1 2] [2 3]] [[4 5]]) (:geometries v)))
      (is (= {} (:opts v)))))
  (testing "1 geometry, one option"
    (let [v (lg/leaflet '[[1 2] [2 3]] :width 400)]
      (is (= '([[1 2] [2 3]]) (:geometries v)))
      (is (= {:width 400} (:opts v)))))
  (testing "2 geometries, 2 options"
    (let [v (lg/leaflet [[1 2] [2 3]] [[4 5]] :width 400 :height 400)]
      (is (= '([[1 2] [2 3]] [[4 5]]) (:geometries v)))
      (is (= {:width 400 :height 400} (:opts v)))))
  (testing "no geometries, 1 option"
    (let [v (lg/leaflet :width 400 :height 400)]
      (is (= '() (:geometries v)))
      (is (= {:width 400 :height 400} (:opts v)))))
  (testing "2 geometries, broken option"
    (is (thrown? Exception (lg/leaflet [[1 2] [2 3]] [[4 5]] :width)))))


(deftest test-geojson-feature
  (testing "explicit :points, 1 point"
    (is (= {:type :Feature, :geometry {:type :MultiPoint, :coordinates [[2 1]]}}
           (geojson-feature [:points [[1 2]]]))))
  (testing "explicit :points, 2 point"
    (is (= {:type :Feature, :geometry {:type :MultiPoint, :coordinates [[2 1] [4 3]]}}
           (geojson-feature [:points [[1 2] [3 4]]]))))
  (testing "implicit :points"
    (is (= {:type :Feature, :geometry {:type :MultiPoint, :coordinates [[2 1]]}}
           (geojson-feature [[1 2]])))))


(deftest test-geojson-features
  (testing "1 geometry"
    (is (= {:features
            [{:type :Feature, :geometry {:type :MultiPoint, :coordinates [[2 1]]}}]}
           (geojson-features [[[1 2]]]))))
  (testing "2 geometries"
    (is (= {:features
            [{:type :Feature, :geometry {:type :MultiPoint, :coordinates [[2 1]]}}
             {:type :Feature, :geometry {:type :MultiPoint, :coordinates [[4 3]]}}]}
           (geojson-features [[[1 2]] [[3 4]]])))))


(deftest test-geojson
  (testing "1 geometry"
    (is (= (json/read-str
            "{\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[2,1]]}}]}")
           (json/read-str (geojson [[[1 2]]]))))))


(deftest test-render
  (testing "rendering"
    (let [v (render/render (lg/leaflet [[1 2]]))]
      (is (= :html (:type v))))))
