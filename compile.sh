#!/bin/bash
set -x

working_dir="$(pwd)"
sources_dir="$working_dir/src/test/resources"
jar_location="$working_dir/target/Projekt3-1.0-SNAPSHOT-jar-with-dependencies.jar"
ll_file=${1/p3/'ll'}

rm ${sources_dir}/*.ll ${sources_dir}/*.s program

java -Duser.dir=${sources_dir} -jar ${jar_location} $1 $ll_file

#opt-3.6 -S ${sources_dir}/$ll_file -o ${sources_dir}/$ll_file

llc-3.6 ${sources_dir}/$ll_file

clang ${sources_dir}/${1/p3/'s'} -o program
