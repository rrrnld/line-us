(ns heyarne.line-us.helpers
  (:require [thi.ng.geom.rect :as rect]
            [thi.ng.math.core :as m]))

(defn g01-bounds
  "Returns a rect representing the drawing bounds of a sequence of g01 coords"
  [g01]
  (let [bounds [Double/MAX_VALUE Double/MIN_VALUE Double/MIN_VALUE Double/MAX_VALUE]
        [top right bottom left] (reduce (fn [[top right bottom left] [x y _]]
                                          [(min top y) (max right x)
                                           (max bottom y) (min left x)])
                                        bounds
                                        g01)]
    (rect/rect [left bottom] [right top])))

(defn rescale
  "Returns a new g01 sequence that is proportionally scaled to fit into the
  bounding box passed in as the second argument. Assumes that the top left
  in g01-seq is at [0 0]."
  [g01-seq [top right bottom left]]
  (let [s-bounds (g01-bounds g01-seq)
        t-bounds (rect/rect [left bottom] [right top])
        ;; we need to translate the bounding box
        [translate-x translate-y] (m/+ (:p s-bounds) (:p t-bounds))
        ;; and scale it
        [s-x s-y] (:p s-bounds)
        [t-x t-y] (:p t-bounds)
        [s-width s-height] (:size s-bounds)
        [t-width t-height] (:size t-bounds)
        factor (min (/ (+ t-x t-width) (+ s-x s-width)) (/ (+ t-y t-height) (+ s-y s-height)))]
    (map (fn [coord]
           (-> (update coord 0 #(+ translate-x (* % factor)))
               (update 1 #(+ translate-y (* % factor)))))
         g01-seq)))
