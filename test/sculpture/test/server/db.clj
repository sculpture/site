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

(deftest upsert!
  (testing "upsert!"
    (let [user (generate :user)]
      (db/init! user)

      (testing "requires a uuid user-id"
        (is (thrown? java.lang.AssertionError
                     (db/upsert! (generate :sculpture) "not-a-uuid"))))

      (testing "requires user-id to match an existing user"
        (is (thrown? java.lang.AssertionError
                     (db/upsert! (generate :sculpture) (:id (generate :user))))))

      (testing "requires doc to match a spec"
        (is (thrown? java.lang.AssertionError
                     (db/upsert! (generate :broken) (user :id)))))

      (testing "when valid and new"
        (let [doc (generate :sculpture)]
          (testing "returns true"
            (is (= true (db/upsert! doc (user :id)))))

          (testing "creates the doc"
            (is (= doc (db/get-by-id (doc :id)))))

          (testing "when doc already exists"
            (let [doc (assoc doc :title "Foobar")]

              (testing "returns true"
                (is (= true (db/upsert! doc (user :id)))))

              (testing "updates the doc")
              (is (= doc (db/get-by-id (doc :id)))))))))))

(deftest exists?
  (let [user (generate :user)]
    (db/init! user)

    (testing "exists?"

      (testing "returns false when not exists"
        (is (= false (db/exists? (uuid)))))

      (testing "returns true when exists"
        (let [doc (generate :sculpture)]
          (db/upsert! doc (user :id))
          (is (= true (db/exists? (doc :id)))))))))

(deftest delete!
  (let [user (generate :user)]
    (db/init! user)

    (testing "delete!"

      (testing "requires a uuid user-id"
        (let [doc (generate :sculpture)]
          (db/upsert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! doc "not-a-uuid")))))

      (testing "requires user-id to match an existing user"
        (let [doc (generate :sculpture)]
          (db/upsert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! doc (:id (generate :user)))))))

      (testing "does not allow delete of non-existant doc"
        (is (thrown? java.lang.AssertionError
                     (db/delete! (generate :sculpture) (user :id)))))

      (testing "does not allow delete of invalid doc"
        (let [doc (generate :sculpture)]
          (db/upsert! doc (user :id))
          (is (thrown? java.lang.AssertionError
                       (db/delete! {:id (doc :id)} (user :id))))))

      (testing "when valid"
        (let [doc (generate :sculpture)]
          (db/upsert! doc (user :id))

          (testing "returns true")
          (is (= true (db/delete! doc (user :id))))

          (testing "removes the doc"
            (is (= nil (db/get-by-id (doc :id))))))))))

(deftest clear!
  (let [user (generate :user)]
    (db/init! user)
    (db/upsert! (generate :sculpture) (user :id))

    (testing "clear!"
      (testing "returns true"
        (is (= true (db/clear!))))

      (testing "clears database"
        (is (= [] (db/all)))))))

(deftest get-by-id
  (let [user (generate :user)]
    (db/init! user)

    (testing "get-by-id"

      (testing "errors when not given an id"
        (is (thrown? java.lang.AssertionError
                     (db/get-by-id "123")))

        (testing "returns doc"
          (let [doc (generate :sculpture)]
            (db/upsert! doc (user :id))
            (is (= doc (db/get-by-id (doc :id))))))))))

(deftest all
  (let [user (generate :user)]
    (db/init! user)

    (testing "all"

      (testing "returns all records"
        (is (= [user] (db/all))))

      (testing "returns [] when empty"
        (db/clear!)
        (is (= [] (db/all)))))))
