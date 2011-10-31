(ns flurfunk.storage
  "Storage and retrieval of objects."
  (:require [fleetdb.client :as db]
	    [clojure.walk :as walk]))

(def message-limit 200)

(defprotocol Storage
  (storage-get-messages [this] [this since])
  (storage-get-messages-before [this] [this before])
  (storage-get-messages-after [this] [this after])
  (storage-add-message [this message])
  (storage-find-message [this id])
  (storage-clear-messages [this]))

(deftype MemoryStorage [messages] Storage
  (storage-get-messages [this] (take message-limit @messages))

  (storage-get-messages
   [this since]
   (take message-limit (filter (fn [message] (> (:timestamp message) since)) @messages)))

  (storage-get-messages-before
   [this before]
   (filter (fn [message] (< (:timestamp message) before)) @messages))
  
  (storage-add-message
   [this message]
   (let [message-with-id (conj message {:id (str (count @messages))})]
     (swap! messages (fn [messages]
                       (cons message-with-id messages)))))

  (storage-find-message
   [this id]
   (first (filter (fn [message] (= id (:id message))) @messages)))

  (storage-clear-messages
   [this]
   (swap! messages (fn [messages] '()))))

(deftype FleetDBStorage [client] Storage
  (storage-get-messages
   [this]
   (walk/keywordize-keys (client ["select" "messages"
                                  {"order" ["timestamp", "desc"]
                                   "limit" message-limit}])))

  (storage-get-messages
   [this since]
   (walk/keywordize-keys (client ["select" "messages"
                                  {"where" [">" :timestamp since]
                                   "order" ["timestamp", "desc"]
                                   "limit" message-limit}])))
  (storage-get-messages-before
   [this before]
   (walk/keywordize-keys (client ["select" "messages"
                                  {"where" ["<" :timestamp before]
                                   "order" ["timestamp", "desc"]
                                   "limit" message-limit}])))
  
  (storage-add-message
   [this message]
   (let [message-with-id
         (conj message {:id (str (client ["count" "messages"]))})]
     (client ["insert" "messages" message-with-id])))

  (storage-find-message
   [this id]
   (walk/keywordize-keys (first (client ["select" "messages"
                                         {"where" ["=" :id id]}]))))

  (storage-clear-messages
   [this]
   (client ["delete", "messages"])))

(defn- make-storage []
  (if (= (System/getProperty "flurfunk.fleetdb") "true")
    (FleetDBStorage. (db/connect {:host "127.0.0.1" :port 3400}))
    (do
      (println (str "Using memory database. Set the system property "
                    "\"flurfunk.fleetdb\" to \"true\" if you want a real one."))
      (MemoryStorage. (atom [])))))

(def ^{:private true} storage (make-storage))

(defn get-messages
  ([]
     (storage-get-messages storage))
  ([since]
     (storage-get-messages storage since)))

(defn get-messages-before
  ([before]
     (storage-get-messages-before storage before)))

(defn get-messages-after
  ([before]
     ;;Note that we just use the overridden messages-since form here.TODO: Replace the override.
     (storage-get-messages storage before)))
          

(defn add-message [message]
  (storage-add-message storage message))

(defn find-message [id]
  (storage-find-message storage id))

(defn clear-messages []
  (storage-clear-messages storage))