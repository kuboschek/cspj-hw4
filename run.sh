#!/bin/bash
jar_file=jsonProcessing-2.0.jar
search_dir=data-json
files=

for entry in `ls $search_dir`; do
    files="$files $search_dir/$entry"
done

java -server -jar $jar_file $files
