#!/bin/bash
# Run from project root
mkdir -p out
javac -d out src/editor/UIEditor.java
java -cp out editor.UIEditor
