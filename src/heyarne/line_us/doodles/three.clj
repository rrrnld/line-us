(ns heyarne.line-us.doodles.three
  (:require [clojure2d.core :as c2d]
            [clojure2d.pixels :as pix]
            [clojure2d.color :as color]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.line :as l]
            [thi.ng.geom.rect :as r]
            [thi.ng.geom.vector :as v]
            [thi.ng.geom.circle :as c]))

;; FIXME: This is not executable at the moment:
;; Execution error (UnsupportedOperationException) at heyarne.line-us.doodles.three/randlines (three.clj:35).
;; nth not supported on this type: Rect2

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

(defn randvec2
  "Generates a random vector that points to the right"
  []
  (g/rotate
   (v/vec2 [0 1])
   (m/random m/HALF_PI m/THREE_HALVES_PI)))

(defn fibonacci
  ([] (fibonacci 1 1))
  ([a b] (cons a (lazy-seq (fibonacci b (+ a b))))))

(def gaps (cons 0 (take 5 (rest (fibonacci)))))

(defn randlines [{:keys [size] :as _bounds} gap]
  ;; - generate a random line just outside of the drawing area
  ;; - generate a normal vector of length some fibonacci multiple of `gap`
  ;; - offset the line by the y component of that vector
  ;; - repeat until image is filled
  (let [[x y] size
        max-length (m/rootn (+ (* x x) (* y y)) 2)
        theta (m/random m/HALF_PI m/THREE_HALVES_PI)
        template (-> (l/line2 [(* max-length -0.5) 0] [(* max-length 0.5) 0])
                     (g/rotate theta))
        [min-x min-y max-x max-y] (g/bounds template)]
    [(g/translate template [(* 0.5 max-x) (* 0.5 max-y)])]
    #_(for [y-off (->>
                 (iterate #(+ % (* (rand-nth gaps) gap)) 0)
                 (take-while #(<= % y)))]
      (->
       (l/line2 (* -0.5 max-length) 0 (* 0.5 max-length) 0)
       (g/rotate theta)
       (g/translate [0 y-off])))))

(defn setup [{:keys [w h] :as canvas}]
  (let [gap 3]
    {:lines (randlines (r/rect 0 0 w h) gap)}))

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

#_(reset sketch)

(defn draw-state [{:keys [w h] :as canvas} state]
  (c2d/set-color canvas :ivory)
  (c2d/rect canvas 0 0 w h)
  (c2d/set-color canvas :light-blue)
  (doseq [[[x1 y1] [x2 y2]] (->> (:lines state)
                                 (map :points))]
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

;; reset when "r" is pressed
(defmethod c2d/key-pressed ["Doodle Three" \r] [_event _state]
  #_(reset sketch))

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
