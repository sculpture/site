(ns sculpture.specs.types
  (:require
    [clojure.spec :as s]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::email-type (s/and
                      string?
                      #(re-matches email-regex %)))

(def uuid-regex #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

(s/def ::uuid-type (s/and
                     string?
                     #(re-matches uuid-regex %)))

(s/def ::related-ids-type (s/coll-of ::uuid-type
                                     :distinct true
                                     :kind vector?))

(s/def ::year-type (s/and
                     integer?))

(s/def ::link-type (s/and
                     string?
                     ; TODO regex http(s)

                     ))


(s/def ::longitude (s/and
                     float?
                     ; TODO bound min/max
                     ))
(s/def ::latitude (s/and
                    float?
                    ; TODO bound min/max
                    ))
(s/def ::precision number?)
(s/def ::location-type
  (s/keys :req-un [::longitude
                   ::latitude
                   ::precision]))

(s/def ::timestamp-type inst?)

(s/def ::slug-type
  (s/and
    string?
    ; lowercase
    ; no spaces
    ))
