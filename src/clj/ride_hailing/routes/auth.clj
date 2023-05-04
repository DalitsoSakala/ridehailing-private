(ns ride_hailing.routes.auth
  (:require
   [ride-hailing.layout :as layout]
   [ride-hailing.middleware :as middleware]
   [ring.util.response]
   [ride_hailing.models.user :as userdb]
   [ring.middleware.session]
   [ring.util.http-response :as response]))



(defn login [request]
  (ring.util.response/redirect "/"))

(defn logout-view [request]
  (ring.util.response/redirect "/"))


(defn login-view [request]
  (layout/render request "auth/login.html"))

(defn signup-view [request]
  (layout/render request "auth/signup.html"))

(defn signup [request]

  (ring.util.response/redirect "/"))


(defn auth-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                ;;  ring.middleware.session/wrap-session
                 ]}

   ["/logout" {:get (middleware/handle-logout logout-view)}]
   ["/login" {:get (middleware/hide-if-auth login-view)
              :post  (middleware/handle-login login)}]
   ["/signup" {:get (middleware/hide-if-auth signup-view)
               :post (middleware/handle-registration (middleware/handle-login signup))}]])

