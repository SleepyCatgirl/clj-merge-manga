# Clojure merge manga
To merge manga chapters into one, for use with e.g 
Ebook readers and such
# Requirements
- Clojure
- Babashka

That's it.
# Usage
In a folder with folders of chapters of Manga, which have Ch.num (e.g Ch.0005) in the name
1. chmod +x ./merge.clj
2. ./merge.clj
3. Zip files inside newly created Manga folder
4. Profit
# License
See LICENSE.md
# TODO
- [x] Make it simpler in usage
- [x] Cleanup function names
- [ ] Do the zipping in the program itself
- [ ] Make deleting chapters an arguemnt/option?
- [ ] Check for whether images are already in correct 'format'
E.g if Folder 1 has 001.jpg to 005.jpg, and Folder 2 has 006.jpg to 009.jpg
Proceeding with -main as if nothing, would just break things
