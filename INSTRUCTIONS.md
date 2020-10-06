## running the harness
```
mvn clean install -DskipTests=true
wrk -t2 -c5  http://127.0.0.1:8080/singleSpan
wrk -t2 -c5 http://127.0.0.1:8080/singleWithChild
```
ramp up threads, concurrency and duration as you load it up, ensure ulimits are set appropriately

