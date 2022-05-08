#!/usr/bin/env bb
(ns merge.core)
(require '(clojure.java.io))
;; Regex used for checking file name
(def re #".*[0-9]")

;; List all folders/files
;; Into an array
(def folder-list
           (into [] (.list
                     (clojure.java.io/file "./"))))
;; Filter out any file name that is not related
;; remove empty, and sort
(def chapters
  (sort
   (remove nil?
           (map
            #(re-matches re %)
            folder-list))))
;; list images from chapter folder-name
(defn list-images [folder-name]
  (into []
        (sort
         (.list (clojure.java.io/file folder-name)))))
;; Find the last/biggest num from chpater folder-name
(defn find-big [folder-name]
  (apply max
         (map
          #(Integer/parseInt (re-find #"[0-9]*" %))
          (list-images folder-name))))
;; Format of images
;; TODO Handle when regex finds nothing
(def format-file (re-find #"[a-zA-Z]*$"
                     (first (list-images (first chapters)))))
;; Num -> image
;; e.g 54 -> 54.jpg
(defn num->image [n]
  (cond
    (> n 999) (str n \. format-file)
    (> n 99) (str "0" n \. format-file)
    (> n 9) (str "00" n \. format-file)
    :else (str "000" n \. format-file)))
;; Incr string number
;; e.g 33.jpg 5 -> 38.jpg
(defn incr-string [string n]
  (num->image
   (+ 
    (Integer/parseInt
     (re-find #"[0-9]*" string))
    n)))
;; helper fn to rename
(defn re-name [old-path new-path]
  (.renameTo (clojure.java.io/file old-path)
             (clojure.java.io/file new-path)))
;; Helper fn to add name of folder to path to num
(defn add-chapter [folder-name string]
  (str \. \/ folder-name \/ string)) 
;; Check if exists
;; example
;; (.exists (clojure.java.io/file (str (first chapters) \/ "33.jpg")))



;; copy images to folder
(defn copy-images-helper [images folder-source folder-dest]
  (doseq [x images]
    (clojure.java.io/copy
     (clojure.java.io/file (add-chapter folder-source x))
     (clojure.java.io/file (add-chapter "Manga" x)))))

;; Given array of names, copy all of whats inside them to "./Manga"
(defn copy-images [arr]
  (do
    (.mkdir (clojure.java.io/file "./Manga"))
    (doseq [x arr]
      (do
        (copy-images-helper (list-images x) x "./Manga")))))

;; Instead of copying, move
(defn move-images-helper [images folder-source folder-dest]
  (doseq [x images]
    (.renameTo
     (clojure.java.io/file (add-chapter folder-source x))
     (clojure.java.io/file (add-chapter "Manga" x)))))
(defn move-images [arr]
  (do
    (.mkdir (clojure.java.io/file "./Manga"))
    (doseq [x arr]
      (do
        (move-images-helper (list-images x) x "./Manga")))))
;; Since there we move, we delete Folders
(defn delete-files [arr]
  (doseq [x arr]
    (do (.delete (clojure.java.io/file x)))))



;; incr images in chapter folder-name by n
;; might be safer to do 'loop'?
(defn incr-string-images [folder-name n]
  (doseq [x (reverse (list-images folder-name))]
    (re-name (add-chapter folder-name x)
             (add-chapter folder-name (incr-string x n)))))
;; doseq over list of chapters, renaming subsequent images
(defn main-rename [arr]
  (let [n (atom 0)]
      (doseq [x arr]
        (do
          (incr-string-images x @n)
          (reset! n (find-big x))))))



;; TODO Handle when folders dont exist
(defn -main
  ([] (do
       (main-rename chapters)
       (move-images chapters)
       (delete-files chapters)))
  ([args] (cond (=
                   (first args)
                   "-d") (-main)
                  (=
                   (first args)
                   "-c") (do
                           (main-rename chapters)
                           (copy-images chapters))
                  (=
                   (first args)
                   "-h") (println "-d : Move files and delete folders\n
-c : Copy files and dont delete\n
-h : Print help")
                  :else (-main))))

(-main *command-line-args*)
