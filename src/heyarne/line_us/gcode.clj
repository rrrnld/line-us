(ns heyarne.line-us.gcode
  "Provides functions to move from geometry types provided by thi.ng to gcode
  so you can plot them."
  (:require [thi.ng.geom.core :as g])
  (:import [thi.ng.geom.types Line2 LineStrip2 Circle2]))

(defprotocol GCode
  "Convert thi.ng.geom types into sequences of GCode to plot them. The GCode
  instructions are given as a sequence of vectors, where each vector is given
  as [x y z]."
  (->gcode [_] [_ r]
    "Returns G01 movements on the x, y, and z axis. Z is constantly 1000 in the
     current implementation, but this might change in the future. The optional
     parameter `r` can be used to adjust the resolution."))

(defn- point-seq->gcode [pts]
  (let [vertices pts
        [l-x l-y] (last vertices)]
    (conj (mapv (fn [[x y]]
                  [x y 0]) vertices)
          [l-x l-y 1000])))

(defn gcode-seq->str [gcode-seq]
  (map (fn [[x y z]]
         (str "G01 X" x " Y" y " Z" z)) gcode-seq))

(extend-protocol GCode
  Line2
  (->gcode
    ([_] (point-seq->gcode (:points _)))
    ([_ r] (->gcode _)))

  LineStrip2
  (->gcode
    ([_] (point-seq->gcode (:points _)))
    ([_ r] (->gcode _)))

  Circle2
  (->gcode
    ([_] (point-seq->gcode (:points (g/as-polygon _))))
    ([_ r] (point-seq->gcode (:points (g/as-polygon _))))))

(comment
  (require '[thi.ng.geom.circle :as c])

  (->
   (->gcode (c/circle))
   (gcode-seq->str)))
