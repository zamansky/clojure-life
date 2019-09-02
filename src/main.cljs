(ns main
  (:require [ajax.core :refer [GET POST PUT]]
            [reagent.core :as r]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]
            [life :as l]
            )
  )


;;(def state (r/atom #{[10 10] [10 11] [10 12] [18 18]}))
(def state (r/atom (set {})))
(def running (r/atom true))
(def fill-value (r/atom 500))

(defn fill-random [event]
  (let [
        canvas (.getElementById js/document "c")
        ctx (.getContext canvas "2d")
        slider (.getElementById js/document "density")
        density (.-value slider)
        ] 
    (print density)
    (set! (. ctx -fillStyle) (str "rgb(" 255 "," 255"," 255 ")"))
    (.fillRect ctx 0 0 500 500)
    (let [newcells (for [i (range density)]
                     [(rand-int 100) (rand-int 100)]
                     )
          ]
      (reset! state (set newcells)))))


(defn reset [event]
  (let [
        canvas (.getElementById js/document "c")
        ctx (.getContext canvas "2d")]
    (set! (. ctx -fillStyle) (str "rgb(" 255 "," 255"," 255 ")"))
    (.fillRect ctx 0 0 500 500)
    (reset! state #{})))




(defn click-it [event]
  (let [canvas (.getElementById js/document "c")
        rect (.getBoundingClientRect canvas)
        x (- (.-clientX event) (.-left rect))
        y (- (.-clientY event) (.-top rect))
        newcell [(int (/ x 10)) (int (/ y 10))]
        ]
    (if (contains? @state newcell)

      (do (swap! state #(set(remove (fn [c] (= c newcell)) %)))
          (println (str "REMOVED: " newcell " : " @state)))
      (do (swap! state conj newcell)
          (println (str "Added: " newcell " : " @state)))
      )
    ))





(defn draw-cells [comp]
(println "MOUNTED")
(let [node (r/dom-node comp)
      ctx (.getContext node "2d")
      ]
  (set! (. ctx -fillStyle) (str "rgb(" 255 "," 255"," 255 ")"))
  (.fillRect ctx 0 0 500 500)
  ()
  (set! (. ctx -fillStyle) (str "rgb(" 255 "," 0"," 0 ")"))
  (doseq [ [x y] @state]
    (.fillRect ctx (* 10 x) (* 10 y) 10 10)                                 
    )

  )
)
(defn canvas [state]
  (let [state state]
    (r/create-class
     {:display-name "canvas"
      :component-did-mount draw-cells 
      :component-did-update draw-cells
      :reagent-render (fn  []
                        @state
                        [:canvas {:id "c" :width 500 :height 500 :style {:border "2px solid green"}
                                  :on-click click-it
                                  }]
                        )}
     )))


(defn generate-loop []
  (go-loop []
    (<! (timeout 25))
    (swap! state l/generate)
    (if @running
      (recur)
      )
    
    ))

(defn start-stop-loop []
  (if @running
    (swap! running not)
    (do
      (swap! running not)
      (generate-loop)
      )
    ))

(defn simple-component []
  [:div
   [:h1.title "Life!"]
   [:hr]
   [canvas state]
   [:hr]
   [:div.section
    [:button.button.is-primary {:on-click #(swap! state l/generate)} "Step"]
    [:button.button.is-primary {:on-click reset} "Reset"]
    [:button.button.is-primary {:on-click fill-random} "fill-random"]
    [:button.button.is-primary {:on-click start-stop-loop} "start/stop"]
    [:hr]
    [:p @fill-value]
    [:input#density.slider.is-fullwidth.is-primary {
                                                    :on-change #(reset! fill-value (-> % .-target .-value))
                                                    :type "range" :step 1 :min 500 :max 10000 :value @fill-value}]
    ]
   ])


(defn mount [c]
(r/render-component [c] (.getElementById js/document "app"))
)

(defn reload! []
  (mount simple-component)
  )

(defn main! []
(mount simple-component))
