# FLiQuE: A Federated and License Aware Query Engine

FLiQuE is a **F**ederated and **Li**cense Aware **Qu**ery **E**ngine.

## Benchmark

```yaml
db-chebi:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8890"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-chebi/virtuoso:/data
  ports:
    - "8890:8890"

db-dbpedia:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8891"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-dbpedia/virtuoso:/data
  ports:
    - "8891:8891"

db-drugbank:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8892"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-drugbank/virtuoso:/data
  ports:
    - "8892:8892"

db-geonames:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8893"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-geonames/virtuoso:/data
  ports:
    - "8893:8893"

db-jamendo:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8894"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-jamendo/virtuoso:/data
  ports:
    - "8894:8894"

db-kegg:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8895"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-kegg/virtuoso:/data
  ports:
    - "8895:8895"

db-linkedmdb:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8896"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-linkedmdb/virtuoso:/data
  ports:
    - "8896:8896"

db-newyorktimes:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8897"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-newyorktimes/virtuoso:/data
  ports:
    - "8897:8897"

db-semanticwebdogfood:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8898"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-semanticwebdogfood/virtuoso:/data
  ports:
    - "8898:8898"

db-affymetrix:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8899"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
  volumes:
    - ./data-affymetrix/virtuoso:/data
  ports:
    - "8899:8899"
```