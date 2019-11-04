(ns heyarne.line-us.doodles.two
  (:require [clojure2d.core :as c2d]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as v]
            [thi.ng.geom.circle :as c]))


(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn setup [canvas]
  (let [t (v/vec2 5 16)
        r 20]
    {:circles (concat
               (for [i (range 1 10)]
                 (->
                  (c/circle (* i r))
                  (g/translate (m/* t (- i)))
                  (g/translate (m/* (v/vec2 10 7.35) 6))))
               (for [i (range 1 10)]
                 (->
                  (c/circle (* i r))
                  (g/translate (m/* t i))
                  (g/translate (m/* (v/vec2 -10 -7.35) 6)))))}))

(reset sketch)

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

(defn draw-state [canvas state]
  (let [{:keys [w h]} canvas
        n-circles (count (:circles state))
        [top-circles bottom-circles] (split-at (/ n-circles 2) (:circles state))]
    (c2d/set-color canvas :white)
    (c2d/rect canvas 0 0 w h)
    (c2d/set-color canvas :salmon)
    (c2d/translate canvas (/ w 2) (/ h 2))
    (doseq [{[x y] :p, r :r} top-circles]
      (c2d/arc canvas x y r r m/THIRD_PI (- m/PI)))
    (doseq [{[x y] :p, r :r} bottom-circles]
      (c2d/arc canvas x y r r m/THIRD_PI (* 1.4 m/PI)))))

(defn update-state [canvas state]
  state)

(defn -main [& args]
  (let [canvas (c2d/canvas 300 400)]
    (c2d/show-window
     {:window-name "Doodle Two"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (update-state canvas state)))})))

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
