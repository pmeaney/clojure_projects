(ns test-app.core
    (:require
      [reagent.core :as r]))

;; -------------------------
;; Views

(defn hello-component [name]
  [:p "Hello, " name "!"])

(defn say-hello []
  [hello-component "world"])

(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(defn lister-user []
  [:div
   "Here is a list:"
   [lister (range 3)]])

(def click-count (r/atom 0))

(defn counting-component []
[:div
  "The atom " [:code "click-count"] " has value: "
  @click-count ". "
  [:input {:type "button" :value "Click me!"
          :on-click #(swap! click-count inc)}]])

(defn timer-component []
  (let [seconds-elapsed (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
       "Seconds Elapsed: " @seconds-elapsed])))

(defn timer-component-no-div []
  (let [seconds-elapsed (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
    ; removed :div just after this opening bracket
      [
       "Seconds Elapsed: " @seconds-elapsed])))

(defn atom-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn shared-state []
  (let [val (r/atom "foo")]
    (fn []
      [:div
       [:p "The value is now: " @val]
       [:p "Change it here: " [atom-input val]]])))

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

  (def bmi-data (r/atom {:height 180 :weight 80}))

(defn calc-bmi []
  (let [{:keys [height weight bmi] :as data} @bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! bmi-data assoc param (.. e -target -value))
                        (when (not= param :bmi)
                          (swap! bmi-data assoc :bmi nil)))}])

(defn bmi-component []
  (let [{:keys [weight height bmi]} (calc-bmi)
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    [:div
     [:h3 "BMI calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider :bmi bmi 10 50]]]))

(defonce timer (r/atom (js/Date.)))

(defonce time-color (r/atom "#f34"))

(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

(defn greeting [message]
  [:h4 message])

(defn clock []
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:div.example-clock
     {:style {:color @time-color}}
     time-str]))

(defn color-input []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change #(reset! time-color (-> % .-target .-value))}]])

(defn simple-example []
  [:div
   [greeting "Hello world, it is now"]
   [clock]
   [color-input]])

 (defonce todos (r/atom (sorted-map)))

(defonce counter (r/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))

(defn toggle [id] (swap! todos update-in [id :done] not))
(defn save [id title] (swap! todos assoc-in [id :title] title))
(defn delete [id] (swap! todos dissoc id))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! todos mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! todos mmap remove #(get-in % [1 :done])))

(defonce init (do
                (add-todo "Rename Cloact to Reagent")
                (add-todo "Add undo demo")
                (add-todo "Make all rendering async")
                (add-todo "Allow any arguments to component functions")
                (complete-all true)))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def todo-edit (with-meta todo-input
                 {:component-did-mount #(.focus (r/dom-node %))}))

(defn todo-stats [{:keys [filt active done]}]
  (let [props-for (fn [name]
                    {:class (if (= name @filt) "selected")
                     :on-click #(reset! filt name)})]
    [:div
     [:span#todo-count
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul#filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done)
       [:button#clear-completed {:on-click clear-done}
        "Clear completed " done])]))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox" :checked done
                        :on-change #(toggle id)}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click #(delete id)}]]
       (when @editing
         [todo-edit {:class "edit" :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todo-app [props]
  (let [filt (r/atom :all)]
    (fn []
      (let [items (vals @todos)
            done (->> items (filter :done) count)
            active (- (count items) done)]
        [:div
         [:section#todoapp
          [:header#header
           [:h1 "todos"]
           [todo-input {:id "new-todo"
                        :placeholder "What needs to be done?"
                        :on-save add-todo}]]
          (when (-> items count pos?)
            [:div
             [:section#main
              [:input#toggle-all {:type "checkbox" :checked (zero? active)
                                  :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul#todo-list
               (for [todo (filter (case @filt
                                    :active (complement :done)
                                    :done :done
                                    :all identity) items)]
                 ^{:key (:id todo)} [todo-item todo])]]
             [:footer#footer
              [todo-stats {:active active :done done :filt filt}]]])]
         [:footer#info
          [:p "Double-click to edit a todo"]]])))) 

(defn home-page []
  [:div 
    [:h2 "Welcome to Reagent"]
    [:div (hello-component "Pat")]
    [:div (lister-user)]
    [:div (counting-component)]
    ; [:div (timer-component)] <-- doesn't display. Perhaps b/c div is already in timer-component
    ; [:div (timer-component-no-div)] nope. still doesnt display
    [(timer-component)]
    [(shared-state)]
    (simple-component) ; note: no brackets.
    (bmi-component)
    (simple-example)
    [:br]
    [:br]
    [:br]
    [:br]
    [todo-app]
    [:br]
    [:br]
    [:br]
    [:br]
  ]
)
; ##############################################################################################
; ############################################## Above here is Reagent web code
; ##############################################################################################
; ##############################################################################################

  ; [:div [:h2 "Welcome to Reagent" :div "hi there"]])
  ; [:div (bmi-component [200 100 30])])


; (defn retrieveThingInPositionOne
;     [[one two three]]
;     one)

; (defn retrieveThingInPositionTwo
;     [[one two three]]
;     two)

; (defn retrieveThingInPositionThree
;     [[one two three]]
;     three)
; (retrieveThingInPositionOne ["first" "second" "third"])
; (retrieveThingInPositionTwo ["first" "second" "third"])
; (retrieveThingInPositionThree ["first" "second" "third"])

; (defn chooser
;   [[first-choice second-choice & unimportant-choices]]
;   (println (str "Your first choice is: " first-choice))
;   (println (str "Your second choice is: " second-choice))
;   (println (str "We're ignoring the rest of your choices. "
;                 "Here they are in case you need to cry over them: "
;                 (clojure.string/join ", " unimportant-choices))))

; (chooser ["Marmalade", "Handsome Jack", "Pigpen", "Aquaman"])

; (defn codger-communication
;   [whippersnapper]
;   (str "Get off my lawn, " whippersnapper "!!!"))

; (defn codger
;   [& whippersnappers]
;   (map codger-communication whippersnappers))

; These are not working.  Why not?
; (codger "Billy" "Anne-Marie" "The Incredible Bulk")
; (codger ["vector_Billy" "vector_Anne-Marie" "vector_The Incredible Bulk"])

; Anonymous Functions
; You create anonymous functions in two ways. The first is to use the fn form:
; (fn [param-list]
  ; function body)

; Here, we map a vector into our function.  The items (arguments) within the vector
; are mapped as the parameter called 'name'
; (map (fn [name] (str "Hi, " name))
;      ["Darth Vader" "Mr. Magoo"])

(def asym-hobbit-body-parts [{:name "head" :size 3}
                             {:name "left-eye" :size 1}
                             {:name "left-ear" :size 1}
                             {:name "mouth" :size 1}
                             {:name "nose" :size 1}
                             {:name "neck" :size 2}
                             {:name "left-shoulder" :size 3}
                             {:name "left-upper-arm" :size 3}
                             {:name "chest" :size 10}
                             {:name "back" :size 10}
                             {:name "left-forearm" :size 3}
                             {:name "abdomen" :size 6}
                             {:name "left-kidney" :size 1}
                             {:name "left-hand" :size 2}
                             {:name "left-knee" :size 2}
                             {:name "left-thigh" :size 4}
                             {:name "left-lower-leg" :size 3}
                             {:name "left-achilles" :size 1}
                             {:name "left-foot" :size 2}])

; Question -- What is the # doing next to the string below?
; ohhh... it checks if the name begins with 'left'
; whereas the carot ^ is regex for 'only terms which begin with'

; (defn matching-part
;   [[bodypart]]
;   {:name (clojure.string/replace (:name [bodypart]) #"^left-" "right-")
;    :size (:size [bodypart])})

; (defn symmetrize-body-parts
;   "expects a seq of maps that have a :name and :size"
;   [asym-body-parts]
;   (loop [remaining-asym-parts asym-body-parts
;           final-body-parts []]
;     (if (empty? remaining-asym-parts)
;       final-body-parts
;       (let [[part & remaining] remaining-asym-parts]
;         (recur remaining
;           (into final-body-parts
;             ; Here, we finally call matching-part, which was defined above
;             (set [part (matching-part part)]
;             )
;           )
;         )
;       )
;     )
;   )
; )

; error from above code    source: https://www.braveclojure.com/do-things/
; app:cljs.user!{:conn 2}=> (symmetrize-body-parts asym-hobbit-body-parts)
; #object[Error Error: nth not supported on this type cljs.core/PersistentArrayMap]
; Error: nth not supported on this type cljs.core/PersistentArrayMap

; copied directly from source:

(defn matching-part
  [part]
  {:name (clojure.string/replace (:name part) #"^left-" "right-")
   :size (:size part)})

(defn symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (loop [remaining-asym-parts asym-body-parts
         final-body-parts []]
    (if (empty? remaining-asym-parts)
      final-body-parts
      (let [[part & remaining] remaining-asym-parts]
        (recur remaining
               (into final-body-parts
                     (set [part (matching-part part)])))))))

(defn better-symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (reduce (fn [final-body-parts part]
            (into final-body-parts (set [part (matching-part part)])))
          []
          asym-body-parts))
        

; https://www.braveclojure.com/do-things/ Exercises at bottom of page:

(defn dec-maker
  "Define a decrementor function, then use it to decrement a number"
  ; parameter: dec-by.  this is passed in when defining the decrementor function itself
  [dec-by] 
  ; anonymous function: operator is -.  % represents parameter.  
  ; (#(* % 3) 8)
  ; => 24
  ; Below, dec-by represents the inner term, 3.  We set it as 9.
  ; Then, we pass 10, which represents the outter term.  (- 9 10)
  ; Nope, incorrect. (- 9 10) => -1.  So, it's (- 10 9)
  ; Decrement a thing, by the thing we choose
  ; So, dec-by is set when we setup the decrementor function.
  ; % is then used within the decrementor function as its own parameter.
  #(- % dec-by)) 

(def dec9 (dec-maker 9))
(dec9 10)
; => 1

; first attempt
; (defn mapset
;   "takes inc or dec, and an array. returns an inc'-ed or dec'-ed set"
;   [incOrDec theArray]
;   (incOrDec (set theArray))
;   )
; (mapset inc [1 1 2 2])
; => "#{1 2}1"

; second attempt -- Got it!
(defn mapset
  "takes inc or dec, and an array. returns an inc'-ed or dec'-ed set"
  [incOrDec theArray]
  (set (map incOrDec theArray))
) 
; (mapset inc [1 1 2 2])
; => #{2 3} 


; https://www.braveclojure.com/do-things/ Exercises at bottom of page:
; (def radial-alien-body-parts [{:name "head" :size 3}
;                              {:name "first-eye" :size 1}
;                              {:name "first-arm" :size 1}
;                              {:name "mouth" :size 1}
;                              {:name "nose" :size 1}])

; (defn radial-matching-part
;   [part]
;   {:name (clojure.string/replace (:name part) #"^first-" "second-" "third-" "fourth-" "fifth-")
;    :size (:size part)})

; (defn alien-better-symmetrize-body-parts
;   "Expects a seq of maps that have a :name and :size"
;   [asym-body-parts]
;   (reduce (fn [final-body-parts part]
;             (into final-body-parts (set [part (radial-matching-part part)])))
;           []
;           asym-body-parts))

; (alien-better-symmetrize-body-parts radial-alien-body-parts)

;;;;; Possible solution: ;;;;;;
; check if part ends in a number:
; change structure to "eye-1"
; Check suffix term: 1
; If < 5, take prefix (exmaple: "eye-") and add incremented number, loop.

; for more user readable version, create a keyword map: {:1 "first" :2 "second"}  etc. and
; replace number literals with number word-strings
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def food-journal
  [{:month 1 :day 1 :human 5.3 :critter 2.3}
   {:month 1 :day 2 :human 5.1 :critter 2.0}
   {:month 2 :day 1 :human 4.9 :critter 2.1}
   {:month 2 :day 2 :human 5.0 :critter 2.5}
   {:month 3 :day 1 :human 4.2 :critter 3.3}
   {:month 3 :day 2 :human 4.0 :critter 3.8}
   {:month 4 :day 1 :human 3.7 :critter 3.9}
   {:month 4 :day 2 :human 3.7 :critter 3.6}])

  

;; -------------------------------------------------
;; -------------------------------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))