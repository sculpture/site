(ns sculpture.specs.types
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.flexdate :as flexdate]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::flexdate-type
  (s/and
    string?
    #(re-matches flexdate/flexdate-regex %)))

(s/def ::email-type (s/and
                      string?
                      #(re-matches email-regex %)))

(s/def ::uuid-type uuid?)

(s/def ::related-ids-type (s/coll-of ::uuid-type
                                     :distinct true
                                     :kind vector?))

(s/def ::url-type (s/and
                    string?
                    ; TODO better regex
                    #(re-matches #"^http.*" %)))


(s/def ::longitude (s/and
                     number?
                     ; TODO bound min/max
                     ))
(s/def ::latitude (s/and
                    number?
                    ; TODO bound min/max
                    ))
(s/def ::precision number?)
(s/def ::location-type
  (s/keys :req-un [::longitude
                   ::latitude]
          :opt-un [::precision]))

(s/def ::timestamp-type inst?)

(s/def ::slug-type
  (s/and
    string?
    ; lowercase
    ; no spaces
    ))
