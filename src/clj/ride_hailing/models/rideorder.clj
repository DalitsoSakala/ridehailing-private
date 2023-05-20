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

(ns ride_hailing.models.rideorder
  (:require [clojure.java.jdbc :as jdbc]
            [ride_hailing.db :as db]
            [ride_hailing.models.utils :as utils]))



(def create-table-sql
  (str "
        
CREATE TABLE if not exists `rideorder` (
   id int not null primary key auto_increment,
  `driver` int not null,
  `customer` int not null,
  `date` datetime default now() not null,
  `status` enum('rejected','accepted','cancelled','open','completed')  default 'open' not null,
  foreign key (driver) references user(id),
  foreign key (customer) references user(id)
);
        "))

(def drop-table-sql
  (str "
        drop table rideorder;
        "))


(defn create-table []
  (jdbc/db-do-commands db/db-settings create-table-sql))

(defn insert-rideorder [data]

  (jdbc/insert! db/db-settings :rideorder data))

(defn update-rideorder-status [id data]
  (jdbc/update! db/db-settings :rideorder data ["id = ?" id]))

(defn update-rideorders-for-driver [driver-id data]
  (jdbc/update! db/db-settings :rideorder data ["driver = ?" driver-id]))


(defn delete-rideorder [id]
  (jdbc/delete! db/db-settings :rideorder {:id id}))

(defn get-rideorder [id]
  (first (jdbc/query db/db-settings ["SELECT * FROM rideorder WHERE id=?" id] :row-fn utils/db-record)))

(defn get-available-rideorders []
  (jdbc/query db/db-settings ["SELECT * FROM rideorder where status='open'"] :row-fn utils/db-record))

(defn get-accepted-order-for-driver [driver-id]
  (first (jdbc/query db/db-settings [(str "
                                    SELECT rideorder.id, u.first_name, u.last_name FROM 
                                           (
                                             select * from user 
                                               where id in
                                                 (
                                                    select customer from rideorder
                                                      where driver=? and status ='accepted'
                                                ) 
                                           ) as u
                                          inner join rideorder on rideorder.customer=u.id;
                                    ") driver-id] :row-fn utils/db-record)))

(defn get-rideorders-for-driver [id]
  (jdbc/query db/db-settings [(str "
                                    select
  rideorder.id as id,
  mybooker.id as cus_id,
  first_name,
  last_name
from
  (
    select
      *
    from
      user
    where
      id in (
        select
          customer
        from
          (
            select
              id,
              customer
            from
              rideorder
            where
              driver = ?
              and status = 'open'
          ) as myorders
      )
  ) as mybooker
  inner join rideorder on rideorder.customer = mybooker.id
where
  rideorder.status = 'open';") id] :row-fn utils/db-record))

(defn get-rideorder-for-customer [id]
  (first (jdbc/query db/db-settings ["SELECT * FROM rideorder WHERE status='open' and customer=?" id] :row-fn utils/db-record)))

