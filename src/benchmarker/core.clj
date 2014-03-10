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

(def config (load-config))
(def device-props (:device-props config))
(def output-dir (:output-dir config))
(def test-apps (:test-apps config))

(conch/programs adb ant)

(defn current-minute-str []
  (time-fmt/unparse (time-fmt/formatter "yyyyMMdd_HHmm" (time/default-time-zone)) (time/now)))

(defn make-log-dir! [project-dir output-dir]
  (let [project-dir-name (last (s/split project-dir #"/"))
        test-dir (str output-dir "/" (current-minute-str))
        log-dir (str test-dir "/" project-dir-name)]
    (.mkdir (io/file test-dir))
    (.mkdir (io/file log-dir))
    log-dir))

(defn uninstall-app [package-name opts]
  (adb "uninstall" package-name opts))

(defn install-app [opts]
  (ant "release" opts)
  (ant "installr" opts))

(defn restart-phone [opts]
  (adb "shell" "reboot" opts)
  (adb "wait-for-device")
  (Thread/sleep 60000))

(defn run-test [{:keys [package-name start-activity]} opts]
  (adb "shell" "am" "start" "-n" (str package-name "/." start-activity) opts)
  (Thread/sleep 20000)
  (adb "shell" "am" "force-stop" package-name opts)
  (Thread/sleep 5000))

(defn save-logcat [log-dir opts]
  (with-open [logcat-file (io/writer (str log-dir "/" logcat-filename))]
    (adb "logcat" "-d" "-v" "time" (assoc opts :out logcat-file))))

(defn get-prop [prop opts]
  (let [result (adb "shell" "getprop" prop (assoc opts :verbose true))
        value (get-in result [:proc :out])]
    (s/trim-newline (first value))))

(defn get-props [props opts]
  (into {} (map (fn [prop] [prop (get-prop prop opts)]) props)))

(defn save-device-info [log-dir opts]
  (let [props (get-props device-props opts)
        prop-strs (map (fn [[k v]] (str "[" k "]: [" v "]\n")) props)]
    (with-open [device-info-file (io/writer (str log-dir "/" device-info-filename))]
      (doseq [p (sort prop-strs)] (.write device-info-file p)))))

(defn execute-test [app-config]
  (let [log-dir (make-log-dir! (:project-dir app-config) output-dir)
        stdout-file (io/file (str log-dir "/" stdout-filename))
        stderr-file (io/file (str log-dir "/" stderr-filename))
        num-trials 10]
    (with-open [stdout-writer (io/writer stdout-file)
                stderr-writer (io/writer stderr-file)]
      (let [opts {:dir (:project-dir app-config)
                  :out stdout-writer
                  :err stderr-writer}
            flush-logs (fn [] (.flush stdout-writer) (.flush stderr-writer))]
        (save-device-info log-dir opts)
        (flush-logs)
        (uninstall-app (:package-name app-config) opts)
        (install-app opts)
        (flush-logs)
        (restart-phone opts)
        (flush-logs)
        (doall (repeatedly num-trials (fn [] (run-test app-config opts) (flush-logs))))
        (save-logcat log-dir opts)))))

(defn execute-tests []
  (doseq [app-config test-apps]
    (execute-test app-config)))

(defn -main
  [& args]
  (execute-tests))

