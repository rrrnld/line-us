(ns heyarne.line-us.doodles.one
  (:require [clojure2d.core :as c2d]
            [thi.ng.geom.line :as l]))

(defn lerp [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn setup [canvas]
  (let [gap 10
        segment-padding (* 25 gap)]
    {:lines (for [y (range gap (inc (- (:h canvas) gap)) gap)
                  :let [perc (/ (- y gap) (- (:h canvas) (* 2 gap)))
                        cos (Math/cos (* Math/PI 2 perc))
                        segment-x (lerp cos -1 1
                                        (* 0.5 segment-padding)
                                        (- (:w canvas) (* 0.5 segment-padding)))
                        segment-y (+ y (lerp cos -1 1 40 0))]]
              (l/linestrip2 [gap y] [segment-x segment-y] [(- (:w canvas) gap) y]))}))

(defn draw-state [canvas state]
  (c2d/set-color canvas :white)
  (c2d/rect canvas 0 0 (:w canvas) (:h canvas))
  (c2d/set-color canvas :salmon)
  (doseq [line (:lines state)]
    (let [[start segment end] (:points line)]
      (c2d/line canvas start segment)
      (c2d/line canvas segment end))))

(defn update-state [canvas state]
  state)

(defn -main [& args]
  (let [canvas (c2d/canvas 400 400)]
    (c2d/show-window
     {:window-name "Doodle One"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (update-state canvas state)))})))

(comment
  (def sketch (-main)))
