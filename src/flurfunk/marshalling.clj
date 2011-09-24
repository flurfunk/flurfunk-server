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
       "' timestamp='" (:timestamp message)
       "'>" (:body message) "</message>"))

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
                       ["<" "&lt;"]
                       [">" "&gt;"]]))

(defn unmarshal-message [xml]
  (let [attrs (:attrs xml)
        timestamp (:timestamp attrs)
        message (transient {:body (escape-xml (first (:content xml)))
                            :author (escape-xml (:author attrs))})]
    (if-let [id (:id attrs)] 
      (conj! message {:id id}))
    (if-let [timestamp (:timestamp attrs)]
      (if (not (empty? timestamp))
        (conj! message {:timestamp (Integer. timestamp)})))
    (persistent! message)))
  
(defn unmarshal-messages [xml]
  (map (fn [x] (unmarshal-message x))
       (:content xml)))
