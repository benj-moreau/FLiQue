#!/bin/bash
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 S8 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 S10 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 S11 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 C1 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 C8 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 C9 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 C10 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 L2 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 L5 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 L6 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 L7 &
wait
rm cache.db
timeout --signal=SIGKILL $1 java -Xmx50g -jar target/flique-0.0.1-SNAPSHOT.jar $2 $3 L8 &
wait