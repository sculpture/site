(ns humandb.editor.field
  (:require
    [reagent.core :as r]
    [humandb.editor.fields.core :as fields]
    [humandb.editor.fields.misc]
    [humandb.editor.fields.location]
    [humandb.editor.fields.geojson]))

(defn debounce
  [f ms]
  (let [timeout (atom nil)]
    (fn [& args]
      (js/clearTimeout @timeout)
      (reset! timeout (js/setTimeout (fn []
                                       (apply f args))
                                     ms)))))

(defn field [opts]
  (let [temp-value (r/atom nil)
        on-change (r/atom nil)
        debounced-fn (debounce (fn [value]
                                 (@on-change value))
                               200)]
    (fn [opts]
      (fields/field (-> opts
                        (update :on-change
                                (fn [original-on-change]
                                  (fn [value]
                                    (reset! temp-value value)
                                    (reset! on-change original-on-change)
                                    (debounced-fn value))))
                        (assoc :value (or @temp-value (opts :value))))))))


