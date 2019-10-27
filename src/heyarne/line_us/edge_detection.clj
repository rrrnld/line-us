(ns heyarne.line-us.edge-detection
  (:require [clojure.set :as set]
            [opencv4.core :as cv]
            [opencv4.colors.rgb :as rgb]
            [opencv4.utils :as u])
  (:import [org.opencv.core Point Rect]))

(def img (-> (u/mat-from-url "https://raw.githubusercontent.com/hellonico/origami/master/doc/cat_in_bowl.jpeg")
             (cv/cvt-color! cv/COLOR_RGB2GRAY)
             (u/resize-by 0.2)
             (cv/canny! 300.0 100.0 3 true)
             (cv/bitwise-not!))) ;; NOTE: The bitwise-not! here is not actually needed

(defn pixels
  "Returns a vector of
   - all pixels in an org.opencv.core.Mat as a byte-array
   - width
   - height"
  [mat]
  (let [out (byte-array (* (.total mat) (.channels mat)))]
    (.get mat 0 0 out)
    [out (.cols mat) (.rows mat)]))

(defn neighborhood [pixels width height x y]
  (for [y-off [-1 0 1]
        x-off [-1 0 1]
        :let [y' (+ y y-off)
              x' (+ x x-off)]
        :when (and (< -1 x' width)
                   (< -1 y' height)
                   (not= y-off x-off 0))]
    [[x' y'] (nth pixels (+ x' (* y' width)))]))

(def shape? zero?)

(defn next-connection
  "Returns the first neighbor of a pixel that's part of the same shape"
  [pixels width height contour coord]
  (let [neighbors (apply neighborhood pixels width height coord)]
    (reduce (fn [_ [neighbor-coord v]]
              (when (and (shape? v)
                         (not (contour neighbor-coord)))
                (reduced neighbor-coord))) neighbors)))

(defn next-start [contour-pixels found-contours]
  (first (remove (apply set/union found-contours) contour-pixels)))

(defn radial-sweep
  "Returns a seq of sets, where each set represents one stroke / contour"
  [mat]
  (let [[pixels width height] (pixels mat)
        contour-pixels (for [idx (range (.total mat))
                             :when (shape? (nth pixels idx))]
                         [(rem idx width) (quot idx width)])
        start (first contour-pixels)]
    (loop [coord start
           contour #{start}
           found-contours []]
      (let [next (next-connection pixels width height contour coord)]
        (if (nil? next)
          contour
          ;; we're continuing with our current contour
          (recur next (conj contour next) found-contours))))))

(defn draw-coords! [mat [x y]]
  (cv/circle mat (cv/new-point x y) 1 rgb/green 0 cv/LINE_AA)
  mat)


(comment
  (u/show img)

  (radial-sweep img)

  (let [out (-> (cv/clone img)
                (cv/cvt-color! cv/COLOR_GRAY2RGB))]
    (doseq [xy (radial-sweep img)]
      (draw-coords! out xy))
    (u/show out {:frame {:width (.cols out) :height (.rows out)}})))
