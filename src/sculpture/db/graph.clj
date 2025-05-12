(ns sculpture.db.graph
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [sculpture.schema.schema :as schema]
   [sculpture.db.datascript :as db.ds]
   [sculpture.db.graph-pg-region :as pg-region]))

(defn blanks-for [ks]
  (zipmap ks (repeat nil)))

(defn make-datascript-resolvers
  []
  (->> schema/entities
       (mapcat (fn [e]
                 (:entity/id e)
                 ;;
                 (concat
                  [
                   ;; widgets - _ => [{widget/*}, ...]
                   (let [attrs (schema/direct-attributes (:entity/id e))]
                     (pco/resolver
                      (symbol (:entity/id-plural e))
                      {::pco/input []
                       ::pco/output [{(keyword (:entity/id-plural e))
                                      attrs}]}
                      (fn [env _in]
                        (let [additional-where (:where (pco/params env))]
                          {(keyword (:entity/id-plural e))
                           ;; TODO could just pull the attributes that are asked for
                           (let [blank (blanks-for attrs)]
                             (->> (db.ds/q (concat [:find [(list 'pull '?e attrs) '...]
                                                    :where
                                                    ['?e (:entity/id-key e) '_]]
                                                   additional-where))
                                  (map (fn [e]
                                         (merge blank e)))))}))))]

                  ;; widget-by-(unique-key) => {widget/*}
                  (->> (:entity/spec e)
                       (keep (fn [[attr-key opts]]
                               (when (:schema.attr/unique opts)
                                 (let [attrs (schema/direct-attributes (:entity/id e))]
                                   (pco/resolver
                                    (symbol (str (:entity/id e) "-by-" attr-key))
                                    {::pco/input [attr-key]
                                     ::pco/output attrs}
                                    (fn [_ in]
                                      ;; TODO could just pull the attributes that are asked for
                                      (merge
                                       (blanks-for attrs)
                                       (db.ds/q [:find (list 'pull '?e attrs) '.
                                                 :in '$ '?value
                                                 :where
                                                 ['?e attr-key '?value]]
                                                (attr-key in))))))))))
                  ;; widget-sprocket - widget-id => [sprocket/id, ...]
                  ;; sprocket-widgets - sprocket-id => [widget/id, ...]
                  (->> (:entity/spec e)
                       (keep (fn [[attr-key opts]]
                               (when-let [[cardinality entity-type] (:schema.attr/relation opts)]
                                 (let [other-e (schema/by-id entity-type)]
                                   [;; normal direction
                                    (let [in-key (:entity/id-key e)
                                          out-key (keyword
                                                   (:entity/id e)
                                                   (case cardinality
                                                     :one (:entity/id other-e)
                                                     :many (:entity/id-plural other-e)))
                                          out-ids-key attr-key
                                          count-key (keyword
                                                     (:entity/id e)
                                                     (str (:entity/id other-e) "-count"))]
                                      (case cardinality
                                        :one
                                        (pco/resolver
                                         (symbol (str (:entity/id e)
                                                      "-"
                                                      (:entity/id other-e)))
                                         {::pco/input [in-key]
                                          ::pco/output [{out-key
                                                         [(:entity/id-key other-e)]}
                                                        out-ids-key]}
                                         (fn [_ in]
                                           (let [id (db.ds/q [:find '?other-id '.
                                                              :in '$ '?id
                                                              :where
                                                              ['?e (:entity/id-key e) '?id]
                                                              ['?e attr-key '?other-e]
                                                              ['?other-e (:entity/id-key other-e) '?other-id]]
                                                              (in-key in))]
                                             {out-ids-key id
                                              out-key {(:entity/id-key other-e) id}})))

                                        :many
                                        (pco/resolver
                                         (symbol (str (:entity/id e)
                                                      "-"
                                                      (:entity/id-plural other-e)))
                                         {::pco/input [in-key]
                                          ::pco/output [{out-key
                                                         [(:entity/id-key other-e)]}
                                                        out-ids-key
                                                        count-key]}
                                         (fn [_ in]
                                           (let [ids (db.ds/q [:find ['?other-id '...]
                                                               :in '$ '?id
                                                               :where
                                                               ['?e (:entity/id-key e) '?id]
                                                               ['?e attr-key '?other-e]
                                                               ['?other-e (:entity/id-key other-e) '?other-id]]
                                                              (in-key in))]
                                             {out-ids-key ids
                                              out-key (mapv (fn [x]
                                                              {(:entity/id-key other-e) x})
                                                            ids)
                                              count-key (count ids)})))))

                                    ;; reverse direction
                                    (let [in-key (:entity/id-key other-e)
                                          out-key (keyword
                                                   (:entity/id other-e)
                                                   (:entity/id-plural e))
                                          count-key (keyword
                                                     (:entity/id other-e)
                                                     (str (:entity/id e) "-count"))]
                                      (pco/resolver
                                       (symbol (str (:entity/id other-e)
                                                    "-"
                                                    ;; always many on the reverse
                                                    (:entity/id-plural e)))
                                       {::pco/input [in-key]
                                        ::pco/output [{out-key
                                                       [(:entity/id-key e)]}
                                                      count-key]}
                                       (fn [_ in]
                                         (let [ids (db.ds/q [:find ['?id '...]
                                                             :in '$ '?other-id
                                                             :where
                                                             ['?other-e (:entity/id-key other-e) '?other-id]
                                                             ['?e attr-key '?other-e]
                                                             ['?e (:entity/id-key e) '?id]]
                                                            (in-key in))]
                                           {out-key (mapv (fn [x]
                                                            {(:entity/id-key e) x})
                                                          ids)
                                            count-key (count ids)}))))]))))
                       (apply concat)))))
       (remove nil?)))

(defn pathom
  []
  (let [resolvers (concat (make-datascript-resolvers)
                          pg-region/resolvers)
        indexes (pci/register resolvers)]
    (fn [i o]
      (p.eql/process indexes i o))))

#_((pathom) {} [{:sculptures [:sculpture/title]}])
#_((pathom) {} [{:materials [:material/name]}])

(def query
  (let [p (pathom)]
    (fn [query-id-or-identifier pattern]
      (cond
        (map? query-id-or-identifier)
        (p query-id-or-identifier pattern)

        (keyword? query-id-or-identifier)
        (query-id-or-identifier (p {} [{query-id-or-identifier pattern}]))

        (nil? pattern)
        (p {} query-id-or-identifier)))))

(comment

  ;; artists
  (contains? (-> ((pathom) {} [{:artists [:artist/name]}])
                 :artists
                 set)
             {:artist/name "Kosso Eloul"})

  ;; artists - with addition :where conditions

  ((pathom) {} [{'(:sculptures {:where [[?e :sculpture/date ?d]
                                        [(< "1910" ?d "1920")]]}) [:sculpture/date]}])

  ;; artist-by-id
  (= {:artist/name "Kosso Eloul"}
     ((pathom) {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
               [:artist/name]))


  #_((pathom) {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
              (schema/direct-attributes "artist"))

  ;; artist-by-slug
  (= {:artist/name "Kosso Eloul"}
     ((pathom) {:artist/slug "kosso-eloul"}
               [:artist/name]))

  ;; user-by-email

  #_((pathom) {:user/email "rafal.dittwald@gmail.com"}
              [:user/avatar])

  ;; artist-nationalities  - one-to-many

  (= {:artist/nationalities [{:nationality/slug "finnish"}]}
     ((pathom) {:artist/id #uuid "6c415c22-af7b-4d96-958b-2d657fd519a7"}
               [{:artist/nationalities [:nationality/slug]}]))

  (= {:artist/nationality-count 1}
     ((pathom) {:artist/id #uuid "6c415c22-af7b-4d96-958b-2d657fd519a7"}
               [:artist/nationality-count]))

  ;; nationality-artists - one-to-many (reverse relation)

  (contains?
   (-> ((pathom) {:nationality/id #uuid "8d2d4920-faf1-4154-be6a-078862e4336c"}
                 [{:nationality/artists [:artist/name]}])
       :nationality/artists
       set)
   {:artist/name "Eino"})

  (= {:artist/sculpture-count 25}
     ((pathom) {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
               [:artist/sculpture-count]))


  ;; photo - user
  (= {:photo/user [{:user/email "wdittwald@gmail.com"}]}
     ((pathom) {:photo/id #uuid "39675b19-f252-4154-9033-82612883c0d2"}
               [{:photo/user [:user/email]}]))

  ;; user - photos
  ((pathom) {:user/id #uuid "1b5391b8-c7e7-493d-809c-cc8d8491d4f6"}
            [{:user/photos [:photo/id]}])


  ;; sculpture - regions
  #_((pathom) {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
              [{:artist/sculptures [:sculpture/title
                                    {:sculpture/regions [:region/name]}]}])


  (= {:artist/name "Kosso Eloul"}
     ((pathom) {:user/id #uuid "013ec717-531b-4b30-bacf-8a07f33b0d43"}
               [:user/email]))
  )

;;; OLD ----

;; smart-map-interface
#_(:sculpture/date (com.wsscode.pathom3.interface.smart-map/smart-map
                    indexes
                    {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"}))

;; single eql query with identifier
#_(peql/process indexes
                [{[:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"]
                  [:sculpture/date]}])

;; two part eql identifier and query
#_(peql/process indexes
                {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"}
                [:sculpture/date])

#_(query :segments [:segment/name])

#_(query {:sculpture/id #uuid "0ef9c6f1-a415-45b2-9afd-925c00ff7955"}
         [:sculpture/location])

#_(query {:region/id #uuid "34338123-76c8-4e73-ac74-1855dd3f87ce"}
         [:region/shape])

#_(query {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
         [:artist/artist-tag-ids])

#_(query '[{(:sculptures {:decade 1960}) [:sculpture/id]}] nil)

#_(do
    (require '[com.wsscode.pathom.viz.ws-connector.core :as pvc])
    (require [com.wsscode.pathom.viz.ws-connector.pathom3 :as p.connector])

    (let [env (p.connector/connect-env
               (pci/register indexes)
               {:com.wsscode.pathom.viz.ws-connector.core/parser-id :sculpture})]
      (peql/process env
                    {:photo/id #uuid "153b622b-3c43-4474-987b-6997913684df"}
                    [{:photo/user [{:user/photos [:photo/colors]}]}])))
