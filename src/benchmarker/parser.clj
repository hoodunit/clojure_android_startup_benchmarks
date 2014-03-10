(ns benchmarker.parser
  (:require [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]
            [clojure.string :as s]))

(defn parse-time [string]
  (let [time-ex #"\d\d-\d\d \d\d:\d\d:\d\d\.\d\d\d"
        formatter (time-fmt/formatter "MM-dd HH:mm:ss.SSS")
        time-str (re-find time-ex string)
        parsed-time (time-fmt/parse formatter time-str)]
    parsed-time))
  

(defn get-start-end-times [filename start-activity main-activity]
  (with-open [rdr (io/reader filename)]
    (let [start-ex (re-pattern (str "START u0.*" start-activity))
          end-ex (re-pattern (str "Displayed.*" main-activity))
          lines (line-seq rdr)
          matching (filter #(or (re-find start-ex %)
                                (re-find end-ex %)) lines)
          pairs (partition 2 matching)
          start-end (map (fn [[start end]] {:start (parse-time start) 
                                            :end (parse-time end)})
                         pairs)]
      (doseq [m matching] (println m))
      (doall start-end))))

(defn get-run-time [{:keys [start end]}]
  (let [interval (time/interval start end)
        milliseconds (.toDurationMillis interval)]
    milliseconds))

(defn get-run-times [start-end-times]
  (map get-run-time start-end-times))

(defn average [seq]
  (try (double (/ (reduce + seq) (count seq)))
       (catch ArithmeticException e nil)))

(defn print-file-results [filename {:keys [start-activity main-activity]}]
  (println "start1:" start-activity)
  (let [start-end (get-start-end-times filename start-activity main-activity)
        times (get-run-times start-end)]
    (println filename)
    (doseq [t times]
      (println t))
    (println "Average:" (average times))))

(defn print-app-test-results [app-dir app-config]
  (let [logcat-file (str app-dir "/logcat")]
    (println "app-config:" app-config)
    (print-file-results logcat-file app-config)))

(defn print-test-results [{:keys [test-apps output-dir]}]
  (let [get-app-dir-name #(last (s/split (:package-name %) #"\."))
        output-dir-files (.listFiles (io/file output-dir))
        test-dirs (filter #(.isDirectory %) output-dir-files)]
    (doseq [test-dir test-dirs]
      (let [test-dir-files (.listFiles (io/file test-dir))
            app-dirs (filter #(.isDirectory %) test-dir-files)]
        (doseq [app-dir app-dirs]
          (if-let [app-config (first (filter #(= (.getName app-dir) (get-app-dir-name %)) test-apps))]
            (print-app-test-results app-dir app-config)
            (println "No matching app config found for dir " (.getName app-dir))))))))
