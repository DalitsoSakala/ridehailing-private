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

(ns ride_hailing.models.location
  (:require [clojure.java.jdbc :as jdbc]
            [ride_hailing.db :as db]
            [ride_hailing.models.utils :as utils]))



(def create-table-sql
  (str "
        CREATE TABLE if not exists location (
            id int primary key not null auto_increment,
            lat int NOT NULL,
            lng int NOT NULL
        );
        "))

(def drop-table-sql
  (str "
        drop table location;
        "))


(defn create-table []
  (jdbc/db-do-commands db/db-settings create-table-sql))

(defn insert-location [data]
  (let [location (dissoc data :__anti-forgery-token)]

    (jdbc/insert! db/db-settings :location location)))

(defn update-location [location]
  (jdbc/update! db/db-settings :location {:id (:id location)} location))

(defn delete-location [id]
  (jdbc/delete! db/db-settings :location {:id id}))

(defn get-location [id]
  (jdbc/query db/db-settings ["SELECT * FROM location WHERE id=?" id] :row-fn utils/db-record))

