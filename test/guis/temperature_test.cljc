(ns guis.temperature-test
  (:require [clojure.test :refer [deftest testing is run-tests]]
            [guis.temperature :as temperature]))

(deftest fahrenheit->celsius
  (testing "Converts to celsius"
    (is (= (temperature/fahrenheit->celsius 32) 0))
    (is (= (temperature/fahrenheit->celsius 122) 50))
    (is (= (temperature/fahrenheit->celsius 212) 100))))

(deftest celsius->fahrenheit
  (testing "Converts to celsius"
    (is (= (temperature/celsius->fahrenheit 0) 32))
    (is (= (temperature/celsius->fahrenheit 50) 122))
    (is (= (temperature/celsius->fahrenheit 100) 212))))

(run-tests)
