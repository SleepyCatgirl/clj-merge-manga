#!/usr/bin/env bb
(require '(clojure.java.io))
;; Regex used for checking file name
(def re #".*Ch.*")


;; List all folders/files
;; Into an array
(def folder-list
           (into [] (.list
                     (clojure.java.io/file "./"))))

;; Check if file is dir
(defn is-dir [fl]
              (if (.isDirectory
                   (clojure.java.io/file fl))
                fl
                nil))
;; Filter out any file name that is not related
;; remove empty, and sort
(def chapters
  (sort
   (remove nil?
           (map
            is-dir
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
(def format-file
  (if (empty? chapters)
    "jpg"
    (re-find #"[a-zA-Z]*$"
             (first (list-images (first chapters))))))
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



;; Renaming not nice folders e.g Folder 1 -> Folder 01
;; For sorting reasons
;; Folder names
(defn folder-name []
  (re-find #"[a-zA-Z]*" (first chapters)))
(def folder-n
  (if (empty? chapters) nil
       (folder-name)))

(defn ch-numbers [n]
  (filter #(= (count %) 1)
          (map #(apply str %)
               (map list 
                    (lazy-seq 
                     (range 1 (+ 1 n)))))))

;; Fix chapters if 1-9 chapters dont have leading 0
;; which leads to order issue
(def to-rename (map #(str folder-n " " "0" %) (ch-numbers 10)))
;; TODO Better regex for this
(def to-renamed (remove nil? (map #(re-matches #"^.*[^0-9][0-9]" %) chapters)))
(def map-to-rename (map list to-renamed to-rename))
(defn fix-chapter-sort []
  (doseq [[x y] map-to-rename]
    (re-name x y)))

;; TODO merge with previous
;; And make it conditional
;; like with num->image
;; if the issue is worse
;; E.g goes beyond Ch 100
(defn ch-numbers-2 [chapters]
  (filter #(= (count %) 2)
          (re-seq #"[0-9]+"
                  (apply str chapters))))
(def to-rename-2 (map #(str folder-n " " "0" %) (ch-numbers-2 chapters)))
(def to-renamed-2 (remove nil? (map #(re-matches #"^.*[^0-9][0-9][0-9]" %) chapters)))
(def map-to-rename-2 (map list to-renamed-2 to-rename-2))
(defn fix-chapter-sort-2 []
  (doseq [[x y] map-to-rename-2]
    (re-name x y)))

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


;; Check for whether images are already in correct order
;; [x y] (map list chapters (rest chapters))
;; -> ((Flight 01 Flight 02), (Flight 02 Flight 03))...
(defn check-folder [arr]
  (let [n (atom false)]
    (if (empty? chapters)
      (reset! n false)
        (doseq [[x y] (map list chapters (rest chapters))]
          (if
              (= (incr-string (first (list-images y)) 0)
                 (incr-string (first (reverse (list-images x))) 1))
            nil
            (reset! n true))))
      @n))

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
                   "-f") (fix-chapter-sort)
                  (=
                   (first args)
                   "-h") (println "-d : Move files and delete folders
-c : Copy files and dont delete
-f : Rename Chapter 1 e.g to Chapter 01
-h : Print help")
                  :else (-main))))
(if (empty? chapters)
  (println "No manga")
  (if (check-folder chapters)
    (-main *command-line-args*)
    (println "Either no manga or wrong num")))
