(ns heyarne.line-us.doodles.twothousand
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
      (lazy-seq (cons [(readable-seg-type seg-type) (vec pt)] (path-iterator->seq pi))))))

(defn setup [canvas]
  {})

(defn draw-state [canvas state]
  (c2d/set-color canvas :white)
  (c2d/rect canvas 0 0 (:w canvas) (:h canvas))
  (c2d/set-color canvas :salmon)
  (c2d/translate canvas [(* 0.5 (:w canvas)) (* 0.5 (:h canvas))])
  (let [ctx (.. canvas graphics getFontRenderContext)
        font (Font. "Courier" Font/PLAIN 40)
        glyph-vector (. font (createGlyphVector ctx "hi"))
        path-iterator (.. glyph-vector getOutline (getPathIterator nil))]
    (doseq [segment (path-iterator->seq path-iterator)]
      (draw-path canvas segment))))

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
