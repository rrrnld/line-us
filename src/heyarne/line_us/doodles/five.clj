(ns heyarne.line-us.doodles.two
  (:require [clojure2d.core :as c2d]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as v]
            [thi.ng.geom.circle :as c]
            [heyarne.line-us.gcode :refer [->gcode]]))

(declare sketch)

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn setup [{:keys [w h] :as _canvas}]
  (let [center (v/vec2 (* 0.5 w) (* 0.5 h))]
    {:circles (for [i (range 1 26)]
                (->
                 (c/circle center (* 10 i))
                 (g/translate (v/vec2 0 (* 10 (Math/sin (* m/TWO_PI (/ i 25))))))))}))

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

#_(reset sketch)

(defn draw-state [{:keys [w h] :as canvas} {:keys [circles] :as _state}]
  (c2d/set-color canvas :white)
  (c2d/rect canvas 0 0 w h)
  (c2d/set-color canvas :salmon)
  (doseq [{[x y] :p, r :r} circles]
    (c2d/arc canvas x y r r 0 m/TWO_PI)))

(defn update-state [canvas state]
  state)

(defn -main [& args]
  (let [canvas (c2d/canvas 300 400)]
    (c2d/show-window
     {:window-name "Doodle Five"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (update-state canvas state)))})))

(c2d/get-state sketch)

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
