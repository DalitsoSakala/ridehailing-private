;; Copyright 2023 Dalitso Sakala <htts://dalitsosakala.github.io>
;; 
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;     http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns ride_hailing.models.vehicle
  (:require [clojure.java.jdbc :as jdbc]
            [ride_hailing.db :as db]
            [ride_hailing.models.utils :as utils]))



(def create-table-sql
  (str "
        
CREATE TABLE if not exists `vehicle` (
   id int not null primary key auto_increment,
  `model` varchar(100) NOT NULL,
  `color` varchar(100) NOT NULL,
  `number_plate` varchar(100) NOT NULL,
  `type` varchar(20) not null,
  `driver` int not null,
  `rate` decimal default 0.0 not null,
  `availability` enum('available','unavailable') default 'unavailable' not null,
  foreign key (driver) references user(id)
);
        "))

(def drop-table-sql
  (str "
        drop table vehicle;
        "))


(defn create-table []
  (jdbc/db-do-commands db/db-settings create-table-sql))

(defn insert-vehicle [data]
  (let [vehicle (dissoc data :__anti-forgery-token)]

    (jdbc/insert! db/db-settings :vehicle vehicle)))

(defn update-vehicle-status [id data]
  (jdbc/update! db/db-settings :vehicle data ["id = ?" id]))

(defn delete-vehicle [id]
  (jdbc/delete! db/db-settings :vehicle {:id id}))

(defn get-vehicle [id]
  (first (jdbc/query db/db-settings ["SELECT * FROM vehicle WHERE id=?" id] :row-fn utils/db-record)))

(defn get-available-vehicles []
  (jdbc/query db/db-settings ["SELECT * FROM vehicle where availability='available'"] :row-fn utils/db-record))

(defn get-vehicle-by-driver [id]
  (first (jdbc/query db/db-settings ["SELECT * FROM vehicle WHERE driver=?" id] :row-fn utils/db-record)))

