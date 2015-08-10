(ns leiningen.cljs
  "Provides a command line wrapper around cljs.build.api and cljs.watch.api"
  (:require [clojure.string :as str]
            [leiningen.core.main :as lmain]
            [leiningen.core.eval :as leval]))


;; Needed to ensure cljsbuild compatibility
(defn make-subproject [project builds]
  (with-meta
    (merge
      (select-keys project [:checkout-deps-shares
                            :dependencies
                            :eval-in
                            :jvm-opts
                            :local-repo
                            :repositories
                            :resource-paths])
      {:local-repo-classpath true
       :source-paths (concat
                       (:source-paths project)
                       (mapcat :source-paths builds))})
    (meta project)))

;; well this is private in the leiningen.cljsbuild ns & figwheel!
(defn run-local-project
  "Runs both forms (requires and form) in the context of the project"
  [project builds requires form]
  (let [project' (-> project
                   (make-subproject builds)
                   ;; just for use inside the plugin
                   (dissoc :eval-in))]
    (leval/eval-in-project project'
      `(try
         (do
           ~form
           (System/exit 0))
         (catch Exception e#
           (do
             (.printStackTrace e#)
             (System/exit 1))))
      requires)))

;; TODO: generate the js-env opts & build-ids dynamically
(def help-string
"\ncljs - compile ClojureScript from lein 
Usage:\n
  lein cljs {watch-mode} {build-id}\n
Where - watch-mode: auto or once
      - build-id: any of the ids under the :cljs map in your project.clj\n")

(defn find-by-id
  "Out of a seq of builds, returns the one with the given id"
  [builds id]
  (first (filter #(= id (:id %)) builds)))

(defn cljs 
  "Command line API for cljs, which compiles your ClojureScript code:

  lein cljs {watch-mode} {build-id}

  - watch-mode: either auto or once,
  - build-id: the build-id from your :cljs map in project.clj"
  ([project] (lmain/info help-string))
  ([project watch-mode]
   ;; TODO: do all
   (lmain/info "You need to specify a build id:\n")
   (lmain/info help-string))
  ([project watch-mode build-id]
   (assert (contains? #{"auto" "once"} watch-mode)
     (str "Possible watch-modes are auto or once, " watch-mode " was given."))
   ;; FIX: execute in a try catch like the one in run-local-project
   (let [builds (-> project :cljs :builds)
         {:keys [source-paths compiler] :as build}
         (find-by-id builds build-id)]
     (assert (not (empty? build))
       (str "The given build (" build-id ") was not found in these options: "
         (str/join ", " (map :id builds))))
     ;; FIX: there is probably a bug regarding the incorrect use of builds
     (run-local-project project [builds]
       '(require 'cljs.build.api)
       (if (= "auto" watch-mode)
         `(cljs.build.api/watch
            (apply cljs.build.api/inputs ~source-paths)
            ~compiler)
         `(cljs.build.api/build
            (apply cljs.build.api/inputs ~source-paths) ~compiler))))))
