(ns heyarne.line-us.doodles.two
  (:require [heyarne.line-us.connection :as line-us]
            [clojure2d.core :as c2d]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.line :as l]
            [thi.ng.geom.polygon :as p]
            [thi.ng.geom.rect :as r])
  (:import [java.awt Font]
           [java.awt.geom PathIterator]))

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

;; NOTE: These are some experiments to convert vector text to a path

(def readable-seg-type
  {PathIterator/SEG_QUADTO ::quad-to
   PathIterator/SEG_MOVETO ::move-to
   PathIterator/SEG_LINETO ::line-to
   PathIterator/SEG_CUBICTO ::cubic-to
   PathIterator/SEG_CLOSE ::close})

(defn draw-path [canvas [seg-type coords]]
  (doseq [[x y] (partition 2 coords)]
    (when-not (= 0 x y)
      (c2d/point canvas x y))))

(defn path-iterator->seq [^PathIterator pi]
  (when-not (.isDone pi)
    (let [pt (double-array 6)
          seg-type (.currentSegment pi pt)]
      (.next pi)
      (lazy-cat [[(readable-seg-type seg-type) (vec pt)]] (path-iterator->seq pi)))))

(c2d/with-canvas [c (c2d/get-canvas sketch)]
  (let [frc (.. c graphics getFontRenderContext)
        font (Font. "Courier" Font/PLAIN 40)
        glyph-vector (. font (createGlyphVector frc "hi"))
        path-iterator (.. glyph-vector getOutline (getPathIterator nil))]
    (path-iterator->seq path-iterator)))
;; => ([:heyarne.line-us.doodles.two/move-to [17.328125 -0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [17.328125 -13.8125 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [17.328125 -18.890625 12.671875 -18.890625 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [6.6875 -18.890625 6.6875 -11.109375 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [6.6875 -0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [3.125 -0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [3.125 -30.390625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [6.6875 -30.390625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [6.6875 -21.375 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [6.53125 -18.5625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [6.71875 -18.5625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [8.734375 -21.84375 13.28125 -21.84375 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to [20.875 -21.84375 20.875 -14.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [20.875 -0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [17.328125 -0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/close [0.0 0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/move-to [36.36328125 -30.28125 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [38.44140625 -30.28125 38.44140625 -28.046875 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [38.44140625 -26.921875 37.82421875 -26.359375 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [37.20703125 -25.796875 36.36328125 -25.796875 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [34.28515625 -25.796875 34.28515625 -28.046875 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/quad-to
;;      [34.28515625 -30.28125 36.36328125 -30.28125 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/close [0.0 0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/move-to
;;      [34.56640625 -18.640625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to
;;      [29.31640625 -19.046875 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to
;;      [29.31640625 -21.453125 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to
;;      [38.12890625 -21.453125 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [38.12890625 -2.796875 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [45.00390625 -2.40625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [45.00390625 0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [27.84765625 0.0 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [27.84765625 -2.40625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to [34.56640625 -2.796875 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/line-to
;;      [34.56640625 -18.640625 0.0 0.0 0.0 0.0]]
;;     [:heyarne.line-us.doodles.two/close [0.0 0.0 0.0 0.0 0.0 0.0]])

(defn setup [canvas]
  {})

(defn draw-state [canvas state]
  (c2d/with-canvas-> canvas
    (c2d/set-color :white)
    (c2d/rect 0 0 (:w canvas) (:h canvas))
    (c2d/set-color :salmon)
    (c2d/line 10 10 (- (:w canvas) 10) (- (:h canvas) 10))
    (c2d/line 10 (- (:h canvas) 30) (- (:w canvas) 10) 30)))

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
