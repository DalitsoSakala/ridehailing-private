(ns ride_hailing.routes.index
  (:require
   [ride-hailing.layout :as layout]
   [clojure.java.io :as io]
   [ride-hailing.middleware :as middleware]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ring.util.response]
   [ring.middleware.session]
   [ring.util.http-response :as response]
   [ride_hailing.models.vehicle :as vehicledb]))



(defn home-view [request]
  (layout/render request "home.html" {:user  (get-in request [:session :user]) :vehicle (get-in request [:session :vehicle])}))


(defn driver-view [request]
  (layout/render request "dashboard.html" {:user  (get-in request [:session :user]) :vehicle (get-in request [:session :vehicle])}))


(defn customer-view [request]
  (let [vehicles (vehicledb/get-available-vehicles)
        params (:params request)
        selected-vehicle  (:vehicle params)
        selected-vehicle  (if selected-vehicle (vehicledb/get-vehicle selected-vehicle) nil)
        selected-vehicle-driver  (if selected-vehicle (userdb/get-user (:driver selected-vehicle)) nil)]
    (layout/render request "order.html" {:user  (get-in request [:session :user])
                                         :vehicles vehicles
                                         :selected_vehicle selected-vehicle
                                         :selected_vehicle_driver selected-vehicle-driver})))


(defn add-vehicle [_]
  (ring.util.response/redirect "/dashboard"))

(defn update-vehicle-status [request]
  (let [id (:vehicle (:params request))
        availability (:availability (:params request))
        rate (:rate (:params request))]
    (vehicledb/update-vehicle-status id {:availability availability :rate rate})
    (ring.util.response/redirect "/dashboard")))




(defn index-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                ;;  ring.middleware.session/wrap-session
                 ]}

   ["/" {:get home-view}]

   ["/dashboard" {:get (middleware/with-auth (middleware/attach-vehicle driver-view))}]

   ["/order" {:get (middleware/with-auth customer-view)}]
   ["/add-vehicle" {:post (middleware/with-auth  (middleware/handle-vehicle-addition add-vehicle))}]
   ["/update-vehicle-availability" {:post (middleware/with-auth (middleware/attach-vehicle update-vehicle-status))}]
   ;
   ]
  ;
  )

