(ns flurfunk.server.storage
  "Storage and retrieval of objects."
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as sql]
            [clojure.walk :as walk]
            [fleetdb.client :as db]))

(def message-limit 200)

(defprotocol Storage
  (storage-get-messages [this] [this options])
  (storage-add-message [this message])
  (storage-find-message [this id])
  (storage-clear-messages [this]))

(defn- generate-message-id []
  (str (java.util.UUID/randomUUID )))

(defn- predicate-for-option [option value]
  (case option
        :before (fn [message] (< (:timestamp message) value))
        :since (fn [message] (> (:timestamp message) value))))

(defn- predicate-for-options [options]
  (let [predicates (map (fn [option]
			  (predicate-for-option (first option) (second option)))
			options)]
    (fn [message]
      (every? (fn [predicate] (predicate message)) predicates))))

(deftype MemoryStorage [messages] Storage
  (storage-get-messages [this] (take message-limit @messages))

  (storage-get-messages
   [this options]
   (take (or (:count options) message-limit)
         (filter (predicate-for-options (dissoc options :count)) @messages)))

  (storage-add-message
   [this message]
   (let [message-with-id (conj message {:id (str (generate-message-id))})]
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
   [this options]
   (take (or (:count options) message-limit)
         (filter (predicate-for-options (dissoc options :count))
                 (walk/keywordize-keys
                  (client ["select" "messages"
                           {"order" ["timestamp", "desc"]}])))))
  
  (storage-add-message
   [this message]
   (let [message-with-id
         (conj message {:id (generate-message-id)})]
     (client ["insert" "messages" message-with-id])))

  (storage-find-message
   [this id]
   (walk/keywordize-keys (first (client ["select" "messages"
                                         {"where" ["=" :id id]}]))))

  (storage-clear-messages
   [this]
   (client ["delete", "messages"])))

(def ^{:private true} postgresql-db
     (or (System/getenv "DATABASE_URL")
         "postgresql://flurfunk:flurfunk@localhost:5432/flurfunk"))

(defn- postgresql-table-exists?
  [name]
  (sql/with-query-results results
    [(str "SELECT EXISTS(SELECT * FROM information_schema.tables"
          " WHERE table_name = '" name "')")]
    (boolean (Boolean. (:?column? (first results))))))

(defn- postgresql-select-messages
  [constraints]
  (try (sql/with-connection postgresql-db
         (if (postgresql-table-exists? "messages")
           (vec (sql/with-query-results results
                  [(str "SELECT id, akeys(attributes), avals(attributes)"
                        " FROM messages " constraints)]
                  (map (fn [result]
                         (let [message (zipmap
                                        (map keyword
                                             (vec (.getArray (:akeys result))))
                                        (vec (.getArray (:avals result))))
                               message (assoc message :id (:id result))
                               message (update-in message [:timestamp]
                                                  #(if % (Long. %) 0))]
                           message))
                       (into [] results))))))
       (catch Exception e (.printStackTrace e))))

(defn- postgresql-select-messages-limited
  [limit]
  (postgresql-select-messages (str "ORDER BY id DESC LIMIT " limit)))

(deftype PostgreSQLStorage [] Storage
  (storage-get-messages
   [this]
   (postgresql-select-messages-limited message-limit))
  
  (storage-get-messages
   [this options]
   (filter (predicate-for-options (dissoc options :count))
           (postgresql-select-messages-limited (or (:count options)
                                                   message-limit))))

  (storage-add-message
   [this message]
   (sql/with-connection postgresql-db
     (let [entries (map #(str \" (name (first %))\" " => \"" (second %) \")
                        message)
           value-string (string/join ",\n" entries)]
       (sql/do-commands
        (str "CREATE TABLE IF NOT EXISTS messages"
             " (id serial PRIMARY KEY, attributes hstore)")
        (str "INSERT INTO messages (attributes) VALUES ('"
             value-string "')")))))

  (storage-find-message
   [this id]
   (postgresql-select-messages (str "WHERE id = " id)))

  (storage-clear-messages
   [this]
   (sql/with-connection postgresql-db
     (sql/do-commands "drop table if exists messages"))))

(defn- make-storage []
  (case (System/getProperty "flurfunk.db")
        "fleetdb" (do
                    (println "Using FleetDB storage backend.")
                    (FleetDBStorage. (db/connect {:host "127.0.0.1"
                                                  :port 3400})))
        "postgresql" (do
                       (println "Using PostgreSQL storage backend.")
                       (PostgreSQLStorage.))
        (do
          (println (string/trim "
Using in-memory storage backend. To use a persistent backend, set the system
property \"flurfunk.db\" to \"fleetdb\" or \"postgresql\"."))
          (MemoryStorage. (atom [])))))

(def ^{:private true} storage (make-storage))

(defn get-messages
  ([]
     (storage-get-messages storage))
  ([options]
     (storage-get-messages storage options)))

(defn add-message [message]
  (storage-add-message storage message))

(defn find-message [id]
  (storage-find-message storage id))

(defn clear-messages []
  (storage-clear-messages storage))
