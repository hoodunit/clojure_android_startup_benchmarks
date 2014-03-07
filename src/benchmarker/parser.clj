(ns benchmarker.parser
  (:require [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]))

(def start-ex #"START u0.*HelloWorld")
(def end-ex #"Displayed.*HelloWorld")

(defn parse-time [string]
  (let [time-ex #"\d\d-\d\d \d\d:\d\d:\d\d\.\d\d\d"
        formatter (time-fmt/formatter "MM-dd HH:mm:ss.SSS")
        time-str (re-find time-ex string)
        parsed-time (time-fmt/parse formatter time-str)]
    parsed-time))
  

(defn get-start-end-times [filename]
  (with-open [rdr (io/reader filename)]
    (let [lines (line-seq rdr)
          matching (filter #(or (re-find start-ex %)
                                (re-find end-ex %)) lines)
          pairs (partition 2 matching)
          start-end (map (fn [[start end]] {:start (parse-time start) 
                                            :end (parse-time end)})
                         pairs)]
      (doall start-end))))

(defn get-run-time [{:keys [start end]}]
  (let [interval (time/interval start end)
        milliseconds (.toDurationMillis interval)]
    milliseconds))

(defn get-run-times [start-end-times]
  (map get-run-time start-end-times))

(defn average [seq]
  (double (/ (reduce + seq) (count seq))))

(defn print-file-results [filename]
  (let [start-end (get-start-end-times filename)
        times (get-run-times start-end)]
    (println filename)
    (doseq [t times]
      (println t))
    (println "Average:" (average times))))
