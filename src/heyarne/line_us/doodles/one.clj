(ns heyarne.line-us.doodles.one
  (:require [heyarne.line-us.connection :as line-us]
            [clojure2d.core :as c2d]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.line :as l]
            [thi.ng.geom.polygon :as p]
            [thi.ng.geom.rect :as r]))

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn setup [canvas]
  (let [gap 5
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

(defn state->g01
  "Converts the state to plotting coordinates; scales the coordinates to fill
  the safe drawing area"
  [state]
  ;; FIXME: At the moment this doesn't preserve aspect ratio
  (let [bounds (->> (:lines state)
                    (mapcat :points)
                    (p/convex-hull*)
                    (p/polygon2)
                    (g/bounds))
        [min-x min-y] (r/bottom-left bounds)
        [max-x max-y] (r/top-right bounds)
        [left right] (:x line-us/drawing-area)
        [down up] (:y line-us/drawing-area)]
    (->> (:lines state)
         (map :points)
         ;; scale points to fill the whole drawing area
         (map (fn [points]
                (map #(-> (update % 0 lerp min-x max-x left right)
                          (update 1 lerp min-y max-y down up)) points)))
         ;; make sure the plotter touches the paper
         (map (fn [points]
                (mapv #(conj % 200) points)))
         ;; lift the plotter after each line and join the movements
         (mapcat (fn [points]
                   (conj points (assoc (last points) 2 1000)))))))

(defn plot!
  "Sends the current global state of a sketch to the line-us for drawing"
  [sketch]
  (let [movements (state->g01 (c2d/get-state sketch))
        println (partial println "Line-us:")]
    (with-open [line-us (line-us/connect "line-us.lan" 1337)]
      (println (line-us/read-response line-us))
      (Thread/sleep 1000)
      (doseq [coords movements]
        (println (line-us/send-movement! line-us coords)))
      (Thread/sleep 1000)
      (println "Done!"))))

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
  ;; to run the sketch, evaluate this line:
  (def sketch (-main))
  ;; to start plotting the sketch:
  (plot! sketch))
