(ns ride_hailing.routes.index
  (:require
   [ride-hailing.layout :as layout]
   [clojure.java.io :as io]
   [ride-hailing.middleware :as middleware]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ride_hailing.models.rideorder :as orderdb]
   [ring.util.response]
   [ring.middleware.session]
   [ring.util.http-response :as response]
   [ride_hailing.models.vehicle :as vehicledb]))



(defn home-view [request]
  (layout/render request "home.html" {:user  (get-in request [:session :user]) :vehicle (get-in request [:session :vehicle])}))


(defn driver-view [request]
  (let [session (:session request)
        driver  (:id (:user session))
        accepted-order (orderdb/get-accepted-order-for-driver driver)]

    (layout/render request "dashboard.html" {:user  (:user session)
                                             :accepted_order accepted-order
                                             :vehicle (:vehicle session)
                                             :orders (:orders session)})))

(defn accept-order [request]
  (let [session (:session request)
        driver (:user session)
        vehicle-id (:id (:vehicle session))
        order-id (read-string (:order (:params request)))]
    (vehicledb/update-vehicle-status vehicle-id {:availability "unavailable"})
    (orderdb/update-rideorders-for-driver (:id driver) {:status "rejected"})
    (orderdb/update-rideorder-status order-id {:status "accepted"})
    (ring.util.response/redirect "/dashboard")))



(defn end-ride [request]
  (let [session (:session request)
        driver-id (:id (:user session)) 
        order-id (:id (orderdb/get-accepted-order-for-driver driver-id))]
    (orderdb/update-rideorder-status order-id {:status "completed"}))
  
  (ring.util.response/redirect "/dashboard"))


(defn customer-view [request]
  (let [vehicles (vehicledb/get-available-vehicles)
        params (:params request)
        session (:session request)
        selected-vehicle  (:vehicle params)
        selected-vehicle  (if selected-vehicle (vehicledb/get-vehicle selected-vehicle) nil)
        selected-vehicle-driver  (if selected-vehicle (userdb/get-user (:driver selected-vehicle)) nil)]
    (layout/render request "order.html" {:user  (:user session)
                                         :order  (:orders session)
                                         :vehicles vehicles
                                         :selected_vehicle selected-vehicle
                                         :selected_vehicle_driver selected-vehicle-driver})))


(defn make-order [request]
  (let [params (:params request)
        driver (read-string (:driver params))
        session (:session request)
        me (:id (:user session))
        existing-order (:orders session)]
    (if existing-order (orderdb/update-rideorder-status (:id existing-order) {:status "cancelled"})
        (orderdb/insert-rideorder {:driver driver :customer me})))
  (ring.util.response/redirect "/order"))

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

   ["/dashboard" {:get (middleware/driver-only (middleware/attach-vehicle (middleware/attach-order driver-view)))}]
   ["/accept-order" {:post (middleware/driver-only accept-order)}]
   ["/end-ride" {:post (middleware/driver-only end-ride)}]
   ["/add-vehicle" {:post (middleware/driver-only (middleware/handle-vehicle-addition add-vehicle))}]
   ["/update-vehicle-availability" {:post (middleware/driver-only (middleware/attach-vehicle update-vehicle-status))}]



   ["/order" {:get (middleware/with-auth (middleware/attach-order customer-view)) :post (middleware/with-auth (middleware/attach-order make-order))}]
   ;
   ]
  ;
  )

