// HI-MEEPO
// RNA:USE /file/FileName/
// RNA:USE /line1/LineStart/
// RNA:USE /line2/LineEnd/
## file line1-line2

// RNA:USE /project/Project/
// RNA:USE /filepath/FilePath/

// RNA:USE /hash/GitHash/
// RNA:USE /time/ModTime/

// RNA:WHEN /yes/GitHash/bg
project hash
// RNA:ELSE bg
project time
// RNA:DONE bg

filepath

// RNA:USE /text/FileType/
// RNA:USE /code/CodeCopy/
```text
code
```
