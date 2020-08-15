(ns sculpture.admin.state.search
  (:require
    [cljsjs.fuse]))

(defn init [data]
  (js/Fuse. (->> data
                 (map (fn [e] (update e :id str)) data)
                 clj->js)
            (clj->js {:keys [:title :name :slug]
                      :shouldSort true
                      :minMatchCharLength 3
                      :threshold 0.3
                      :id :id})))

(defn search [fuse query limit]
  (if (= 0 (count query))
    []
    (->> (.slice (.search fuse query) 0 limit)
         js->clj
         (map (fn [id] (UUID. id nil))))))
