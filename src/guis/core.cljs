(ns guis.core
  (:require [replicant.dom :as r]
            [clojure.walk :as walk]
            [guis.counter :as counter]
            [guis.temperature :as temperature]
            [guis.layout :as layout]))

(def views
  [{:id :counter
    :text "Counter"}
   {:id :temperatures
    :text "Temperatures"}])

(defn get-current-view [state]
  (:current-view state))

(defn render-ui [state]
  (let [currnt-view (get-current-view state)]
    [:div.m-8
     (layout/tab-bar currnt-view views)
     (case currnt-view
       :counter (counter/render-ui state)
       [:h1.text-lg "Select your UI of choice"]

       :temperatures
       (temperature/render-ui state))]))

(defn process-effect [store [effect & args]]
  (case effect
    :effect/assoc-in
    (apply swap! store assoc-in args)))

(defn perform-actions [state event-data]
  (mapcat
   (fn [action]
     (or (counter/perform-action state action)
         (case (first action)
           :action/assoc-in
           [(into [:effect/assoc-in] (rest action))])))
   event-data))

(defn interpolate [event data]
  (walk/postwalk
   (fn [x]
     (case x
       :event.target/value-as-number
       (some-> event .-target .-valueAsNumber)
       x))
   data))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              (r/render
                               js/document.body
                               (render-ui new-state))))

  (r/set-dispatch! (fn [{:replicant/keys [dom-event]} event-data]
                     (js/console.log dom-event)
                     (->> (interpolate dom-event event-data)
                          (perform-actions @store)
                          (run! #(process-effect store %)))))

  (swap! store assoc ::loaded-at (.getTime (js/Date.))))

