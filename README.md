# FLiQue: a Federated License aware Query processing strategy

When two or more licensed data sources participate in the evaluation of a federated query, the query result must be protected by a license that is compliant with each license of the involved datasets. 
However, such a license does not always exist, and this leads to a query result that cannot be reused.
We propose to deal with this issue during the federated query processing by discarding datasets of conflicting licenses.
But, a query with an empty result set can be obtained.
To face this problem, we use query relaxation techniques. 
Our problem statement is, **given a SPARQL query and a federation of licensed datasets, how to guarantee a relevant and non-empty query result whose license is compliant with each license of involved datasets?** 
In a distributed environment, the challenge is to limit communication costs when the query relaxation process is necessary. 
we propose FLiQue, a license aware query processing strategy for federated query engines. 
This repository is an implementation of a federated license aware query engine (extension of CostFed).


## Running instructions (same as CostFed)

- Build the project using maven
- Configuration File: Set properties in /flique/flique.props or run with default
- Query Execution: flique/src/main/java/org/ods/start/QueryEvaluation.java. Here you need to specify the URLs of the SPARQL endpoints which you want the given query to be executed.
You need to execute it with the following 3 arguments: (e.g., FLIQUE relax C3) First is to use FLiQue strategy, second is to allow query relaxation and third is the query (in /queries dir) to execute.

## Benchmark: [LargeRDFBench](https://github.com/AKSW/largerdfbench)

All the datasets can be downloaded from the links given below.
To improve query relaxation we advise you tu run your queries on saturated RDF dataset (at least on rdfs:subclassOf and rdfs:subPropertyOf rules)

| *Dataset*  | *Data-dump*  | *Windows Endpoint*  | *Linux Endpoint*  | *Local Endpoint Url*  | *Live Endpoint Url*|
|------------|--------------|---------------------|-------------------|-----------------------|--------------------|
| [LinkedTCGA-A](http://tcga.deri.ie/)  |[Download](https://drive.google.com/file/d/0B_MUFqryVpByd2FVQ2gzOXhIemc/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B_MUFqryVpBydUpVWjBMX2FGNEE/edit?usp=sharing/) | [Download](https://drive.google.com/file/d/0B_MUFqryVpByYXdtbGhNYjdCZ3M/edit?usp=sharing/ ) |your.system.ip.address:8889/sparql | -|
| [ ChEBI](https://www.ebi.ac.uk/chebi/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-Vk81dGVkNVNuY1E/edit?usp=sharing/ )| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-TUR6RF9jX2xoMFU/edit?usp=sharing/)|[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-Wk5LeHBzMUd3VHc/edit?usp=sharing )|your.system.ip.address:8888/sparql | - |
| [DBPedia-Subset](http://DBpedia.org/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-QWk5MVJud3cxUXM/edit?usp=sharing/ )|  [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-WjNkZEZrTTZzbW8/edit?usp=sharing/)|[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-OEgyXzBUVmlMQlk/edit?usp=sharing )|your.system.ip.address:8891/sparql |http://dbpedia.org/sparql |
| [ DrugBank](http://www.drugbank.ca/)|[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-cVp5QV9VUWRuYkk/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-QmMyOE9RWV9oNHM/edit?usp=sharing/ )| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-U0V5Y0xDWXhzam8/edit?usp=sharing/ )|your.system.ip.address:8892/sparql | http://wifo5-04.informatik.uni-mannheim.de/drugbank/sparql |
| [Geo Names](http://www.geonames.org/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-WEZZb2VwOG5vZkU/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-VC1HWmhBMlFncWc/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B_MUFqryVpByd3hJcHBPeHZhejA/edit?usp=sharing/ ) |your.system.ip.address:8893/sparql | http://factforge.net/sparql |
| [Jamendo](http://dbtune.org/jamendo/ ) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-cWpmMWxxQ3Z2eVk/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-YXV6U0ZzLUF0S0k/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-V3JMZjdfRkZxLUU/edit?usp=sharing/ ) |your.system.ip.address:8894/sparql  | http://dbtune.org/jamendo/sparql/ |
| [KEGG](http://www.genome.jp/kegg/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-TUdUcllRMGVJaHM/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-c1BNQ0dVWTVkUEU/edit?usp=sharing/ )| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-R1dKbDlHNXZ6blk/edit?usp=sharing/ ) |your.system.ip.address:8895/sparql |http://cu.kegg.bio2rdf.org/sparql |
| [Linked MDB](http://linkedmdb.org/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-bU5VN25NLXZXU0U/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-eXpVSjd2Y25PaVk/edit?usp=sharing/ )| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-NjVTVERvajJUcGc/edit?usp=sharing/) |your.system.ip.address:8896/sparql |http://www.linkedmdb.org/sparql |
| [New York Times](http://data.nytimes.com/) |[ Download](https://drive.google.com/file/d/0B1tUDhWNTjO-dThoTm9DSmY4Wms/edit?usp=sharing/) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-VDhmNWJmZVcybm8/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-RG9GeVdxbDR4YjQ/edit?usp=sharing/ ) |your.system.ip.address:8897/sparql | - |
| [Semantic Web Dog Food](http://data.semanticweb.org/) |[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-RjBWZXYyX2FDT1E/edit?usp=sharing/ )| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-c2h4al9VREF6bDg/edit?usp=sharing/ ) | [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-UW5HaF9rekdialU/edit?usp=sharing/ ) |your.system.ip.address:8898/sparql | http://data.semanticweb.org/sparql |
| [Affymetrix](http://download.bio2rdf.org/release/2/affymetrix/affymetrix.html)| [Download](https://drive.google.com/file/d/0B1tUDhWNTjO-eHVlZ1RyVVFJQU0/edit?usp=sharing/ )| [ Download](https://drive.google.com/file/d/0B1tUDhWNTjO-RnV4SWtKelJTb0U/edit?usp=sharing/)|[Download](https://drive.google.com/file/d/0B1tUDhWNTjO-Tm9oazNUdV9Cb1k/edit?usp=sharing )|your.system.ip.address:8899/sparql |http://cu.affymetrix.bio2rdf.org/sparql |

To facilitate the initialisation of the benchmark we propose to use docker. The following docker-compose will setup all virtuoso endpoints for you.
[how to load rdf data?](https://hub.docker.com/r/tenforce/virtuoso/)
```yaml
db-tcgaa:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8889"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
  volumes:
    - ./data-tcgaa/virtuoso:/data
  ports:
    - "8889:8889"

db-chebi:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8888"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
  volumes:
    - ./data-chebi/virtuoso:/data
  ports:
    - "8888:8888"

db-dbpedia:
  image: tenforce/virtuoso:1.3.2-virtuoso7.2.5.1
  environment:
    SPARQL_UPDATE: "true"
    DEFAULT_GRAPH: "http://aksw.org/benchmark"
    VIRT_HTTPServer_ServerPort: "8891"
    VIRT_SPARQL_ResultSetMaxRows: "9999999"
    VIRT_SPARQL_MaxQueryCostEstimationTime: "9999999"
    VIRT_SPARQL_MaxQueryExecutionTime: "9999999"
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
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
    VIRT_Parameters_ThreadsPerQuery: "1"
    VIRT_Parameters_NumberOfBuffers: "45000"
    VIRT_Parameters_MaxDirtyBuffers: "34000"
    VIRT_Parameters_MaxQueryMem: "360M"
    VIRT_Parameters_HashJoinSpace: "4M"
  volumes:
    - ./data-affymetrix/virtuoso:/data
  ports:
    - "8899:8899"
```