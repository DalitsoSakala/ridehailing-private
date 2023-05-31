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

(ns ride_hailing.models.ride
  (:require [clojure.java.jdbc :as jdbc]
            [ride_hailing.db :as db]
            [ride_hailing.models.utils :as utils]))



(def create-table-sql
  (str "
        
CREATE TABLE if not exists `ride` (
   id int not null primary key auto_increment,
   vehicle int not null,
   driver int not null ,
   customer int NOT NULL ,
   `rideorder` int NOT NULL ,
  `price` decimal NOT NULL,
  `date` datetime default now() NOT NULL,
   foreign key (vehicle) references vehicle(id),
   foreign key (driver) references user(id),
   foreign key (customer) references user(id),
   foreign key (rideorder) references rideorder(id)
);
        "))

(def drop-table-sql
  (str "
        drop table ride;
        "))


(defn create-table []
  (jdbc/db-do-commands db/db-settings create-table-sql))

(defn insert-ride [data]
  (let [ride (dissoc data :__anti-forgery-token)]

    (jdbc/insert! db/db-settings :ride ride)))

(defn update-ride [ride]
  (jdbc/update! db/db-settings :ride {:id (:id ride)} ride))

(defn delete-ride [id]
  (jdbc/delete! db/db-settings :ride {:id id}))

(defn get-ride [id]
  (jdbc/query db/db-settings ["SELECT * FROM ride WHERE id=?" id] :row-fn utils/db-record))

