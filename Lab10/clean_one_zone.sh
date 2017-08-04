#!/bin/sh
a=$1
a=$(echo $a | tail -c 5)
python src/lab10.py $1
echo "end of script"