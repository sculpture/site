(ns sculpture.schema.types
  (:require
    [clojure.string :as string]
    [sculpture.flexdate :as flexdate]))

(def NonBlankString
  [:and
   [:string {:min 1}]
   [:fn {:error/message "Is blank"}
    (fn [s]
      (not (string/blank? s)))]])

(def Slug
  [:and
   NonBlankString
   [:re #"^[0-9a-z-]+$"]])

(def FlexDate
  [:and
   NonBlankString
   [:re flexdate/flexdate-regex]])

(def Url
  [:and
   NonBlankString
   [:re #"^https?.+$"]])

(def Email
  [:and
   NonBlankString
   [:re #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"]])

(def Longitude
  [:and number?
   [:fn (fn [x]
          (<= -180 x 180))]])

(def Latitude
  [:and number?
   [:fn (fn [x]
          (<= -90 x 90))]])

(def Location
  [:map
   [:longitude Longitude]
   [:latitude Latitude]
   [:precision {:optional true}
    number?]])

(def RelatedIds
  ;; TODO distinct
  [:vector uuid?])

(def GeoJson
  ;; TODO
  any?)

(def Color
  [:and
   NonBlankString
   [:re #"^#[0-9A-F]{6}$"]])
