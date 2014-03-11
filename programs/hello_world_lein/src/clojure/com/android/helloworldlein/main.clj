(ns com.android.helloworldlein.main
  (:use [neko.activity :only [defactivity set-content-view!]]
        [neko.threading :only [on-ui]]
        [neko.ui :only [make-ui]]))

(defactivity com.android.helloworldlein.HelloWorld
  :on-create
  (fn [this bundle]
    (on-ui
     (set-content-view! this
      (make-ui [:linear-layout {}
                [:text-view {:text "Hello from Clojure!"}]])))))

