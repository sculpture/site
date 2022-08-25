(ns sculpture.admin.state.spec
  (:require
    [sculpture.schema.types :as types]
    [sculpture.schema.schema :as schema]))

(def AppState
  [:map
   [:db/search
    [:map
     [:query [:maybe string?]]
     [:results [:maybe [:sequential [:map
                                     [:id uuid?]
                                     [:title types/NonBlankString]
                                     [:subtitle [:maybe types/NonBlankString]]
                                     [:photo-id [:maybe uuid?]]
                                     [:type schema/EntityType]]]]]
     [:focused? boolean?]]]
   [:active-entity-id [:maybe uuid?]]
   [:db/user [:maybe
              [:map
               [:email types/Email]
               [:avatar types/Url]
               [:name types/NonBlankString]]]]
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
