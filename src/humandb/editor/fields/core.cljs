(ns humandb.editor.fields.core)

(defmulti field (fn [opts]
                  (opts :type)))
