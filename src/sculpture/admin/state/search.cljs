(ns sculpture.admin.state.search
  (:require
    [cljsjs.fuse]))

(defn init [data]
  (js/Fuse. (clj->js data)
            (clj->js {:keys [:title :name]
                      :shouldSort true
                      :minMatchCharLength 3
                      :threshold 0.3
                      :id :id})))

(defn search [fuse query limit]
  (if (= 0 (count query))
    []
    (->> (.slice (.search fuse query) 0 limit)
         js->clj)))
