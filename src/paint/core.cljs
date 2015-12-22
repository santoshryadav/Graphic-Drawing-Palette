(ns ^:figwheel-always paint.core
  (:require [reagent.core :as reagent :refer [atom]] [reagent.core :as r] [schema.core :as s]))

(enable-console-print!)

;;point1 and point2 are temporary variables to hold the values of start-point and end-point respectively.
(def point1 (atom {}))
(def point2 (atom {}))

;;click-count will keep the count of the number of clicks made on the drawing board.
(def click-count (atom 0))

;;active-button holds value of the currently active state. It can be in line, rect, circle or nil mode.
(def active-button (atom "nil"))

;;This is the main BIG r/atom for the start of the point for the diagrams.
(def start-point (r/atom {:x 0 :y 0}))
;;This is the main BIG r/atom for the end of the point for the diagrams.
(def end-point (r/atom {:x 0 :y 0}))

;;This provides a definition of prismatic schema to the r/atom variables.
(def my-schema-startpoint
  {:x s/Int
   :y s/Int
   })
(def my-schema-endpoint
  {:x s/Int
   :y s/Int
   })

;;The below code validates the data assigned to the r/atom varibles. It throws an exception if the values done match to
;;what is provided into the schema.
;;Error in Console will be something like this:-------------------------
;;***********
;;Uncaught #error {:message "Value does not match schema: {:x (not (cljs$core$integer? \"f\"))}", :data {:type :schema.core/error, :schema {:x #schema.core.Predicate{:p? #object[cljs$core$integer_QMARK_ "function cljs$core$integer_QMARK_(n){
;;return (typeof n === 'number') && (!(isNaN(n))) && (!((n === Infinity))) && ((parseFloat(n) === parseInt(n,(10))));
;;}"], :pred-name cljs$core$integer?}, :y #schema.core.Predicate{:p? #object[cljs$core$integer_QMARK_ "function cljs$core$integer_QMARK_(n){
;;return (typeof n === 'number') && (!(isNaN(n))) && (!((n === Infinity))) && ((parseFloat(n) === parseInt(n,(10))));
;;}"], :pred-name cljs$core$integer?}}, :value {:x "f", :y 4}, :error {:x (not (cljs$core$integer? "f"))}}}
;;***********

(s/validate my-schema-startpoint @start-point)
(s/validate my-schema-endpoint @end-point)


;;draw-button variable tells if the button that is clciked is a drawing button or not.
(def draw-button (atom "no"))

;;board-clicked tells if the click was made on the drawing board.
(def board-clicked (atom "no"))

;;This is the list where we keep the state of our project. All diagramatic states are placed here.
(def undo-list (atom nil))

;;rect-values is used to store the coordinates of the rectangle.
(def rect-values (atom []))



;;This function will show the drawing board cordinates of the mouse movement and current active drawing mode.
(defn stateful-with-atom []
[:div "Drawing Board Coordinates: " (- (:x @end-point) 100) " Y: " (- (:y @end-point) 85)
      [:br] @active-button " mode"])


;;This function calculates what should be the rectangle drawing cordinates based on the mouse movement.
;;There are four cases for the movement.
(defn rectangle-data []
  (if (and (> (:x @point2) (:x @point1)) (> (:y @point2) (:y @point1)))
    (reset! rect-values (conj [] (:x @point1) (:y @point1)
                                 (Math/abs (- (:x @point1) (:x @point2)))
                                 (Math/abs (- (:y @point1) (:y @point2)))))

    (if (and (> (:x @point2) (:x @point1)) (< (:y @point2) (:y @point1)))
      (reset! rect-values (conj [] (:x @point1) (:y @point2)
                                 (Math/abs (- (:x @point1) (:x @point2)))
                                 (Math/abs (- (:y @point1) (:y @point2)))))

      (if (and (< (:x @point2) (:x @point1)) (< (:y @point2) (:y @point1)))
        (reset! rect-values (conj [] (:x @point2) (:y @point2)
                                (Math/abs (- (:x @point1) (:x @point2)))
                                 (Math/abs (- (:y @point1) (:y @point2)))))

        (reset! rect-values (conj [] (:x @point2) (:y @point1)
                                 (Math/abs (- (:x @point1) (:x @point2)))
                                 (Math/abs (- (:y @point1) (:y @point2)))))
      )
    )
  )
)


;;radius function caclcutes the radius of the rectangle base don the distance between two points.

(defn radius[]
  (Math/sqrt (+ (Math/pow (Math/abs (- (:x @point1) (:x @point2))) 2)
                (Math/pow (Math/abs (- (:y @point1) (:y @point2))) 2)))
)


;;This function checks the count of clicks made on the drawing board. If the click is 1 then it activates the drawing
;; and if it is 2nd click then it stores the state.

(defn inc-click-count []
  (swap! click-count inc)
  (if (= (mod @click-count 2) 0)
    (do
      (reset! board-clicked "no")
      (case @active-button
        "line" (do
                 (swap! undo-list conj
                        [:line {:x1 (- (:x @point1) 100) :y1 (- (:y @point1) 85)
                                :x2 (- (:x @point2) 100)   :y2 (- (:y @point2) 85) :key (Math/random)}])
                 (println @undo-list)
                )
        "rect"  (do
                  (swap! undo-list conj
                        [:rect {:x (- (nth @rect-values 0 0) 100) :y (- (nth @rect-values 1 0) 85)
                            :width (nth @rect-values 2 0) :height (nth @rect-values 3 0) :fill "none" :key (Math/random)}])
                  (println @undo-list)
                )
        "circle" (do
                  (swap! undo-list conj
                         [:circle {:cx (- (:x @point1) 100) :cy (- (:y @point1) 85) :r (radius) :fill "none" :key (Math/random)}])
                  (println @undo-list)
                 )
      )
    )
    (reset! board-clicked "yes")
  )
)


