(ns com.gfredericks.qubits.objects-test
  (:require [clojure.test :refer :all]
            [com.gfredericks.qubits.complex :as c]
            [com.gfredericks.qubits.objects :refer :all]))

(defn =ish [x y] (< (- x y) 0.0000001))
(def one? #(= 1 %))

(defn probably?
  [q p0 p1]
  (let [p (probabilities q)]
    (and (=ish p0 (p 0))
         (=ish p1 (p 1)))))

(deftest single-qubit-probabilities-tests
  (testing "that qubits start out in the |0> state"
    (qubits [q]
      (is (probably? q 1 0))))
  (testing "that the X gate puts a qubit in the |1> state"
    (qubits [q]
      (X q)
      (is (probably? q 0 1))))
  (testing "that the Z gate doesn't change initial probabilities"
    (qubits [q]
      (Z q)
      (is (probably? q 1 0))))
  (testing "that the Y gate flips the initial probabilities"
    (qubits [q]
      (Y q)
      (is (probably? q 0 1))))
  (testing "that the H gate gives equal probabilities"
    (qubits [q]
      (H q)
      (is (probably? q 1/2 1/2))))
  (testing "that two H gates reverts to initial probabilities"
    (qubits [q]
      (H q)
      (H q)
      (is (probably? q 1 0))))
  (testing "that two H gates with a Z or a Y in between reverses probabilities"
    (are [G] (qubits [q]
               (doto q H Z H)
               (probably? q 0 1))
         Z
         Y))
  (testing "that H Y Z H reverts to initial probabilities"
    (qubits [q]
      (doto q H Y Z H)
      (is (probably? q 1 0)))))

(deftest single-qubit-observation-tests
  (testing "That qubits with single possibilities observe correctly."
    (qubits [q]
      (is (zero? (observe q)))
      (X q)
      (is (one? (observe q)))))
  (testing "That observing a qubit after an H gives sane probabilities."
    (qubits [q]
      (H q)
      (is (case (observe q)
            0 (probably? q 1 0)
            1 (probably? q 0 1)))
      ;; doing this twice as a regression test (a bug failed it)
      (is (case (observe q)
            0 (probably? q 1 0)
            1 (probably? q 0 1))))))

(deftest multiple-qubit-probability-tests
  (testing "that we can entangle two qubits"
    (qubits [a b]
      (H a)
      (X b a)
      (is (probably? a 1/2 1/2))
      (is (probably? b 1/2 1/2))
      (let [va (observe a)]
        (is (case va
              0 (probably? b 1 0)
              1 (probably? b 0 1)))
        (is (= (observe b) va))))))
