(ns sculpture.db.git
  (:require
   [sculpture.db.plain :as db.plain]
   [sculpture.config :refer [config]]
   [sculpture.schema.util :as schema.util]
   [clj-jgit.porcelain :as git]))

(def repo (delay (git/load-repo (:data-dir config))))

(defn upsert-entity!
  [entity author]
  (git/git-add @repo ".")
  (git/git-commit @repo
    (str (if (db.plain/exists? (:data-dir config) entity)
           "Update"
           "Add") " "
         (schema.util/entity-type entity) " "
         (or (schema.util/entity-slug entity)
             (schema.util/entity-id entity)))
    :author
    {:name (:user/name author)
     :email (:user/email author)}
    :committer
    {:name (:github-committer-name config)
     :email (:github-committer-email config)})
    (git/with-identity {:name "sculpturebot"
                        :key-dir "./ssh_key/"
                        :trust-all? true}
      (git/git-push @repo)))


