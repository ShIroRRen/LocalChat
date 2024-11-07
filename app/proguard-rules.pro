-optimizations !code/allocation/variable,!code/simplification/arithmetic,!field/*,!class/merging/*
-dontskipnonpubliclibraryclassmembers
-dontskipnonpubliclibraryclasses
-allowaccessmodification
-optimizationpasses 11
-overloadaggressively
-ignorewarnings
-dontpreverify
#-dontoptimize
#-verbose

-repackageclasses ''

-obfuscationdictionary obf.txt
-classobfuscationdictionary obf.txt
-packageobfuscationdictionary obf.txt
-renamesourcefileattribute obf.txt

-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents