(ns sculpture.admin.state.spec
  (:require
    [sculpture.schema.types :as types]
    [sculpture.schema.schema :as schema]))

(def AppState
  [:map
   [:search
    [:map
     [:query [:maybe string?]]
     [:results [:maybe [:sequential schema/Entity]]]
     [:fuse any?]
     [:focused? boolean?]]]
   [:active-entity-id [:maybe uuid?]]
   [:user [:maybe
           [:map
            [:email types/Email]
            [:avatar types/Url]
            [:name types/NonBlankString]]]]
   [:page [:maybe
           [:map
            [:type keyword?]
            [:id {:optional true} uuid?]
            [:edit? {:optional true} boolean?]]]]
   [:main-page [:maybe keyword?]]
   [:data [:maybe
           [:map-of uuid? schema/Entity]]]
   [:saving? boolean?]
   [:entity-draft
    ;; validation of entity draft is done seperately
    [:maybe any?]]
   [:mega-map [:map
               [:dirty? boolean?]
               [:center {:optional true}
                [:map
                 [:longitude types/Longitude]
                 [:latitude types/Latitude]]]
               [:zoom-level {:optional true} int?]
               [:markers {:optional true}
                [:sequential
                 [:map
                  [:type keyword?]
                  [:bound? boolean?]
                  [:geojson types/GeoJson]
                  [:shapes {:optional true}
                   [:sequential any?]]]]]]]])
