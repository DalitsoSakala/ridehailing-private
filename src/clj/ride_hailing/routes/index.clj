(ns ride_hailing.routes.index
  (:require
   [ride-hailing.layout :as layout]
   [clojure.java.io :as io]
   [ride-hailing.middleware :refer [with-auth handle-vehicle-addition]]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ring.util.response]
   [ring.middleware.session]
   [ring.util.http-response :as response]))



(defn home-view [request]
  (layout/render request "home.html" {:user  (get-in request [:session :user]) :vehicle (get-in request [:session :vehicle])}))


(defn driver-view [request]
  (layout/render request "dashboard.html" {:user  (get-in request [:session :user]) :vehicle (get-in request [:session :vehicle])}))


(defn customer-view [request]
  (layout/render request "order.html" {:user  (get-in request [:session :user])}))


(defn add-vehicle [_]
  (ring.util.response/redirect "/"))




(defn index-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                ;;  ring.middleware.session/wrap-session
                 ]}

   ["/" {:get home-view}]

   ["/dashboard" {:get (with-auth driver-view)}]

   ["/order" {:get (with-auth customer-view)}]
   ["/add-vehicle" {:post (with-auth (handle-vehicle-addition add-vehicle))}]
   ;
   ]
  ;
  )

