(ns heyarne.line-us.connection
  (:require [clojure.java.io :as io])
  (:import [java.net Socket]))

;; https://github.com/Line-us/Line-us-Programming/blob/master/Documentation/LineUsDrawingArea.pdf
(def drawing-area
  {:x [800 1700]
   :y [-900 900]
   :z [0 1000]})

(defn connect
  [url port]
  (Socket. url port))

(defn disconnect [^Socket line-us]
  (.close line-us))

(defn read-response [^Socket line-us]
  (let [stream (io/input-stream line-us)]
    (loop [line []]
      (let [c (.read stream)]
        (case c
          0 (when-not (empty? line)
              (apply str (map char line)))
          -1 (recur line)
          (recur (conj line c)))))))

(defmacro validate-coord
  "Throws an exception when a coordinate is outside of the drawable area"
  [coord]
  (let [[min-v# max-v#] (get drawing-area (keyword (name coord)))]
    `(when-not (< ~(dec min-v#) ~coord ~(inc max-v#))
       (throw (IllegalArgumentException.
               (str ~(name coord) " should be in range [" ~min-v# ", " ~max-v# "] but is " ~coord))))))

(defn- send-command! [^Socket line-us ^String raw-cmd]
  ;; this is basically taken from the Processing example code and the processing
  ;; "Client" class
  (doto (io/output-stream line-us)
    (.write (.getBytes (str raw-cmd "\0")))
    (.flush))
  ;; wait for the response
  (let [res (read-response line-us)]
    (if-not (re-find #"^(ok|hello)" res)
      (throw (Exception. res))
      res)))

(defn send-movement!
  "Moves the arm to the [x y z] vector that is coord. Coordinates are validated
  to be inside the valid drawing area."
  [^Socket line-us [x y z :as coords]]
  (validate-coord x)
  (validate-coord y)
  (validate-coord z)
  (send-command! line-us (str "G01 X" x " Y" y " Z" z))
  coords)

(defn move-home! [^Socket line-us]
  (send-movement! line-us [1000 1000 1000]))

(defn- parse-coords [^String raw-coords]
  (->>
   (re-find #"X:(-?\d+) Y:(\d+) Z:(\d+)" raw-coords)
   (rest)
   (mapv #(Integer/parseInt % 10))))

(defn move-up! [^Socket line-us]
  (let [[x y _] (parse-coords (send-command! line-us "M114"))]
    (send-movement! line-us [x y 1000])))

(defn move-down! [^Socket line-us]
  (let [[x y _] (parse-coords (send-command! line-us "M114"))]
    (send-movement! line-us [x y 200])))
