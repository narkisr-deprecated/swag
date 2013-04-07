(defproject swag "0.2.0"
  :description "A DSL for documenting Compojure routes using Swagger spec"
  :url "https://github.com/narkisr/swag"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5" :exclusions  [ring/ring-core]]
                 [org.clojure/core.incubator "0.1.2"]
                 [org.flatland/useful "0.9.5"]
                 [ring "1.1.8"]]
  :exclusions  [org.clojure/clojure] 

  :plugins  [[codox "0.6.4"]]
)
