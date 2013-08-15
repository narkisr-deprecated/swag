(defproject swag-example "0.1.0"
  :description "The swag library example project"
  :url ""
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [swag "0.2.2"]
                 [ring-middleware-format "0.3.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring "1.2.0"]
                 [compojure "1.1.5" :exclusions  [ring/ring-core]]]
  
  :main swag.example
  )
