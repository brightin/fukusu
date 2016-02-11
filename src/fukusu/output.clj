(ns fukusu.output
  (:require [fukusu.core :as core]
            [clojure.string :as string]))

(defn- print-results [formatter response]
  (doall
   (for [[name r] response]
     (println (format "%-40s %s" name (formatter r))))))

(defn list-apps
  "List all apps"
  [app-regex _]
  (doall
   (map println (core/get-app-names app-regex)))
  (System/exit 0))

(defn list-ruby
  "List Ruby versions for apps"
  [app-regex _]
  (let [app-names (core/get-app-names app-regex)
        command "ruby -v"
        split-by-space #(string/split % #"\s")
        formatter (comp second split-by-space last)]
    (print-results formatter (core/get-response command app-names))
    (System/exit 0)))

(defn list-gem
  "List gem versions for apps"
  [app-regex [gem-name]]
  (let [app-names (core/get-app-names app-regex)
        command (str "bundle show " gem-name)
        split-by-dash #(string/split % #"-")
        formatter (comp second split-by-dash last)]
    (print-results formatter (core/get-response command app-names))
    (System/exit 0)))

(def commands
  {"list:apps" #'list-apps
   "list:ruby" #'list-ruby
   "list:gem" #'list-gem})
