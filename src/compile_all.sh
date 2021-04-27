#!/bin/sh
if ! [ -d classes ];
then
   mkdir classes
fi
javac -cp . -d classes ir/Engine.java ir/PostingsEntry.java ir/PostingsList.java ir/Query.java ir/Searcher.java ir/SearchGUI.java ir/Option.java
