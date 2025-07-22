(ns guis.dev
  (:require [guis.core :as guis]))


(defonce state (atom {:number 0}))

(defn main []
  (guis/init state)
  (println "Loaded!"))

(defn ^:dev/after-load reload []
  (guis/init state)
  (println "Reloaded!"))
