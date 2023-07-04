(ns sculpture.admin.views.pages.entity-editor
  (:require
    [clojure.string :as string]
    [humandb.ui.field :refer [field]]
    [sculpture.admin.state.api :refer [subscribe dispatch!]]
    [sculpture.admin.cdn :as cdn]
    [sculpture.schema.schema :as schema]
    [sculpture.schema.util :as schema.util]))

(defn key->title [k]
  (-> k
      name
      (string/split #"-")
      (->> (map string/capitalize)
           (string/join " "))
      (string/replace #"Id" "ID")))

(defn field-opts [field type]
  (get-in schema/schema [type field :input] {}))

(defn entity-editor-view [entity]
  (when entity
    (let [invalid-fields @(subscribe [:state.edit/invalid-fields])
          entity-type (schema.util/entity-type entity)
          default-entity (schema.util/default-entity entity-type)]
      [:div.page.entity.edit
       [:div.header
        [:h1 "Editing " (schema.util/label entity)]
        [:button.close
         {:on-click (fn [_]
                      (dispatch! [:state.edit/stop-editing!]))} "Close"]

        (let [saving? @(subscribe [:state.edit/saving?])
              invalid? (not (empty? invalid-fields))]
          [:button.save {:disabled (or saving? invalid?)
                         :class (when invalid? "invalid")
                         :on-click
                         (fn [_]
                           (dispatch! [:state.edit/save!]))}
           (cond
             invalid?
             [:span {:title (str invalid-fields)} "Invalid"]
             saving?
             "Saving..."
             :else
             "Save")])]

       [:table
        [:tbody
         (for [k (keys default-entity)]
           (let [v (or (entity k)
                       (default-entity k))]
             ^{:key k}
             [:tr {:class (when (contains? invalid-fields k)
                            "invalid")}

              [:td.key (key->title k)]
              [:td
               [field (merge
                        (field-opts k entity-type)
                        {:value v
                         :entity entity
                         :on-change (fn [v]
                                      (dispatch! [:state.edit/update-draft! k v]))})]]
              [:td [:button.delete
                    {:on-click (fn []
                                 (dispatch! [:state.edit/remove-draft-key! k]))}]]]))

         (when (= "photo" entity-type)
           [:tr
            [:td]
            [:td
             [:img {:src (cdn/image-url entity :thumb)}]]
            [:td]])]]])))
