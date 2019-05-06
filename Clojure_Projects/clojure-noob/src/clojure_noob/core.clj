(ns clojure-noob.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World2ll1111l3333222!!"))


;---- REPL notes
; (load "core") <-- to load updated file into repl

; 'average' is a symbol, whose value is a function. 
(defn average [x y] (/ (+ x y) 2))

; here we feed it two vectors
(map average [1 2 3] [4 5 6])

(defn addx [x] (fn [y] (+ x y)))

 (map (addx 5) [1 2 3 4 5])

;  function shorthand:
; #(+ %1 %2)

; Will create a function that calls '+' with two arguments %1 and %2.
(map #(+ %1 5) [1 2 3 4 5])

; computes factorial of 5
; The loop special form establishes bindings followed by expressions to be evaluated
 (loop [i 5 accumulator 1]
;  if zero, we stop and return the accumulator
 (if (zero? i)
    accumulator
; else, decrement i, then multiply accumulator * i, and loop back
    (recur (dec i) (* accumulator i))))


; def names a global thing
(def pi 3.14)

; use Let to bind values to local names
(let [r 3.0
      c (* 2.0 Math/PI r)
      a (* Math/PI (* r r))]
  {:radius r
   :circumfrence c
   :area a})
; output: {:radius 3.0, :circumfrence 18.84955592153876, :area 28.274333882308138}

; destructuring example
(defn currency-of
 [[amount currency]]
 currency)

(currency-of [125 "usd"])
; "usd"


; However, notice that we do not use the amount local. In that case, we can ignore it by replacing it with an underscore:
(defn currency-of
  [[_ currency]]
  currency)

; Destructuring can nest (destructure deeper than one level):
(defn first-first
  [[[i _] _]]
  i)

(first-first [["hi" "there"] ["good" "bye"]])

; While this article does not cover let and locals, it is worth demonstrating that positional destructuring works exactly the same way for let bindings:
; http://clojure-doc.org/articles/language/functions.html
(let [pair         [10 :gbp]
      [_ currency] pair]
  currency)

