(ns flurfunk.marshalling
  "Marshalling and unmarshalling of XML objects."
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]))

(defn parse-xml [string]
  (xml/parse (io/input-stream (streams/to-byte-array string))))

;; TODO: The marshal methods should return an XML object (no string).

(defn marshal-message [message]
  (str "<message id='" (:id message)
       "' author='" (:author message)
       "'>" (:body message) "</message>"))

(defn marshal-messages [messages]
  (str "<messages>"
       (if (not (empty? messages))
         (reduce (fn [x y] (str x y))
                 (map (fn [message]
                        (marshal-message message))
                      messages)))
       "</messages>"))

(defn unmarshal-message [xml]
  (let [attrs (:attrs xml)
        message {:body (first (:content xml))
                 :author (:author attrs)}]
    (if-let [id (:id attrs)] 
      (conj message {:id id})
      message)))
  
(defn unmarshal-messages [xml]
  (map (fn [x] (unmarshal-message x))
       (:content xml)))
