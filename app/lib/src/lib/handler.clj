(ns lib.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            ;; [ring.middleware.defaults :refer [wrap-defaults site-defaults wrap-resource]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as r]
            [lib.model.model :as dbc]
            [lib.pages.pages :as pages]))


(def conn (dbc/db-connect))


(defn render-not-found []
  (route/not-found
  (pages/render-page-not-found {})))


(defn render-doc [id] 
  (let [doc (dbc/get-doc conn id)]
    (if (some? doc)
      (pages/render-page-doc {:doc doc})
      (render-not-found)
      )
    )
  
  
  
  )

(defn render-search [q]
  (pages/render-page-search {
                             :res (dbc/search conn q)
                             :search-query q
                             }))

(defn render-file [id]
  (let
   [file (dbc/get-file-by-id conn id)]
    
(if (some? file)
  (let [writer (:writer file)
        info (:info file)]
    (->
     (r/response (ring.util.io/piped-input-stream (fn [ostream] (writer ostream))))
     (r/content-type (info "contentType"))
     (r/header "Cache-Control" "no-cache, must-revalidate, post-check=0, pre-check=0")
     (r/header "Content-Disposition" (format "attachment; filename=\"%s\"" (info "original_filename")))))


  (render-not-found))



    
    
    
    )
  
  
  )
  

(defn render-gfs [id]
  (dbc/gfs-test conn id))



(defn render-home []
  (pages/render-page-home {}))


(defroutes app-routes
  (GET "/" [] (render-home))
  (GET "/doc" [id] (render-doc id))
  (GET "/file" [id] (render-file id))
  (GET "/gfs" [id] (render-gfs id))
  (GET "/search" [q] (render-search q))
  ;; (route/not-found (pages/render-page-not-found {}))
  ;; (route/not-found (render-not-found))
  (render-not-found)
  
  )



(def app
  (wrap-defaults (wrap-resource app-routes "public") site-defaults))

;; (def app
;;   (wrap-defaults app-routes site-defaults))
