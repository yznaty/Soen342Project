#!/bin/bash
# compile and run

mkdir -p out
find src -name "*.java" | xargs javac -d out
jar cfe task-manager.jar com.taskmanager.Main -C out .
java -jar task-manager.jar
