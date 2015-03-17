(ns com.lemondronor.leaflet-gorilla-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.leaflet-gorilla :as lg]
            [gorilla-renderable.core :as render]))

(deftest a-test
  (testing "FIXME, I fail."
    (let [v (lg/leaflet-view [[1 2]])]
      (is (= 4
             (render/render v))))))
