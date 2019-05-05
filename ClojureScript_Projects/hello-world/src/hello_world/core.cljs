(ns hello-world.core
    (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "This text is printed from src/hello-world/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


; (defn even-numbers
;   ([] (even-numbers 0) ;<-- Heres my error-- needs close-paren.  One needs to be added here, and removed from the next line.
;   ([n] (cons n (lazy-seq (even-numbers (+ n 2)))))))


(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))

(take 10 (even-numbers))

; cons returns a new list with an element appended to the given list:
; (cons 0 '(2 4 6))
; => (0 2 4 6)

(cons 1 '(2 3 4 5))

(def db {:users [{ :name "Eduardo" }]})
(def new-element {:name "Eva"})
(assoc db :users (cons new-element (:users db)))

(def my-text
  (reagent/atom "I am an atom of text"))

; can just use "atom" since we referred to it at top as such:
;  -->   (:require [reagent.core :as reagent :refer [atom]]))
; alternative style (shown in reagent blog) is:
; (:require [reagent.core :as r]))
; in which case we'd do stuff like this, since we referennce reagent.core as "r"
; --> (let someText [r/atom "some text"]
(def message
  (atom "try changing this text"))

(+ 3 1)
; this creates: <input> with various attributes mapped, such as:
; <input type="text" value="some message">
; (defn render-message-input []
;   [:input {
;     :type "text"
;     :value @message
;  }])


; ################## ################## ################## ##################
; ################## ################## ################## ##################
; BEGIN --- Examples from reagent github page https://github.com/reagent-project/reagent
; ################## ################## ################## ##################


; this shows adding css classes to html & inline styling
(defn some-component []
  [:div
    [:h3 "h3 component"]
    [:p.someclass
      "this is a p tag " [:strong " with a bold tag "] 
        [:span {:style {:color "red"}} " and red span tag "]
      " and additional text."
    ]
  ]  
)

(defn nested-elements []
  [:div.nestedStuff>p>b "Nested b tag in p tag in div"]  
)

; Can also use collections of classes
(defn example-collection-of-classes []
  [:div
    ; {:class ["a-class" (when active? "active") "b-class"]}
    {:class ["a-class" (when true "active") "b-class"]}
  ]  
)

(defonce click-count (atom 0))

(defn stateful-clicker []
  [:div 
    [:div {:on-click #(swap! click-count inc) :style {:border "1px solid grey" :border-radius "2rem" :line-height "2rem" :height "2rem" :width "10rem" :padding "1.5rem"}}
      "Ive been called " @click-count " times."]
    [:br]
  ])

(defn timer-component []
  (let [seconds-elapsed (atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)  
      [:div {:style {:border "1px solid grey" :border-radius "2rem" :line-height "2rem" :height "2rem" :width "10rem" :padding "1.5rem"}} "Seconds elapsed: " @seconds-elapsed ]
    )  
  )  
)
; ################## ################## ################## ##################
; ################## ///////////////////////////////////// ##################
; END ---- ///// Examples from reagent github page https://github.com/reagent-project/reagent
; ################## ################## ################## ##################




(defn render-message-input []
  [:input {
    :type "text"
    :value @message
  ; Here, we're going to change the message atom, passing something to target and value
    :on-change #(reset! message (-> % .-target .-value))
  }])



; This is an attempt to repurpose from this reference: https://reagent-project.github.io/news/reagent-is-async.html
; into a simple timer-- starts on component mount, stops on button click
; and displays timer as it runs.

; (defn timing-wrapper [f]
;   (let [start-time (atom nil)
;         render-time (atom nil)
;         now #(.now js/Date)
;         start #(reset! start-time (now))
;         stop #(reset! render-time (- (now) @start-time))
;       ; goal: start timer on mount.  On button click, stop (need to create button) in separate fn
;         timed-f (with-meta f 
;                   { :component-will-mount start })]
  
;     (fn []
;       [:div
;         [:p [:span "render time: " @render-time "ms" ]]
;         [timed-f]
;       ]
;     )
;   )
; )

; (defn stop-time []
;   [:div 
;     [:button (with-meta {:on-click @stop})]
;   ]
; ) 

(defn hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:h3 "Edit this ?? and 22 it changes!"]
   [:p (take 10 (even-numbers))]
   [:p @my-text]
  ; -> CSS NOTE: vertically centered text -- apparently via having same line-height and height, see reference: https://stackoverflow.com/a/14850381
   [:div [:p {:style {:border "1px solid grey" :border-radius "2rem" :line-height "2rem" :height "2rem" :width "10rem" :padding "1.5rem"}} @message]]
   [:br]
   [render-message-input]
    [some-component]
    [nested-elements]
    [example-collection-of-classes]
    [stateful-clicker]
    [timer-component]
    ; [timing-wrapper]
    ; [stop-time]
   ])



(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
