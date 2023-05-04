(ns ride_hailing.routes.index
  (:require
   [ride-hailing.layout :as layout]
   [clojure.java.io :as io]
   [ride-hailing.middleware :as middleware]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ring.util.response]
   [ring.middleware.session]
   [ring.util.http-response :as response]))



(defn home-view [request]
  (layout/render request "home.html" {:user  (get-in request [:session :user] )}))





(defn index-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                ;;  ring.middleware.session/wrap-session
                 ]}

   ["/" {:get home-view}]
   ]
  )

