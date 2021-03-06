(ns fukusu.commands
  (:require [fukusu.core :as core]
            [fukusu.util :as util]))

(defn print-response [formatter response]
  (doall
   (for [[name r] response]
     (println (format "%-40s %s" name (formatter r))))))

(defn list-apps
  "List all apps"
  [app-names _]
  (doall
   (map println app-names))
  (System/exit 0))

(defn list-ruby
  "List Ruby versions for apps"
  [app-names _]
  (let [command ["run" "ruby -v"]
        formatter (comp second util/split-by-space last)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))

(defn list-gem
  "List gem versions for apps"
  [app-names [gem-name]]
  (let [command ["run" (str "bundle show " gem-name)]
        formatter (comp second util/split-by-dash last)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))

(defn list-db
  "List the Heroku Postgres database plan"
  [app-names [_]]
  (let [command ["pg:info"]
        formatter (comp second (partial re-find #"Plan:\s+(.+)") str second)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))

(defn list-addons
  "List apps with specified addon"
  [app-names [addon-name]]
  (let [command ["addons"]
        find-addon #(filter (fn [line] (re-find (re-pattern addon-name) line)) %)
        formatter (comp first util/split-by-space str last find-addon)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))

(defn check-access
  "List app name with user if user has access"
  [app-names [user-name]]
  (let [command ["access"]
        find-user #(filter (fn [line] (re-find (re-pattern user-name) line)) %)
        formatter (comp first util/split-by-space str last find-user)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))


(defn remove-access
  "Remove access for specific EMAIL."
  [app-names [email]]
  (let [command ["access:remove" email]
        formatter (comp first util/split-by-space last)]
    (print-response formatter (core/get-response command app-names))
    (System/exit 0)))

(defn command-do
  "Run a specific heroku command for mulitple apps for example `fukusu do \"pg:backups:capture\"`"
  [app-names [command]]
  (print-response last (core/get-response [command] app-names))
  (System/exit 0))

(def all
  {"list:apps" #'list-apps
   "list:ruby" #'list-ruby
   "list:gem" #'list-gem
   "list:db" #'list-db
   "list:addons" #'list-addons
   "access:check" #'check-access
   "access:remove" #'remove-access
   "do" #'command-do})

(def usage
  (->>
   (for [[name fn] all]
     (format "  %-12s # %s" name (:doc (meta fn))))
   (apply util/long-str)))
