(ns guis.core
  (:require [replicant.dom :as r]
            [clojure.walk :as walk]
            [guis.counter :as counter]
            [guis.temperature :as temperature]
            [guis.flights :as flights]
            [guis.layout :as layout]))

(def views
  [{:id :counter
    :text "Counter"}
   {:id :temperatures
    :text "Temperatures"}
   {:id :flights
    :text "Flights"}])

(defn get-current-view [state]
  (:current-view state))

(defn render-ui [state]
  (let [currnt-view (get-current-view state)]
    [:div.m-8
     (layout/tab-bar currnt-view views)
     (case currnt-view
       :counter
       (counter/render-ui state)

       :temperatures
       (temperature/render-ui state)

       :flights
       (flights/render-ui state)

       [:h1.text-lg "Select your UI of choice"])]))

(defn process-effect [store [effect & args]]
  (case effect
    :effect/assoc-in
    (let [r (apply swap! store assoc-in args)]
      (prn r)
      r)))

(defn perform-actions [state event-data]
  (mapcat
   (fn [action]
     (or (counter/perform-action state action)
         (temperature/perform-action state action)
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

       :event.target/value-as-keyword
       (some-> event .-target .-value keyword)

       :event.target/value
       (some-> event .-target .-value)

       x))
   data))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              (r/render
                               js/document.body
                               (render-ui (assoc new-state :now (js/Date.))))))

  (r/set-dispatch! (fn [{:replicant/keys [dom-event]} event-data]
                     (js/console.log dom-event)
                     (->> (interpolate dom-event event-data)
                          (perform-actions @store)
                          (run! #(process-effect store %)))))

  (swap! store assoc ::loaded-at (.getTime (js/Date.))))

