(ns sculpture.test.server.db
  (:require
    [clojure.test :refer :all]
    [sculpture.test.fixtures.github :refer [github-api-mock]]
    [sculpture.server.db :as db]))

(defn db-reset [t]
  (db/clear!)
  (t)
  (db/clear!))

(use-fixtures :each db-reset github-api-mock)

(defn uuid []
  (java.util.UUID/randomUUID))

(defn generate [entity-type]
  (case entity-type
    :user
    {:id (uuid)
     :type "user"
     :name "John Smith"
     :email "john.smith@example.com"}
    :sculpture
    {:id (uuid)
     :type "sculpture"
     :title "Cube"
     :slug "cube"}
    :broken
    {:id (uuid)}))

(deftest init!
  (testing "init!"
    (let [user (generate :user)]
      (testing "requires user"
        (is (thrown? java.lang.AssertionError
                     (db/init! {}))))

      (testing "returns true"
        (is (= true (db/init! user))))

      (testing "initializes db w/ user"
        (is (= [user] (db/all)))))))

(deftest insert!
  (testing "insert!"
    (let [user (generate :user)]
      (db/init! user)

      (testing "requires a uuid user-id"
        (is (thrown? java.lang.AssertionError
                     (db/insert! (generate :sculpture) "not-a-uuid"))))

      (testing "requires user-id to match an existing user"
        (is (thrown? java.lang.AssertionError
                     (db/insert! (generate :sculpture) (:id (generate :user))))))

      (testing "does not allow insert of doc with duplicate (existing) id"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/insert! doc (user :id))))))

      (testing "requires doc to match a spec"
        (is (thrown? java.lang.AssertionError
                     (db/insert! (generate :broken) (user :id)))))

      (testing "when valid"
        (let [doc (generate :sculpture)]
          (testing "returns true"
            (is (= true (db/insert! doc (user :id)))))

          (testing "creates the doc"
            (is (= doc (db/select (doc :id))))))))))

(deftest exists?
  (let [user (generate :user)]
    (db/init! user)

    (testing "exists?"

      (testing "returns false when not exists"
        (is (= false (db/exists? (uuid)))))

      (testing "returns true when exists"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (= true (db/exists? (doc :id)))))))))

(deftest update!
  (let [user (generate :user)]
    (db/init! user)

    (testing "update!"

      (testing "requires a uuid user-id"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/update! doc "not-a-uuid")))))

      (testing "requires user-id to match an existing user"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/update! doc (:id (generate :user)))))))

      (testing "does not allow update of non-existant doc"
        (let [doc (generate :sculpture)]
          (is (thrown? java.lang.AssertionError
                       (db/update! doc (user :id))))))

      (testing "does not allow update with doc not matching any spec"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/update! (generate :broken) (user :id))))))

      (testing "when used correctly"
        (let [doc (generate :sculpture)
              updated-doc (assoc doc :slug "newslug")]
          (db/insert! doc (user :id))

          (testing "returns true"
            (is (= true (db/update! updated-doc (user :id)))))

          (testing "updates the doc"
            (is (= updated-doc (db/select (updated-doc :id))))))))))

(deftest delete!
  (let [user (generate :user)]
    (db/init! user)

    (testing "delete!"

      (testing "requires a uuid user-id"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! doc "not-a-uuid")))))

      (testing "requires user-id to match an existing user"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! doc (:id (generate :user)))))))

      (testing "does not allow delete of non-existant doc"
        (is (thrown? java.lang.AssertionError
                     (db/delete! (generate :sculpture) (user :id)))))

      (testing "does not allow delete of invalid doc"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! {:id (doc :id)} (user :id))))))

      (testing "when valid"
        (let [doc (generate :sculpture)]
          (db/insert! doc (user :id))

          (testing "returns true")
          (is (= true (db/delete! doc (user :id))))

          (testing "removes the doc"
            (is (= nil (db/select (doc :id))))))))))

(deftest clear!
  (let [user (generate :user)]
    (db/init! user)
    (db/insert! (generate :sculpture) (user :id))

    (testing "clear!"
      (testing "returns true"
        (is (= true (db/clear!))))

      (testing "clears database"
        (is (= [] (db/all)))))))

(deftest select
  (let [user (generate :user)]
    (db/init! user)

    (testing "select"

      (testing "errors when not given an id"
        (is (thrown? java.lang.AssertionError
                     (db/select "123")))

        (testing "returns doc"
          (let [doc (generate :sculpture)]
            (db/insert! doc (user :id))
            (is (= doc (db/select (doc :id))))))))))

(deftest all
  (let [user (generate :user)]
    (db/init! user)

    (testing "all"

      (testing "returns all records"
        (is (= [user] (db/all))))

      (testing "returns [] when empty"
        (db/clear!)
        (is (= [] (db/all)))))))
