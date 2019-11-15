(ns heyarne.line-us.gcode
  "Provides functions to move from geometry types provided by thi.ng to gcode
  so you can plot them."
  (:import [thi.ng.geom.types Line2 LineStrip2]))

(defprotocol GCode
  "Convert thi.ng.geom types into sequences of GCode to plot them. The gcode
  instructions are given as a sequence of vectors, where each vector is given
  as [x y z]."
  (->gcode [_] [_ r]
    "Returns G01 movements on the x, y, and z axis. Z=0 implies that the
    plotter head is on paper, z=1000 implies it's up.
    The optional parameter `r` can be used to adjust the resolution."))

(defn gcode-seq->str [gcode-seq]
  (map (fn [[x y z]]
         (str "G01 X" x " Y" y " Z" z))))

(defn- line->gcode [l]
  (let [vertices (:points l)
        [l-x l-y] (last vertices)]
    (conj (mapv (fn [[x y]]
                  [x y 0]) vertices)
          [l-x l-y 1000])))

(extend-protocol GCode
  Line2
  (->gcode
    ([_] (line->gcode _))
    ([_ r] (->gcode _)))

  LineStrip2
  (->gcode
    ([_] (line->gcode _))
    ([_ r] (->gcode _))))
