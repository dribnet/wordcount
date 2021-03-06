(ns wordcount.counter
  (:require 
    [clojure.string :as string]
    [cascalog.api :refer :all]
    [cascalog.logic.ops :as c]))

(defmapcatfn split
  "Accepts a sentence 1-tuple, splits that sentence on whitespace, and
   emits a single 1-tuple for each word."
  [^String sentence inc-counter]
  (inc-counter "wordcount" "tuples")
  (let [words (string/split sentence #"\s+")]
    (inc-counter "wordcount" "words" (count words))
    words))

(defn wordcount-query
  "Accepts a generator of lines of text and returns a subquery that
  generates a count for each word in the text sample."
  [src]
  (<- [?word ?count]
      (src ?textline)
      (counterfn :> ?inc-counter)
      (split ?textline ?inc-counter :> ?word)
      (c/count ?count)))

; rm -Rf output/simple && lein run -m wordcount.simple.run sample/words.txt output/simple && cat output/simple/part-00000
(defmain run [in out]
  (?- (hfs-textline out)
      (wordcount-query (hfs-textline in))))
