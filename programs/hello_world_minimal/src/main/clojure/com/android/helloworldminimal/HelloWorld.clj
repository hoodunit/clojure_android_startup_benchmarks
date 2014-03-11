(ns com.android.helloworldminimal.HelloWorld
  (:gen-class
   :extends android.app.Activity
   :exposes-methods {onCreate superOnCreate})
  (:import [android.app Activity]
           [android.os Bundle]))

(defn -onCreate [this #^android.os.Bundle bundle ]
  (.superOnCreate this bundle)
  (.setContentView this com.android.helloworldminimal.R$layout/main))
