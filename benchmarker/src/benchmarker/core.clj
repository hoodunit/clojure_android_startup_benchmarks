(ns benchmarker.core
  (:gen-class)
  (:require [benchmarker.parser :as parser]
            [clojure.string :as s]
            [clojure.java.shell :as shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [me.raynes.conch :as conch]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]))

(def config-file "config.clj")
(def stdout-filename "stdout")
(def stderr-filename "stderr")
(def logcat-filename "logcat")
(def device-info-filename "device_info")

(defn load-config []    
  (with-open [rdr (-> config-file
                      io/reader
                      java.io.PushbackReader.)]
    (edn/read rdr)))

(conch/programs adb ant)

(defn current-minute-str []
  (time-fmt/unparse (time-fmt/formatter "yyyyMMdd_HHmm" (time/default-time-zone)) (time/now)))

(defn make-test-dir! [output-dir]
  (let [test-dir-name (str output-dir "/" (current-minute-str))]
    (.mkdir (io/file test-dir-name))
    test-dir-name))

(defn make-app-dir! [{:keys [package-name]} test-dir]
  (let [app-dir-name (last (s/split package-name #"\."))
        app-dir (str test-dir "/" app-dir-name)]
    (.mkdir (io/file app-dir))
    app-dir))

(defn restart-phone [opts]
  (adb "shell" "reboot" opts)
  (adb "wait-for-device")
  (Thread/sleep 120000))

(defn run-test [{:keys [package-name start-activity]} opts]
  (adb "shell" "am" "start" "-n" (str package-name "/." start-activity) opts)
  (Thread/sleep 20000)
  (adb "shell" "pm" "clear" package-name opts)
  (Thread/sleep 1000))

(defn save-logcat [log-dir opts]
  (with-open [logcat-file (io/writer (str log-dir "/" logcat-filename))]
    (adb "logcat" "-d" "-v" "time" (assoc opts :out logcat-file))))

(defn get-prop [prop opts]
  (let [result (adb "shell" "getprop" prop (assoc opts :verbose true))
        value (get-in result [:proc :out])]
    (s/trim-newline (first value))))

(defn get-props [props opts]
  (into {} (map (fn [prop] [prop (get-prop prop opts)]) props)))

(defn save-device-info [device-props info-dir opts]
  (let [props (get-props device-props opts)
        prop-strs (map (fn [[k v]] (str "[" k "]: [" v "]\n")) props)]
    (with-open [device-info-file (io/writer (str info-dir "/" device-info-filename))]
      (doseq [p (sort prop-strs)] (.write device-info-file p)))))

(defn execute-test [app-config test-dir device-props]
  (let [app-dir (make-app-dir! app-config test-dir)
        stdout-file (io/file (str app-dir "/" stdout-filename))
        stderr-file (io/file (str app-dir "/" stderr-filename))
        num-trials 10]
    (with-open [stdout-writer (io/writer stdout-file)
                stderr-writer (io/writer stderr-file)]
      (let [opts {:dir (:project-dir app-config)
                  :out stdout-writer
                  :err stderr-writer}
            flush-logs (fn [] (.flush stdout-writer) (.flush stderr-writer))]
        (save-device-info device-props app-dir opts)
        (flush-logs)
        (restart-phone opts)
        (flush-logs)
        (doall (repeatedly num-trials (fn [] (run-test app-config opts) (flush-logs))))
        (save-logcat app-dir opts)))))

(defn execute-tests [{:keys [test-apps output-dir device-props]}]
  (let [test-dir (make-test-dir! output-dir)]
    (doseq [app-config test-apps]
      (try (execute-test app-config test-dir device-props)
           (catch Exception e (println "Exception, aborting test:" (.getMessage e)))))))

(defn -main
  [& args]
  (let [config (load-config)]
    (if (= (first args) "results")
      (parser/print-test-results config)
      (execute-tests config))))

