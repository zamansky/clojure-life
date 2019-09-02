(ns life)

;; (def state #{ [2 1] [2 2] [2 3] })




(def neigbordeltas (for [x [-1 0 1] y [-1 0 1]
                         :when (not= 0 x y)] [x y]))

(def cell-bloc-3x3 (for [x [-1 0 1] y [-1 0 1]] [x y]))

(defn alive? [[cx cy :as cell] state]
  "Returns: true if the cell will be alive in the next generation,
  false otherwise"
  (let [living? (contains? state cell)
        ;; calc the # of living neighbors
        n (->>
           ;; make a set of all neigbors
           (map (fn [[dx dy]] [(+ dx cx) (+ dy cy)] ) neigbordeltas)
           ;; see how many of them are alive
           (map #(contains? state %))
           (filter #(true? %))
           count)]
    (cond
      (and living? (or (= n 2) (= n 3))) true
      (and (not living?) (= n 3)) true
      :else false)))

(defn generate [state]
  "Generate and return the next state"
  (let [potential-cells (for [[x y] state [dx dy] cell-bloc-3x3]
                          [(+ x dx) (+ y dy)])
        next-state (for [cell potential-cells]
                     (if (alive? cell state) cell))]
    (into #{} (remove nil?(into #{} next-state)))))
