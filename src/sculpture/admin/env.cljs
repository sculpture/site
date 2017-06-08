(ns sculpture.admin.env)

(defn env [k]
  (aget js/window "env" (name k)))


