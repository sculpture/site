(ns sculpture.admin.styles.entity-box)

(defn entity-box-styles []
  (let [size "85px"]
    [:&
     {:display "inline-block"
      :border "1px solid #ededed"
      :width size
      :padding "0.5em"
      :margin [[0 "0.5em" "0.5em" 0]]
      :text-align "center"
      :text-decoration "none"}

     [:.photo
      {:width size
       :height size
       :display "flex"
       :align-items "center"
       :justify-content "center"
       :margin-bottom "0.5em"}

      [:img
       {:max-width size
        :max-height size
        :vertical-align "center"
        :text-align "center"}]]

     [:.title
      {:font-weight "bold"
       :color "#000"}]

     [:.year
      {:color "#AAA"}]

     [:&:hover
      {:border-color "#ccc"}]

     [:&:active
      {:border-color "#aaa"}]]))
