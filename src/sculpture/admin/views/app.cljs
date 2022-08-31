(ns sculpture.admin.views.app
  (:require
    [sculpture.admin.state.api :refer [subscribe dispatch! dispatch-sync!]]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.mega-map :refer [mega-map-view]]
    [sculpture.admin.views.sidebar :refer [sidebar-view]]
    [sculpture.admin.views.page :refer [page-view]]))

(defn new-entity-button-view []
  [:button.menu {:on-click (fn [_]
                             (dispatch! [:state.core/set-main-page! :main-page/actions]))}])

(defn toolbar-view []
  (if-let [user @(subscribe [:state.auth/user])]
    [:div.toolbar
     [new-entity-button-view]
     [:img.avatar {:src (user :avatar)}]]
    [:div.toolbar
     [:button.auth
      {:on-click (fn []
                   (dispatch-sync! [:state.auth/start-oauth!]))}]]))

(defn app-view []
  [:div.app
   [styles-view]
   [:div.main
    [sidebar-view]
    [mega-map-view]]
   [toolbar-view]
   [page-view]])
