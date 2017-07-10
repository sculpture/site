(ns sculpture.admin.state.spec
  (:require
    [clojure.spec.alpha :as s]
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


(s/def ::active-entity-id (s/nilable ::entity-id))


(s/def ::page (s/nilable
                (s/keys :req-un [::type]
                        :opt-un [::id
                                 ::edit?])))

(s/def ::data (s/nilable
                ; use s/map-of to check all
                (s/every-kv ::entity-id ::entity)))



; -- ::search

(s/def ::query (s/nilable string?))
(s/def ::results (s/nilable (s/coll-of ::entity)))
(s/def ::fuse any?)
(s/def ::focused? ::boolean)

(s/def ::search
  (s/keys :req-un [::query
                   ::results
                   ::fuse
                   ::focused?]))


; -- ::user

(s/def ::email (s/nilable string?))
(s/def ::name (s/nilable string?))
(s/def ::avatar (s/nilable string?))

(s/def ::user
  (s/nilable (s/keys :req-un [::email
                              ::avatar
                              ::name])))

; -- ::entity-draft

(s/def ::entity-draft
  (s/nilable map?))

(s/def ::saving?  ::boolean)

; -- ::marker

(s/def ::bound? ::boolean)
(s/def ::geojson any?)
(s/def ::shapes seq?)

(s/def ::marker
  (s/keys :req-un [::type
                   ::bound?
                   ::geojson
                   ::shapes]))

; -- ::mega-map

(s/def ::dirty? ::boolean)
(s/def ::center
  (s/keys :req-un [:sculpture.specs.types/longitude
                  :sculpture.specs.types/latitude]))
(s/def ::zoom-level int?)
(s/def ::current-marker ::marker)

(s/def ::mega-map
  (s/keys :req-un [::dirty?]
          :opt-un [::center
                   ::zoom-level
                   ::current-marker]))

; -- ::main-page

(s/def ::main-page
  (s/nilable #{:edit :actions :upload :advanced-search}))

; -- ::app-state

(s/def ::app-state
  (s/keys :req-un [::search
                   ::active-entity-id
                   ::user
                   ::page
                   ::main-page
                   ::data
                   ::saving?
                   ::entity-draft
                   ::mega-map]))

; -- helpers

(defn check-state! [db]
  (when-let [ed (s/explain-data ::app-state db)]
    ed))

(defn validate [entity]
  (:cljs.spec.alpha/problems (s/explain-data ::entity entity)))
