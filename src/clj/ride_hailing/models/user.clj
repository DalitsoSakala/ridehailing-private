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

(ns ride_hailing.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [ride_hailing.db :as db]
            [ride_hailing.models.utils :as utils]
            ))



(def create-table-sql
  (str "
        create table if not exists user(
          id int unique primary key auto_increment,
          first_name varchar(200) not null,
          last_name varchar(200) not null,
          email varchar(200) unique,
          password varchar(225),
          role enum('driver','customer')
        );
        "))

(def drop-table-sql
  (str "
        drop table user;
        "))


(defn create-table []
  (jdbc/db-do-commands db/db-settings create-table-sql))

(defn insert-user [data]
  (let [user (dissoc data :__anti-forgery-token)]
    
    (jdbc/insert! db/db-settings :user user)))

(defn update-user [user]
  (jdbc/update! db/db-settings :user {:id (:id user)} user))

(defn delete-user [id]
  (jdbc/delete! db/db-settings :user {:id id}))

(defn get-user [id]
  (first (jdbc/query db/db-settings ["SELECT * FROM user WHERE id=?" id] :row-fn utils/db-record)))

(defn get-user-by-email [email]
  (first (jdbc/query db/db-settings ["SELECT * FROM user WHERE email=? LIMIT 1" email] :row-fn utils/db-record)))
