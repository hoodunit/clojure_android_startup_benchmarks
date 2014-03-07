(ns benchmarker.core
  (:gen-class)
  (:require [benchmarker.parser :as parser]))

(defn -main
  [& args]
  (let [filename (first args)]
    (parser/print-file-results filename)))

