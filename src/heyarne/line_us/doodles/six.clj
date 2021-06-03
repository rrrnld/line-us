(ns heyarne.line-us.doodles.six
  (:require [clojure2d.core :as c2d]
            [thi.ng.math.core :as m]
            [thi.ng.math.noise :as n]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as v]
            [thi.ng.geom.rect :as r]
            [thi.ng.geom.circle :as c]))

(defn lerp
  "Normalizes a value from [min-v, max-v] to [min-t, max-t]"
  [v min-v max-v min-t max-t]
  (+ min-t (* (- max-t min-t) (/ (- v min-v) (- max-v min-v)))))

;; particle logic

(def n-particles 50)
(def palette (vec
              (concat (repeat 3 :light-grey)
                      [:pink])))

(defrecord Particle [p direction health color])

(defn spawn-particle [{:keys [w h] :as _canvas}]
  (let [center (m/* (v/vec2 w h) 0.5)
        direction (-> (v/randvec2 (min (* 0.1 w) (* 0.1 h)))
                      (m/* (m/random)))
        p (g/translate direction center)]
    (Particle. p (m/normalize direction) (m/random 100) (rand-nth palette))))

(defn update-particle [{[x y] :p :as particle}]
  (let [step 2
        turbulence (v/randvec2 (* step (n/noise2 x y)))
        dir (-> (:direction particle)
                (m/+ turbulence)
                (m/* step))]
    (->
     (update particle :health - step)
     (update :p m/+ dir))))

(defn draw-particle [canvas {[x y] :p, health :health, color :color}]
  (let [r (* 0.25 health)]
    (c2d/filled-with-stroke canvas :white color c2d/ellipse x y r r)))

;; higher-level sketch logic

(defn setup [canvas]
  {:particles (repeatedly n-particles #(spawn-particle canvas))})

(defn reset [w]
  (c2d/set-state! w (setup (c2d/get-canvas w))))

#_(reset sketch)

(defn draw-state [{:keys [w h] :as canvas} {:keys [particles] :as _state}]
  (doseq [particle particles]
    (draw-particle canvas particle)))

(defn update-state [canvas state]
  ;; update each particle's position and remove them from the sketch when their
  ;; health is < 0
  (let [state (update state :particles
                      (fn [particles]
                        (->> (map update-particle particles)
                             (filter #(> (:health %) 0)))))]
    ;; spawn new particles. such is life
    (cond-> state
      (< (count (:particles state)) n-particles)
      (update :particles conj (spawn-particle canvas)))))

(defn -main [& args]
  (let [w 300
        h 400
        canvas (c2d/with-canvas->
                (c2d/canvas w h)
                (c2d/set-color :white)
                (c2d/rect 0 0 w h))]
    (c2d/show-window
     {:window-name "Doodle Six"
      :canvas canvas
      :state (setup canvas)
      :draw-fn (fn [canvas window _frame _local-state]
                 (let [state (c2d/get-state window)]
                   (draw-state canvas state)
                   (c2d/set-state! window (update-state canvas state))))})))

(declare sketch)

;; reset when "r" is pressed
(defmethod c2d/key-pressed ["Doodle Six" \r] [_event _state]
  (let [c (c2d/get-canvas sketch)]
    (c2d/with-canvas-> c
      (c2d/set-color :white)
      (c2d/rect 0 0 (:w c) (:h c)))
    (reset sketch)))

(comment
  ;; to run the sketch, evaluate this line:
  (def sketch (-main)))
