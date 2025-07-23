(ns guis.flights)

(defn parse-date [s]
  (when (string? s)
    (when-let [[_ y m d] (re-find #"(\d\d\d\d).(\d\d).(\d\d)" s)]
      (str y "-" m "-" d))))

(defn format-inst [inst]
  (parse-date (pr-str inst)))

(defn prepare-departure-date [state]
  (cond-> {:value (or (::departure-date state)
                      (format-inst (:now state)))}
    (::departure-date state)
    (assoc :invalid? (-> (::departure-date state)
                         parse-date
                         nil?))))

(defn prepare-return-date [state flight-type departure-date]
  (let [enabled? (= flight-type :roundtrip)]
    (cond-> {:value (or (::return-date state)
                        (:value departure-date)
                        (format-inst (:now state)))
             :disabled? (not enabled?)}
      (and enabled? (::return-date state))
      (assoc :invalid? (-> (::return-date state)
                           parse-date
                           nil?)))))

(defn before? [a b]
  (< (compare a b) 0))

(defn get-form-state [state]
  (let [flight-type (or (::type state) :one-way)
        departure-date (prepare-departure-date state)
        return-date (prepare-return-date state flight-type departure-date)]
    {::type flight-type
     ::departure-date departure-date
     ::return-date return-date
     ::button {:disabled? (or (:invalid? departure-date)
                              (:invalid? return-date)
                              (and (= :roundtrip flight-type)
                                   (before? (:value return-date)
                                            (:value departure-date))))}}))

(defn render-form [form-state]
  [:form.flex.flex-col.max-w-48.gap-4
   [:label "Choose type!"]
   [:select.select
    {:on
     {:input
      [[:action/assoc-in [::type] :event.target/value-as-keyword]]}}
    [:option {:value "one-way"} "One-way"]
    [:option {:value "roundtrip"} "Roundtrip"]]
   [:div.m-9]
   [:input.input
    (cond-> {:type "text"
             :name "departure-date"
             :value (:value (::departure-date form-state))
             :disabled (:disabled? (::departure-date form-state))
             :on {:input
                  [[:action/assoc-in [::departure-date] :event.target/value]]}}
      (-> form-state ::departure-date :invalid?)
      (assoc :class :input-error))]
   [:input.input {:type "text"
                  :name "return-date"
                  :value (:value (::return-date form-state))
                  :disabled (:disabled? (::return-date form-state))}]
   [:button.btn
    {:disabled (:disabled? (::button form-state))}
    "Book"]])

(defn render-ui [state]
  [:div [:h1.text-lg.mb-4 "Flight booking"]
   (render-form (get-form-state state))])
