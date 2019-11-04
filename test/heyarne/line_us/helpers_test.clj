(ns heyarne.line-us.helpers-test
  (:require [clojure.test :refer :all]
            [heyarne.line-us.helpers :as helpers]
            [thi.ng.geom.rect :as rect]))

(deftest finding-drawing-brounds
  (testing "Should return the correct bounding box"
    (is (= (rect/rect 10 10 40 30)
           (helpers/g01-bounds [[10 10 200]
                                [10 40 200]
                                [10 40 1000]
                                [50 40 200]
                                [40 10 200]])))))

(deftest  rescaling-g01
  (testing "New bounds should match the bounds passed in"
    (testing "when the shape fills the bounds perfectly"
      (let [bounds (helpers/g01-bounds (helpers/rescale [[0 0]
                                                         [100 100]]
                                                        [0 50 50 0]))]
        (is (= (rect/rect [0 0] [50 50]) bounds))))
    (testing "when the shape is wider than high"
      (let [bounds (helpers/g01-bounds (helpers/rescale [[0 0]
                                                         [1000 75]]
                                                        [0 50 50 0]))
            [width height] (:size bounds)]
        (is (= [0.0 0.0] (:p bounds)))
        (is (= 50.0 width))
        (is (< height 50.0))))
    (testing "when the shape is higher than wide"
      (let [bounds (helpers/g01-bounds (helpers/rescale [[0 0]
                                                         [75 1000]]
                                                        [0 50 50 0]))
            [width height] (:size bounds)]
        (is (= [0.0 0.0] (:p bounds)))
        (is (< width 50.0))
        (is (= 50.0 height))))
    (testing "when the shape needs to be translated and scaled"
      (let [bounds (helpers/g01-bounds (helpers/rescale [[0 0]
                                                         [100 200]]
                                                        [-100 100 100 -100]))
            [width height] (:size bounds)]
        (is (= [-100.0 -100.0] (:p bounds)))
        (is (= 100.0 width))
        (is (= 200.0 height)))))
  (testing "G01 sequence should be properly transformed"
    (let [result (helpers/rescale [[50 0]
                                   [100 0]
                                   [150 0]]
                                  [-100 100 100 -100])]
      (is (= [[-100.0 -100.0]
              [0.0 -100.0]
              [100.0 -100.0]]
             result)))))
