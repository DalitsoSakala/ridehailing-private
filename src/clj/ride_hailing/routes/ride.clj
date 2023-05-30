(ns ride_hailing.routes.ride
  (:require
   [ride-hailing.layout :as layout]
   [clojure.java.io :as io]
   [ride-hailing.middleware :as middleware]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ring.util.response]
   [ring.middleware.session]
   [ring.util.http-response :as response]))



(defn picker-view [request]
  (layout/render request "ride/picker.html" {:user  (get-in request [:session :user])
  
  :accepted_order  (get-in request [:session :accepted-order])}))





(defn ride-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 ]}

   ["/ride" {:get  (middleware/with-auth picker-view)}]])

