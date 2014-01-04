(ns swag.model
  "Swagger model related functionality" 
  (:import clojure.lang.ExceptionInfo)
  (:use 
    clojure.pprint
    [swag.common :only (defstruct-)]
    [clojure.string :only (capitalize)])
  )

(def ^{:doc "User defined types"} models (atom {}))

(defn add-model 
  "Adds a model (used internaly)"
  [k m] (swap! models assoc k m))

(defstruct- model :id :properties)

(defn nest-types 
  "Taks type :foo and nests in {:type :foo}"
  [m]
  (reduce (fn [r [k v]] (assoc r k (if (keyword? v) {:type v} v))) {} m))

(defmacro defmodel
  "Defining a swagger model: 
  (defmodel module :name :string :src :string) " 
  [name & props]
  `(do 
     (def ~name (model- ~(-> name str capitalize) (nest-types (hash-map ~@props))))
     (add-model ~(keyword name) ~name)))


(def ^{:doc "applied conversion path -> fn"} conversions (atom {}))

(defmacro defc
  "Defines a model conversion v (value) passed implicitly."
  [uri path f]
   `(swap! conversions (fn [m#] (update-in m# [~uri] merge {~path (fn [~'v] ~f)}))))

(defn convert
  "Process a list of params running conversions"
  [uri params]
    (reduce 
      (fn [r [path c]]
        (if-let [v (get-in r path)] 
          (update-in r path c) r)) params (@conversions uri))
    )

(defn wrap-swag 
  "A ring middleware that utilizes swagger metadata on passed in requests, it runs validations and conversions according to ones defined using swag.model"
  [app]
  (fn [{:keys [params uri] :as req}]
    (if params 
      (try 
         (app (assoc req :params (convert uri params)))
        (catch ExceptionInfo e 
          (if (= (-> e (.data) :error) :validation)
           {:body  (.getMessage e) :status 400}
           {:body  (.getMessage e) :status 500})) 
        )
      (app req))))
