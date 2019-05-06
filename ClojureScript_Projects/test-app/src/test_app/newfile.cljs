(ns test-app.newfile)
;     (:require
;       [reagent.core :as r]
;       [clojure.test :refer :all]))
; ; (use 'clojure.test)

; ; source: https://clojurescript.org/tools/testing

; ; (deftest test-async-awesome
; ;   (testing "the API is awesome"
; ;     (let [url "http://localhost:3000/test_getEmployees_All"
; ;           res (http/get url)]
; ;       (async done
; ;         (go
; ;             ; (println res)
; ;             ; (done) )))))
; ;           (is (= (<! res) :awesome))
; ;           (done))))))

; (deftest test-async-awesome
;   (testing "the API is awesome"
;     (let [url "http://localhost:3000/test_getEmployees_All"
;           res (http/get url)]
;       (async done
;         (go
;           (is (= (<! res) :awesome))
;           (done))))))