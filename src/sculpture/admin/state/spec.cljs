(ns sculpture.admin.state.spec
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity]
    [sculpture.specs.types]))

; -- types

(s/def ::uuid :sculpture.specs.types/uuid-type)
(s/def ::entity-id ::uuid)
(s/def ::entity :sculpture.specs.entity/entity)
(s/def ::boolean #(or (true? %) (false? %)))

; -- keys in ::app-state ::page

(s/def ::type keyword?)
(s/def ::id ::uuid)
(s/def ::edit? ::boolean)

; -- keys in ::app-state

(s/def ::query (s/nilable string?))

(s/def ::active-entity-id (s/nilable ::entity-id))

(s/def ::results (s/nilable (s/coll-of ::entity)))

(s/def ::page (s/nilable
                (s/keys :req-un [::type]
                        :opt-un [::id
                                 ::edit?])))

(s/def ::data (s/nilable
                ; use s/map-of to check all
                (s/every-kv ::entity-id ::entity)))

(s/def ::fuse any?)

; -- ::app-state

(s/def ::app-state
  (s/keys :req-un [::query
                   ::active-entity-id
                   ::results
                   ::page
                   ::data
                   ::fuse]))

; -- helpers

(defn check-state! [db]
  (when-let [ed (s/explain-data ::app-state db)]
    (with-out-str (s/explain-out ed))))
