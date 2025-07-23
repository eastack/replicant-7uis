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
  (prn "flight-type: " (name (or (::type state) :one-way)))
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

(defn render-date-input [form-state k]
  [:input.input
   (cond-> {:type "text"
            :name (name k)
            :value (:value (k form-state))
            :disabled (:disabled? (k form-state))
            :on {:input
                 [[:action/assoc-in [k] :event.target/value]]}}
     (-> form-state k :invalid?)
     (assoc :class :input-error)

     (:disabled? (k form-state))
     (assoc :disabled "disabled"))])

(defn render-select-option [form-state option-name  k]
  [:option
   (cond-> {:value (name k)}
     (= (::type form-state) k)
     (assoc :selected true))
   option-name])

(defn render-form [form-state]
  [:form.flex.flex-col.max-w-48.gap-4
   [:select.select
    {:name "flight-type"
     :on
     {:input
      [[:action/assoc-in [::type] :event.target/value-as-keyword]]}}
    (render-select-option form-state "One-way" :one-way)
    (render-select-option form-state "Roundtrip" :roundtrip)]
   (render-date-input form-state ::departure-date)
   (render-date-input form-state ::return-date)
   [:button.btn
    {:disabled (:disabled? (::button form-state))
     :on {:click [[:action/assoc-in [::booked?] true]]}}
    "Book"]])

(defn render-receipt [form-state]
  [:div
   [:p.mb-4
    "You have booked a "
    (name (::type form-state))
    " flight on "
    (:value (::departure-date form-state))
    (when (= :roundtrip (::type form-state))
      (str ", returning on " (:value (::return-date form-state))))
    "."]
   [:button.btn
    {:on {:click [[:action/assoc-in [::booked?] false]]}}
    "Try again"]])

(defn render-ui [state]
  [:div [:h1.text-lg.mb-4 "Flight booking"]
   (if (::booked? state)
     (render-receipt (get-form-state state))
     (render-form (get-form-state state)))])
