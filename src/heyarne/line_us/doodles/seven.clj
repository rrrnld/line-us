(ns heyarne.line-us.doodles.seven
  (:require [clojure2d.core :as c2d]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.circle :as c]
            [thi.ng.geom.polygon :as p])
  (:import [thi.ng.geom.types Polygon2 Rect2]))

(declare sketch)

(defn bisect
  "Returns the normalized vector bisecting v1 and v2"
  [v1 v2]
  (m/normalize (m/+! (m/* v1 (m/mag v2)) (m/* v2 (m/mag v1)))))

(defn shrink-polygon [p size]
  (let [vertices (g/vertices p)
        ;; we partition and use the first vertex as pad so we have can cycle
        ;; back to the beginning
        line-vecs (->> (partition 2 1 [(first vertices)] vertices)
                       (map (fn [[a b]] (m/- b a))))]
    (->> (partition 2 1 [(first line-vecs)] line-vecs)
         (map #(-> (apply bisect %2)
                   (g/scale size)
                   (g/translate %1)) vertices))))
;; draw helpers

(defprotocol Drawable
  (stroke [_ c] "Draws a thi.ng.geom.type with clojure2d functions"))

(extend-protocol Drawable
  Polygon2
  (stroke [_ c]
    (let [vertices (g/vertices _)]
      (doseq [[[ax ay] [bx by]] (partition 2 1 [(first vertices)] vertices)]
        (c2d/line c ax ay bx by))))

  Rect2
  (stroke [_ c]
    (let [{[x y] :p, [w h] :size} _]
      (c2d/rect c x y w h true))))

;; higher-level sketch logic

(defn setup [canvas]
  {})

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

#_(reset sketch)
(defn draw-state [{:keys [w h] :as c} state]
  (let [base (g/as-polygon (c/circle (* 0.5 w) (* 0.5 h) 100) 4)]
    (c2d/set-color c :white)
    (c2d/rect c 0 0 w h)
    (c2d/set-color c :light-green)
    (doseq [i (range 1 1)
            :let [shrunk (p/polygon2 (shrink-polygon base (* 3 i)))]]
      (stroke shrunk c))))

(defn update-state [canvas state]
  state)

(defn -main [& args]
  (let [w 300
        h 400
        canvas (c2d/with-canvas->
                (c2d/canvas w h)
                (c2d/set-color :white)
                (c2d/rect 0 0 w h))]
    (c2d/show-window
     {:window-name "Doodle Seven"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (c2d/set-state! window (update-state canvas state))))})))

;; reset when "r" is pressed
(defmethod c2d/key-pressed ["Doodle Seven" \r] [_event _state]
  (let [c (c2d/get-canvas sketch)]
    (c2d/with-canvas-> c
      (c2d/set-color :white)
      (c2d/rect 0 0 (:w c) (:h c)))
    (reset sketch)))

;; save when "s" is pressed
(defmethod c2d/key-pressed ["Doodle Seven" \s] [_ _]
  (let [timestamp (.format (java.text.SimpleDateFormat. "yyyy-MM-dd--HH-mm-ss")
                           (java.util.Date.))
        name (str timestamp ".png")]
    (c2d/save sketch name)))

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
