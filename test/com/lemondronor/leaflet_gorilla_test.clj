(ns com.lemondronor.leaflet-gorilla-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.leaflet-gorilla :as lg]
            [gorilla-renderable.core :as render]))


(defmacro testable-privates [namespace & symbols]
  (let [defs (map (fn [s] `(def ~s (ns-resolve '~namespace '~s))) symbols)]
    `(do ~@defs)))


(testable-privates com.lemondronor.leaflet-gorilla parse-args)


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
