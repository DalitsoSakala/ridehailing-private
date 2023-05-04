(ns ride-hailing.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [ride-hailing.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[ride-hailing started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[ride-hailing has shut down successfully]=-"))
   :middleware wrap-dev})
