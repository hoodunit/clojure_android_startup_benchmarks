(defproject log_parser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.6.0"]]
  :main ^:skip-aot log-parser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
