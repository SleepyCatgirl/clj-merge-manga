# Clojure merge manga
To merge manga chapters into one, for use with e.g 
Ebook readers and such
# Requirements
- Clojure

That's it.
# Usage
In a folder with folders of chapters of Manga, which have Ch.num (e.g Ch.0005) in the name
1. Run clojure REPL with merge.clj
2. Execute (main chapters)
3. Execute (copy-images-2 chapters)
4. Zip files inside newly created Manga folder
5. Profit
# License
See LICENSE.md
# TODO
- [ ] Make it simpler in usage
- [x] Cleanup function names
- [ ] Do the zipping in the program itself
