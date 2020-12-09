(ns sculpture.admin.views.pages.entity-editor
  (:require
    [clojure.string :as string]
    [humandb.ui.field :refer [field]]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.cdn :as cdn]
    [sculpture.admin.routes :as routes]
    [sculpture.schema.schema :as schema]))

(defn key->title [k]
  (-> k
      name
      (string/split #"-")
      (->> (map string/capitalize)
           (string/join " "))
      (string/replace #"Id" "ID")))

(defn field-opts [field type]
  (let [opts (get-in schema/schema [type field :input] {})]
    (if (fn? opts)
      (opts)
      opts)))

(defn entity-editor-view [entity]
  (when entity
    (let [invalid-fields @(subscribe [:sculpture.edit/invalid-fields])
          default-entity (schema/->default-entity (:type entity))]
      [:div.entity.edit
       [:div.header
        [:h1 "Editing " (or (entity :name)
                            (entity :title)
                            (entity :slug)
                            (entity :id))]
        [:button.close
         {:on-click (fn [_]
                      (dispatch! [:sculpture.edit/stop-editing]))} "Close"]

        (let [saving? @(subscribe [:sculpture.edit/saving?])
              invalid? (not (empty? invalid-fields))]
          [:button.save {:disabled (or saving? invalid?)
                         :class (when invalid? "invalid")
                         :on-click
                         (fn [_]
                           (dispatch! [:sculpture.edit/save]))}
           (cond
             invalid?
             "Invalid"
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
                        (field-opts k (entity :type))
                        {:value v
                         :on-change (fn [v]
                                      (dispatch! [:sculpture.edit/update-draft k v]))})]]
              [:td [:button.delete
                    {:on-click (fn []
                                 (dispatch! [:sculpture.edit/remove-draft-key k]))}]]]))

         (when (= "photo" (:type entity))
           [:tr
            [:td]
            [:td
             [:img {:src (cdn/image-url entity :thumb)}]]
            [:td]])]]])))
