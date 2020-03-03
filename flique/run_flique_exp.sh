#!/bin/bash
for i in `seq 1 $2`;
do
        sh ./run_queries.sh $1 FLIQUE noRelax &
        wait
        sh ./run_queries.sh $1 FLIQUE relax &
        wait
done