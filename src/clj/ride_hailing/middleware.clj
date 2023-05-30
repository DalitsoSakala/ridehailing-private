(ns ride-hailing.middleware
  (:require
   [ride-hailing.env :refer [defaults]]
   [clojure.tools.logging :as log]
   [ride-hailing.layout :refer [error-page]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ride-hailing.middleware.formats :as formats]
   [muuntaja.middleware :refer [wrap-format wrap-params]]
   [ring.util.response :as response]
   [ride_hailing.models.user :as userdb]
   [ride_hailing.models.vehicle :as vehicledb]
   [ride_hailing.models.rideorder :as orderdb]
   [ride-hailing.config :refer [env]]
   [ring.middleware.session :as session]
   [ring.middleware.session.memory :as store]
   [ring.middleware.reload :as reload]
   [ring.middleware.flash :refer [wrap-flash]]
  ;;  [ring.adapter.undertow.middleware.session :refer [wrap-session]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-internal-error [handler]
  (let [error-result (fn [^Throwable t]
                       (log/error t (.getMessage t))
                       (error-page {:status 500
                                    :title "Something very bad has happened!"
                                    :message "We've dispatched a team of highly trained gnomes to take care of the problem."}))]
    (fn wrap-internal-error-fn
      ([req respond _]
       (handler req respond #(respond (error-result %))))
      ([req]
       (try
         (handler req)
         (catch Throwable t
           (error-result t)))))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (error-page
     {:status 403
      :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn
      ([request]
         ;; disable wrap-formats for websockets
         ;; since they're not compatible with this middleware
       ((if (:websocket? request) handler wrapped) request))
      ([request respond raise]
       ((if (:websocket? request) handler wrapped) request respond raise)))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      (reload/wrap-reload)
      (session/wrap-session {:store (store/memory-store (atom {}))})

      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      wrap-internal-error))



(defn with-auth [handler]
  (fn [request]
    (if (:user (:session request))
      (let 
        [
        user  (:user (:session request))
          accepted_order (if (= "customer" (:role user)) (orderdb/get-accepted-order-for-customer (:id user)) nil ) 
        session (:session request)
        session (assoc session :accepted-order accepted_order)
        request (assoc request :session session)
        ]

        (handler request)
      )
                                         

      (response/redirect "/login"))))

(defn driver-only [handler]
  (fn [request]
    (if (= (:role (:user (:session request))) "driver")
      (handler request)
      (response/redirect "/"))))



(defn handle-login [handler]
  (fn [{:keys [params] :as request}]
    ;; Perform actions before the request is handled
    ;; (println "Before handling request...")
    (let
     [user (userdb/get-user-by-email (:email params))
      authenticated (= (:password user) (:password params))
      session (if  authenticated (assoc (:session request) :user user)  (:session request))
      vehicle (if (and (= (:role user) "driver") authenticated)  (vehicledb/get-vehicle-by-driver (:id user)) nil)
      session (if  vehicle (assoc session :vehicle vehicle)  session)
      ;
      ]
      ;; (handler request)
      ;; Pass the request down the middleware stack...
      (let [response (handler request)]

      ;; Perform actions after the request is handled
      ;; Add headers to the response , e.t.c
      ;; (println "After handling request...")
        (assoc response :session session)))
    ;
    ))

(defn handle-logout [handler]
  (fn [{:keys [params] :as request}]
    ;; Perform actions before the request is handled
    ;; (println "Before handling request...")



      ;; Pass the request down the middleware stack...
    (let [response (handler request)]

      ;; Perform actions after the request is handled
      ;; Add headers to the response , e.t.c
      ;; (println "After handling request...")
      (assoc response :session {})))
    ;
  )


(defn handle-registration [handler]
  (fn [request]

    (let [params (:params request)]
      (userdb/insert-user params))
  ;;

    (handler request)))

(defn handle-vehicle-addition [handler]
  (fn [request]

    (let [params (:params request)]
      (vehicledb/insert-vehicle params))
  ;;
    (let [response (handler request)
          session (:session request)
          user (:user session)
          vehicle (if (= (:role user) "driver") (vehicledb/get-vehicle-by-driver (:id user)) nil)
          response (assoc response :session (assoc session :vehicle vehicle))]

      ;; Perform actions after the request is handled
      ;; Add headers to the response , e.t.c
      ;; (println "After handling request...")
      response)))

(defn attach-order [handler]
  (fn [request]

    (let [session (:session request)
          user  (:user session)
          id (:id user)
          orders (if (= (:role user) "driver") (orderdb/get-rideorders-for-driver id) (orderdb/get-rideorder-for-customer id))

          response (handler (assoc request :session (assoc session :orders orders)))


          response (assoc response :session (assoc session :orders orders))]
      ;; Perform actions after the request is handled
      ;; Add headers to the response , e.t.c
      ;; (println "After handling request...")
      response)))




(defn attach-vehicle [handler]
  (fn [request]

    (let [session (:session request)
          uid (:id (:user session))
          response (handler request)
          vehicle (vehicledb/get-vehicle-by-driver uid)
          session (:session request)

          response (assoc response :session (assoc session :vehicle vehicle))]

      ;; Perform actions after the request is handled
      ;; Add headers to the response , e.t.c
      ;; (println "After handling request...")
      response)))


(defn hide-if-auth [handler]
  (fn [request]
    (if (:user (:session request))

      (response/redirect "/")
      (handler request)
      ;
      )))