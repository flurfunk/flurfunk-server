(ns flurfunk.storage
  "Storage and retrieval of objects.")

(def messages [])

(defn get-messages []
  messages)

(defn- generate-id []
  (str (System/currentTimeMillis)))

(defn add-message [message]
  (let [message-with-id (conj message {:id (generate-id)})]
  (def messages (conj messages message-with-id))))

(defn find-message [id]
  (first (filter (fn [message] (= id (:id message))) messages)))