;;This function will draw the state history shapes which are stored in undo-list.
(defn draw-old-shapes []
  @undo-list
)


;;This function draws the new lines when the user clicks on the drawing board after selecting line button
(defn draw-new-lines []
  (if (and (= @draw-button "yes") (= @board-clicked "yes"))
    (when (= @active-button "line")
     [:line {:x1 (- (:x @start-point) 100) :y1 (- (:y @start-point) 85)
             :x2 (- (:x @end-point) 100)   :y2 (- (:y @end-point) 85) :key "1"}]
    )
  )
)


;;This function draws the new rectangles when the user clicks on the drawing board after selecting rectangle button
(defn draw-new-rect []
  (if (and (= @draw-button "yes") (= @board-clicked "yes"))
    (when (= @active-button "rect")
      [:rect {:x (- (nth (rectangle-data) 0 0) 100) :y (- (nth (rectangle-data) 1 0) 85)
            :width (nth (rectangle-data) 2 0) :height (nth (rectangle-data) 3 0) :fill "none" :key "2"}]
     )
   )
)


;;This function draws the new circles when the user clicks on the drawing board after selecting circle button
(defn draw-new-circle []
  (if (and (= @draw-button "yes") (= @board-clicked "yes"))
    (when (= @active-button "circle")
      [:circle {:cx (- (:x @start-point) 100) :cy (- (:y @start-point) 85) :r (radius) :fill "none" :key "3"}]
    )
  )
)

;;This function calls in all new diagram drawing functions to render the figure.
(defn draw-new-shapes []
  (list
   (draw-new-lines)
   (draw-new-rect)
   (draw-new-circle))
)

;;This function sets the parameters for the drawing board and defines the on-click and on-mouse-move events for the same.
;;It also validate the schema of the two r/atom variables start-point and end-point
(defn drawing-board []
  [:div {:on-mouse-move #((reset! end-point {:x (.-clientX %) :y (.-clientY %)})
                          (s/validate my-schema-endpoint @end-point)
                          (reset! point2 @end-point))
         :on-click #(if (= @draw-button "yes")
                        (do
                          (if (= (mod @click-count 2) 0)
                           (do
                             (reset! start-point {:x (.-clientX %) :y (.-clientY %)})
                             (s/validate my-schema-startpoint @start-point)
                             (reset! point1 @start-point)
                            )
                          )
                          (inc-click-count)
                        )
                        (println "No drawing button selected!"))}
        [:svg {:width 1200 :height 550 :stroke "black" :stroke-width 3
               :style {:position :fixed :top 85 :left 100 :border "brown solid 3px"}}
              (draw-new-shapes) (draw-old-shapes)]
  ]
)

;;This functions defines all the buttons to be displayed on the screen and their properties.
;;It also decides what happens at the click of each button.
(defn buttons []
  [:div [:button {:on-click (fn myfunc [e]
                              (swap! undo-list rest)
                              (case (first (first @undo-list))
                                :line  (reset! active-button "line")
                                :rect   (reset! active-button "rect")
                                :circle  (reset! active-button "circle")
                                nil (reset! active-button "nil")
                              )
                            )

                  :disabled (empty? @undo-list)}
        [:img {:src "undo.png" :height 20 :width 30}] "Undo"] [:br] [:br]

        [:button {:on-click (fn myfunc [e]
                                (reset! active-button "line")
                                (reset! draw-button "yes")
                                (reset! board-clicked "no")
                                (if-not (= (first (first @undo-list)) :line)
                                (swap! undo-list conj [:line {:key (Math/random)}]))
                                (println @undo-list)
                                )}
        [:img {:src "line.GIF" :height 20 :width 37}] "Line"] [:br] [:br]

        [:button {:on-click (fn myfunc [e]
                                (reset! active-button "rect")
                                (reset! draw-button "yes")
                                (reset! board-clicked "no")
                                (if-not (= (first (first @undo-list)) :rect)
                                (swap! undo-list conj [:rect {:key (Math/random)}]))
                                (println @undo-list)
                              )}
        [:img {:src "rect.GIF" :height 20 :width 33}] "Rect"] [:br] [:br]

        [:button {:on-click (fn myfunc [e]
                                (reset! active-button "circle")
                                (reset! draw-button "yes")
                                (reset! board-clicked "no")
                                (if-not (= (first (first @undo-list)) :circle)
                                (swap! undo-list conj [:circle {:key (Math/random)}]))
                                (println @undo-list)
                              )}
        [:img {:src "circle.png" :height 20 :width 27}] "Circle"] [:br] [:br]

        [:button {:on-click (fn myfunc [e]
                               (reset! active-button "nil")
                               (reset! draw-button "no")
                               (reset! board-clicked "no")
                               (reset! undo-list nil))
                  :disabled (empty? @undo-list)}
        [:img {:src "clear.png" :height 20 :width 30}] "Clear"]
  ]
)

;;This defines the main home page for the applications.
(defn home-page []
 [:div [:h2 "Paint Board"]
   [stateful-with-atom] [buttons] [drawing-board]]
)

;;This tells the clojure script what is to be rendered. The main function that needs to be called to render.
(reagent/render-component [home-page]
                          (. js/document (getElementById "app")))
