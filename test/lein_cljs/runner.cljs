(ns lein-cljs.runner
  (:require [cljs.test :as test :include-macros true]
            [lein-cljs.core-test]))

(test/run-tests 'lein-cljs.core-test)
