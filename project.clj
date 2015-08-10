(defproject lein-cljs "0.1.0-SNAPSHOT"
  :description "lein-cljs is a minimal lein plugin for ClojureScript compiler"
  :url "https://github.com/bensu/lein-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :scm {:name "git"
        :url "https://github.com/bensu/lein-cljs"}

  :deploy-repositories [["clojars" {:creds :gpg}]]

  :eval-in-leiningen true
  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48" :scope "provided"]]

  :clean-targets ^{:protect false} [:target-path "out" "resources/public/js/"]

  :cljs
  {:builds [{:id "dev"
             :source-paths ["src" "test"]
             :main 'lein-cljs.core-test
             :compiler {:output-to "resources/public/js/testable.js"
                        :optimizations :none}}
            {:id "test"
             :source-paths ["src" "test"]
             :compiler {:output-to "resources/public/js/testable.js"
                        :main 'lein-cljs.runner
                        :optimizations :whitespace}}]})
