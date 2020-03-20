#!/bin/bash
for i in `seq 1 $2`;
do
        sh ./run_relaxed_queries.sh $1 BFS relax &
        wait
        sh ./run_relaxed_queries.sh $1 OBFS relax &
        wait
        sh ./run_relaxed_queries.sh $1 OMBS relax &
        wait
        sh ./run_relaxed_queries.sh $1 FLIQUE relax &
        wait
done