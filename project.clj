(defproject swag "0.2.7-SNAPSHOT"
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

  :plugins  [[codox "0.6.6"] [lein-tag "0.1.0"] [lein-midje "3.1.1"] [lein-set-version "0.3.0"]]

  :profiles {
    :dev {
       :dependencies [[midje "1.6.0"]]
          
       :set-version {
          :updates [
            {:path "README.md" :search-regex #"\"\d+\.\d+\.\d+\""}]
       }
     }
  }          
  

  :aliases {
      "runtest"  ["midje" ":filter" "-integration"]
   }
)
