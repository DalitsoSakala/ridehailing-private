(ns ride_hailing.models.init 
  (:require [ride_hailing.models.user :as userdb])
  (:require [ride_hailing.models.location :as locdb])
  (:require [ride_hailing.models.ride :as ridedb])
  (:require [ride_hailing.models.vehicle :as vehicledb])
  ;
  )

(defn run []
  (println "Checking user table ...")
  (userdb/create-table)
  (println "Checking location table ...")
  (locdb/create-table)
  (println "Checking vehicle table ...")
  (vehicledb/create-table)
  (println "Checking ride table ...")
  (ridedb/create-table)
  ;
  )