(ns ride-hailing.handler
  (:require
    [ride-hailing.middleware :as middleware]
    [ride-hailing.layout :refer [error-page]]
    [ride_hailing.routes.index :refer [index-routes]]
    [ride_hailing.routes.auth :refer [auth-routes]]
    [ride_hailing.routes.ride :refer [ride-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [ride-hailing.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(defn- async-aware-default-handler
  ([_] nil)
  ([_ respond _] (respond nil)))


(mount/defstate app-routes
  :start
  (ring/ring-handler
   (ring/router
    [(index-routes) (auth-routes) (ride-routes)])
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (wrap-content-type
     (wrap-webjars async-aware-default-handler))
    (ring/create-default-handler
     {:not-found
      (constantly (error-page {:status 404, :title "404 - Page not found"}))
      :method-not-allowed
      (constantly (error-page {:status 405, :title "405 - Not allowed"}))
      :not-acceptable
      (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  (middleware/wrap-base #'app-routes))
