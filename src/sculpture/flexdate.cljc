(ns sculpture.flexdate
  #?(:clj
     (:import
       (java.time Year YearMonth LocalDate))))

;; 2020-02-01  date
;; 2020-02     year-month
;; 2020        year
;; 202*        decade

(def flexdate-regex #"\d+\*?(-\d{2})?(-\d{2})?")
(def year-month-day-regex #"(\d+)-(\d{2})-(\d{2})")
(def year-month-regex #"(\d+)-(\d{2})")
(def year-regex #"(\d+)")
(def decade-regex #"(\d+)\*")

(defn ->int [s]
  #?(:cljs (js/parseInt s 10)
     :clj (Integer. s)))

(defn parse [s]
  (when (string? s)
    (or
      (when-let [m (re-matches year-month-day-regex s)]
        {:flexdate/precision :day
         :flexdate/year (->int (m 1))
         :flexdate/month (->int (m 2))
         :flexdate/day (->int (m 3))
         :flexdate/string s})
      (when-let [m (re-matches year-month-regex s)]
        {:flexdate/precision :month
         :flexdate/year (->int (m 1))
         :flexdate/month (->int (m 2))
         :flexdate/string s})
      (when-let [m (re-matches year-regex s)]
        {:flexdate/precision :year
         :flexdate/year (->int (m 1))
         :flexdate/string s})
      (when-let [m (re-matches decade-regex s)]
        {:flexdate/precision :decade
         :flexdate/decade (* 10 (->int (m 1)))
         :flexdate/string s}))))

(defn valid? [s]
  (boolean (parse s)))

(comment
  (parse "202*")
  (parse "2020")
  (parse "2020-01")
  (parse "2020-01-02")
  ;; not valid:
  (parse "2020-01-02x"))

#?(:clj
   (defn to-java-time [s]
     (when-let [result (parse s)]
       (case (result :precision)
         :day
         (LocalDate/parse (result :flexdate/string))
         :month
         (YearMonth/parse (result :flexdate/string))
         :year
         (Year/parse (result :flexdate/string))
         :decade
         (Year/parse (str (result :flexdate/decade)))))))

(defn to-string [])



