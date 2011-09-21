(ns flurfunk.storage
  "Storage and retrieval of objects."
  (:require [fleetdb.client :as db]
	    [clojure.walk :as walk]))

(defprotocol Storage
  (storage-get-messages [this])
  (storage-add-message [this message])
  (storage-find-message [this id]))

(deftype MemoryStorage [messages] Storage
  (storage-get-messages [this] @messages)

  (storage-add-message
   [this message]
   (let [message-with-id (conj message {:id (str (count @messages))})]
     (swap! messages (fn [messages]
                       (cons message-with-id messages)))))

  (storage-find-message
   [this id]
   (first (filter (fn [message] (= id (:id message))) @messages))))

(deftype FleetDBStorage [client] Storage
  (storage-get-messages
   [this]
   (walk/keywordize-keys (client ["select" "messages"
                                  {"order" ["id", "desc"]}])))

  (storage-add-message
   [this message]
   ;; TODO: Find a better way to generate the ID.
   (let [message-with-id (conj message {:id (str (System/currentTimeMillis))})]
     (client ["insert" "messages" message-with-id])))

  (storage-find-message
   [this id]
   (walk/keywordize-keys (first (client ["select" "messages"
                                         {"where" ["=" :id id]}])))))

(defn- make-storage []
  (if (= (System/getProperty "flurfunk.fleetdb") "true")
    (FleetDBStorage. (db/connect {:host "127.0.0.1" :port 3400}))
    (do
      (println (str "Using memory database. Set the system property "
                    "\"flurfunk.fleetdb\" to \"true\" if you want a real one."))
      (MemoryStorage. (atom [])))))

(def ^{:private true} storage (make-storage))

(defn get-messages []
  (storage-get-messages storage))

(defn add-message [message]
  (storage-add-message storage message))

(defn find-message [id]
  (storage-find-message storage id))
