(ns heyarne.line-us.doodles.three
  (:require [clojure2d.core :as c2d]
            [clojure2d.pixels :as pix]
            [clojure2d.color :as color]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.line :as l]
            [thi.ng.geom.rect :as r]))

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn load-normalized-img [path]
  (->>
   (pix/load-pixels path)
   (pix/filter-channels pix/normalize)
   (pix/filter-colors color/to-Gray*)))

(defn fit-to-canvas
  "Resizes an image so it fits onto the canvas without cutting while preserving
  the aspect ratio"
  [canvas img]
  (let [ratio (max (/ (c2d/width img) (:w canvas))
                   (/ (c2d/height img) (:h canvas)))]
    (cond-> img
      (> ratio 1) (c2d/resize (/ (c2d/width img) ratio) (/ (c2d/height img) ratio)))))

(defn offset [pixels x y min-v max-v max-off]
  (* max-off (/ (pix/get-value pixels 0 x y) max-v)))

(defn lines-from-img [w h img]
  (let [frame (r/rect 12 12 (- w 12) (- h 12))
        n-lines 180
        x-step 2
        y-step (/ h n-lines 2)
        y-offset 5
        min-v (reduce min (pix/get-channel img 0))
        max-v (reduce max (pix/get-channel img 0))]
    (->> (for [y (map #(+ y-step (* % (/ h n-lines))) (range n-lines))]
           (->
            (->> (map (fn [x]
                        (let [off1 (offset img x y min-v max-v y-offset)
                              off2 (offset img (+ x x-step) y min-v max-v y-offset)]
                          [x (+ y off1) (+ x x-step) (+ y off2)]))
                      (range 0 (inc w) x-step))
                 (l/linestrip2))))
         (flatten))))

(defn setup [{:keys [w h] :as canvas}]
  (let [narcissus (->> (load-normalized-img "resources/narcissus.jpg")
                       (fit-to-canvas canvas))]
    {:img narcissus
     :lines (lines-from-img w h narcissus)}))

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

#_(reset sketch)

(defn draw-state [{:keys [w h] :as canvas} state]
  (c2d/set-color canvas :white)
  (c2d/rect canvas 0 0 w h)
  #_(c2d/image canvas (:img state) 0 0)
  (c2d/set-color canvas :light-grey)
  (doseq [line (->> (lines-from-img w h (:img state))
                    (map (comp #(partition 2 1 %) :points)))
          [[x1 y1] [x2 y2]] line]
    (c2d/line canvas x1 y2 x2 y1)))


(defn update-state [canvas state]
  state)

(defn -main [& args]
  (let [canvas (c2d/canvas 300 400)]
    (c2d/show-window
     {:window-name "Doodle Three"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (update-state canvas state)))})))

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
