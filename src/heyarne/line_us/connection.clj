(ns heyarne.line-us.connection
  (:require [clojure.java.io :as io])
  (:import [java.net Socket]))

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
  [coord min-v max-v]
  `(when-not (< ~(dec min-v) ~coord ~(inc max-v))
     (throw (IllegalArgumentException.
             (str ~(name coord) " should be between [" ~(dec min-v) ", " ~(inc max-v) "] but is " ~coord)))))

(defn send-movement! [^Socket line-us [^int x ^int y ^int z :as coords]]
  ;; https://github.com/Line-us/Line-us-Programming/blob/master/Documentation/LineUsDrawingArea.pdf
  (validate-coord x -1000 1000)
  (validate-coord y 650 1775)
  (validate-coord z 0 1000)
  ;; this is basically taken from the Processing example code and the processing
  ;; "Client" class
  (doto (io/output-stream line-us)
    (.write (.getBytes (str "G01 X" x " Y" y " Z" z "\0")))
    (.flush))
  ;; wait for the response
  (let [res (read-response line-us)]
    (if-not (re-find #"^ok" res)
      (throw (Exception. res))
      coords)))

(defn move-home [^Socket line-us]
  (send-movement! line-us [1000 1000 1000]))

(move-home s)
(def msg *1)

(send-movement! s [100 1000 1000])
