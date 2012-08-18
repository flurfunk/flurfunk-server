(ns flurfunk.server.marshalling
  "Marshalling and unmarshalling of XML objects."
  (:require [clojure.xml :as xml]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn parse-xml [input]
  (xml/parse (io/input-stream (if (instance? String input)
                                (.getBytes input "UTF-8") input))))

;; TODO: The marshal methods should return an XML object (no string).

(defn attribute
  ([message k]
     (attribute message k identity))
  ([message k convert]
     (if-let [v (k message)]
       (str " " (name k) "='" (convert v) "'")
       "")))

(defn marshal-message [message]
  (str "<message"
       (reduce str
               (map #(attribute message %) [:id :author :timestamp :channels]))
       ">" (:body message) "</message>"))

(defn marshal-messages [messages]
  (str "<messages>"
       (if (not (empty? messages))
         (reduce (fn [x y] (str x y))
                 (map (fn [message]
                        (marshal-message message))
                      messages)))
       "</messages>"))

(defn- replace-all [string replacements]
  (if (nil? string)
    nil
    (reduce (fn [string [from to]]
              (.replaceAll string from to)) string replacements)))

(defn- escape-xml [string]
  (replace-all string [["&" "&amp;"]
                       ["\"" "&quot;"]
                       ["'" "&#39;"]
                       ["<" "&lt;"]
                       [">" "&gt;"]]))

(defn unmarshal-message [xml]
  (let [attrs (:attrs xml)
        timestamp (:timestamp attrs)
        message (transient {:body (escape-xml (first (:content xml)))})]
    (if-let [id (:id attrs)] 
      (conj! message {:id id}))
    (if-let [author (:author attrs)] 
      (conj! message {:author (escape-xml author)}))    
    (if-let [timestamp (:timestamp attrs)]
      (if (not (empty? timestamp))
        (conj! message {:timestamp (Long. timestamp)})))
    (if-let [channels (:channels attrs)]
      (let [channel-coll (string/split channels #",")
            channel-coll (map string/trim channel-coll)
            channel-string (reduce #(str %1 "," %2) channel-coll)]
        (conj! message {:channels channel-string})))
    (persistent! message)))
  
(defn unmarshal-messages [xml]
  (map (fn [x] (unmarshal-message x))
       (:content xml)))
