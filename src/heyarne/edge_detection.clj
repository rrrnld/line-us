(ns heyarne.edge-detection
  (:require [opencv4.core :as cv]
            [opencv4.utils :as u]))

(-> (u/mat-from-url "https://raw.githubusercontent.com/hellonico/origami/master/doc/cat_in_bowl.jpeg")
    (cv/cvt-color! cv/COLOR_RGB2GRAY)
    (cv/canny! 300.0 100.0 3 true)
    (cv/bitwise-not!)
    (u/resize-by 0.5)
    #_(u/show))
