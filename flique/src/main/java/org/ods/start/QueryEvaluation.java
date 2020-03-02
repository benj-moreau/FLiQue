package org.ods.start;

import com.fluidops.fedx.*;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.SourceSelection;
import com.fluidops.fedx.structures.QueryInfo;
import org.aksw.simba.start.QueryProvider;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.ods.core.license.LicenseChecker;
import org.ods.core.relaxation.QueryRelaxationLattice;
import org.ods.core.relaxation.RelaxedQuery;
import org.ods.core.relaxation.strategy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class QueryEvaluation {
    protected static final Logger log = LoggerFactory.getLogger(QueryEvaluation.class);
    protected static Model ontology = RDFDataMgr.loadModel("ontologies/ontology.n3");
    protected static Model summary = RDFDataMgr.loadModel("summaries/saturated-largeRDFBench-summaries.n3");
    protected static Model licensedSummary = RDFDataMgr.loadModel("summaries/largeRDFBench.n3");
    protected static  final double minSimilarity = 0.0;
    private HashMap<String, String> results = new HashMap<>();
    private HashMap<String, String> portEndpoints = new HashMap<>();
    private int nbFed = 0;
    private long licenseCheckTime;
    private long startQueryExecTime = 0;
    private String strategy;
    private QueryRelaxer queryRelaxer;
    private Boolean relax;
    private Boolean error;
    static {
        try {
            ClassLoader.getSystemClassLoader().loadClass("org.slf4j.LoggerFactory"). getMethod("getLogger", ClassLoader.getSystemClassLoader().loadClass("java.lang.String")).
                    invoke(null,"ROOT");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    QueryProvider qp;

    public QueryEvaluation(String strategy,QueryRelaxer queryRelaxer, Boolean relax) throws Exception {
        qp = new QueryProvider("../queries/");
        this.results.put("Query", null);
        this.results.put("LicenseCheckTime", null);
        this.results.put("FirstResultTime", null);
        this.results.put("nbFederations", null);
        this.results.put("totalExecTime", null);
        this.results.put("nbGeneratedRelaxedQueries", "0");
        this.results.put("nbEvaluatedRelaxedQueries", "0");
        this.results.put("ResultSimilarity", "0.0");
        this.results.put("validResult", "false");
        //endpoints
        this.portEndpoints.put("8881", "D1");
        this.portEndpoints.put("8882", "D2");
        this.portEndpoints.put("8883", "D3");
        this.portEndpoints.put("8889", "LinkedTCGA-A");
        this.portEndpoints.put("8888", "ChEBI");
        this.portEndpoints.put("8891", "DBPedia-Subset");
        this.portEndpoints.put("8892", "DrugBank");
        this.portEndpoints.put("8893", "Geo Names");
        this.portEndpoints.put("8894", "Jamendo");
        this.portEndpoints.put("8895", "KEGG");
        this.portEndpoints.put("8896", "Linked MDB");
        this.portEndpoints.put("8897", "New York Times");
        this.portEndpoints.put("8898", "Semantic Web Dog Food");
        this.portEndpoints.put("8899", "Affymetrix");
        this.strategy = strategy;
        this.queryRelaxer = queryRelaxer;
        this.relax = relax;
        this.error = false;
        if (strategy.equals("PAPER")) {
            this.ontology = RDFDataMgr.loadModel("ontologies/paper_ontology.n3");
            this.summary = RDFDataMgr.loadModel("summaries/saturated-paper-summary.n3");
            this.licensedSummary = RDFDataMgr.loadModel("summaries/paper_licensed_summary.n3");
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        String cfgName;
        String strategy = args[0];
        Boolean relax = true;
        if (args[1].equals("noRelax")) { relax = false;}
        strategy = strategy.toUpperCase();
        QueryRelaxer queryRelaxer;
        if (strategy.equals("BFS")) {
            cfgName = "fedx.props";
            queryRelaxer = new BFSQueryRelaxer();
        } else if (strategy.equals("OBFS")) {
            cfgName = "fedx.props";
            queryRelaxer = new OBFSQueryRelaxer();
        } else if (strategy.equals("OMBS")) {
            cfgName = "fedx.props";
            queryRelaxer = new OMBSQueryRelaxer();
        } else {
            // default case
            cfgName = "paper_example.props";
            if (!strategy.equals("PAPER")) {
                strategy = "FLIQUE";
                cfgName = "flique.props";
            }
            queryRelaxer = new FLIQUEQueryRelaxer();
        }
        String host = "localhost";
        String queries = args[2];
        // String queries = "S1";
        // String queries = "S1 S2 S3 S4 S5 S6 S7 S8 S9 S10 S11 S12 S13 S14 C1 C2 C3 C4 C5 C6 C7 C8 C9 C10 L1 L2 L3 L4 L5 L6 L7 L8 CH1 CH2 CH3 CH4 CH5 CH6 CH7 CH8";


        ArrayList<String> endpointsMin2 = new ArrayList<>(Arrays.asList(
                "http://" + host + ":8888/sparql",
                "http://" + host + ":8891/sparql",
                "http://" + host + ":8892/sparql",
                "http://" + host + ":8893/sparql",
                "http://" + host + ":8894/sparql",
                "http://" + host + ":8895/sparql",
                "http://" + host + ":8896/sparql",
                "http://" + host + ":8897/sparql",
                "http://" + host + ":8898/sparql",
                "http://" + host + ":8899/sparql",
                "http://" + host + ":8889/sparql"
        ));

        ArrayList<String> endpointsPaper = new ArrayList<>(Arrays.asList(
                "http://" + host + ":8881/sparql",
                "http://" + host + ":8882/sparql",
                "http://" + host + ":8883/sparql"
        ));
        ArrayList<String> endpoints = endpointsMin2;
        if (strategy.equals("PAPER")) {
            endpoints = endpointsPaper;
        }
        multyEvaluate(queries, 1, cfgName, endpoints, strategy, queryRelaxer, relax);
        System.exit(0);
    }

    public void evaluate(String queries, String cfgName, ArrayList<String> endpoints) throws Exception {
        List<String> qnames = Arrays.asList(queries.split(" "));
        String queryFileName;
        String fedName;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date();
        String execFileName = "results/" + formatter.format(date) + this.strategy + "relax_" + this.relax +".csv";
        BufferedWriter ExecutionWriter = new BufferedWriter(new FileWriter(execFileName));
        // CSV header
        ExecutionWriter.write(String.join(";", this.results.keySet()) + "\n");
        ExecutionWriter.flush();
        for (String curQueryName : qnames)
        {
            this.nbFed = 0;
            this.results.put("Query", curQueryName);
            fedName = "Federation " + curQueryName;
            execute(curQueryName, cfgName, endpoints, fedName);
            long totalExecTime = System.currentTimeMillis() - this.startQueryExecTime;
            log.info(curQueryName + ": Query execution time (msec): "+ totalExecTime);
            this.results.put("nbFederations", Integer.toString(this.nbFed));
            this.results.put("totalExecTime", Long.toString(totalExecTime));
            this.results.put("LicenseCheckTime", Long.toString(this.licenseCheckTime));
            ExecutionWriter.write(String.join(";", this.results.values()) + "\n");
            ExecutionWriter.flush();
        }
        ExecutionWriter.close();
    }

    public void execute(String curQueryName, String cfgName, ArrayList<String> endpoints, String fedName) throws Exception {
            // We only search for the first result
            if (this.nbFed == 0) {
                this.startQueryExecTime = System.currentTimeMillis();
            }
            String curQuery = qp.getQuery(curQueryName);
            Config config = new Config(cfgName);
            SailRepository repo = null;
            TupleQueryResult res = null;
            try {
                repo = FedXFactory.initializeSparqlFederation(config, endpoints);
                TupleQuery query = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, curQuery);
                res = query.evaluate();
                // This is where FLiQuE is inserted
                QueryInfo queryInfo = QueryInfo.queryInfo.get();
                SourceSelection sourceSelection = queryInfo.getSourceSelection();
                Map<StatementPattern, List<StatementSource>> stmtToSources = sourceSelection.getStmtToSources();
                log.info("--" + fedName + ":\n");
                log.info(portsToDatasets(stmtToSources.toString()) + "\n\n");
                // 1. Check licenses
                if (this.nbFed == 0) {
                    this.licenseCheckTime = System.currentTimeMillis();
                }
                LicenseChecker licenseChecker = new LicenseChecker(this.licensedSummary);
                EndpointManager endpointManager = queryInfo.getFedXConnection().getEndpointManager();
                Set<String> consistentLicenses = licenseChecker.getConsistentLicenses(sourceSelection, endpointManager);
                if (nbFed == 0) {
                    this.licenseCheckTime = System.currentTimeMillis() - this.licenseCheckTime;
                }
                this.nbFed += 1;
                if (relax) {
                    if (consistentLicenses.isEmpty() && relax) {
                        // a license compatible with licenses of sources does not exists
                        // We need to eliminate sources
                        licenseChecker.getEndpointlicenseConflicts();
                        ArrayList<ArrayList> listSourcesToRemove = licenseChecker.getSourcesToRemove();
                        //remove endpoints
                        // Collections.reverse(listSourcesToRemove);
                        for (ArrayList<String> sourcesToRemove : listSourcesToRemove) {
                            ArrayList<String> newEndpoints = new ArrayList<>(endpoints);
                            newEndpoints.removeAll(sourcesToRemove);
                            fedName += "'";
                            log.info("We removed the following sources: " + endpointsToDatasets(sourcesToRemove.toString()) + " in " + fedName + ".\n");
                            // recursive call.. we restart a query execution with the new federation
                            execute(curQueryName, cfgName, newEndpoints, fedName);
                        }
                        if (null != res) {
                            res.close();
                        }
                        repo.shutDown();
                        return;
                    }
                }
                if (this.results.get("FirstResultTime") == null || this.error) {
                    // Here, we resolved all license conflicts
                    int nbGeneratedRelaxedQueries = 0;
                    int nbEvaluatedRelaxedQueries = 0;
                    double ResultSimilarity = 0.0;
                    this.queryRelaxer.setRepo(repo);
                    RelaxedQuery relaxedQuery = new RelaxedQuery();
                    QueryFactory.parse(relaxedQuery, curQuery, null, null);
                    relaxedQuery.initOriginalTriples();
                    if (relax) {
                        QueryRelaxationLattice relaxationLattice = new QueryRelaxationLattice(relaxedQuery, ontology, summary, stmtToSources, minSimilarity, this.queryRelaxer, endpoints);
                        log.info("--------Evaluated Relaxed Queries:-----------\n");
                        res = relaxedQuery.mayHaveAResult(repo);
                        if (res == null) {
                            while (relaxationLattice.hasNext()) {
                                relaxedQuery = relaxationLattice.next();
                                nbGeneratedRelaxedQueries += 1;
                                log.info(relaxedQuery.toString());
                                if (relaxedQuery.needToEvaluate()) {
                                    nbEvaluatedRelaxedQueries += 1;
                                    res = relaxedQuery.mayHaveAResult(repo);
                                    if (res != null) {
                                        log.info(" This query may have 1 result (SourceSelection) !\n");
                                        if (res.hasNext()) {
                                            log.info(" This query has a result !\n\n");
                                            ResultSimilarity = relaxedQuery.getSimilarity();
                                            nbGeneratedRelaxedQueries += relaxationLattice.sizeOfRemaining();
                                            break;
                                        }
                                    }
                                    log.info(" This query has no result\n\n");
                                }
                            }
                            nbGeneratedRelaxedQueries += relaxationLattice.sizeOfRemaining();
                        }
                        queryInfo = QueryInfo.queryInfo.get();
                        sourceSelection = queryInfo.getSourceSelection();
                        endpointManager = queryInfo.getFedXConnection().getEndpointManager();
                        consistentLicenses = licenseChecker.getConsistentLicenses(sourceSelection, endpointManager);
                    }
                    // we found a query that return at least 1 result.
                    this.results.put("nbGeneratedRelaxedQueries", Integer.toString(Integer.parseInt(this.results.get("nbGeneratedRelaxedQueries")) + nbGeneratedRelaxedQueries));
                    this.results.put("nbEvaluatedRelaxedQueries", Integer.toString(Integer.parseInt(this.results.get("nbEvaluatedRelaxedQueries")) + nbEvaluatedRelaxedQueries));
                    this.results.put("ResultSimilarity", Double.toString(Math.max(Double.parseDouble(this.results.get("ResultSimilarity")), ResultSimilarity)));
                    // Now we can execute the query with FedX
                    // TODO Uncomment next to execute query
                    if (res != null) {
                        this.results.put("validResult", "true");
                        BindingSet row = res.next();
                        log.info("First result of the query is:");
                        log.info(row.toString());
                        long FirstResultTime = System.currentTimeMillis() - this.startQueryExecTime;
                        this.results.put("FirstResultTime", Long.toString(FirstResultTime));
                        // only one result
                    } else {
                        log.info("Final query has no result !");
                    }
                    log.info(this.results.toString());
                    log.info(curQueryName + ": Query result have to be protected with one of the following licenses:" + licenseChecker.getLabelLicenses(consistentLicenses) + "\n");
                }
            } catch (Throwable e) {
                long FirstResultTime = System.currentTimeMillis() - this.startQueryExecTime;
                this.results.put("FirstResultTime", Long.toString(FirstResultTime));
                e.printStackTrace();
                log.error("", e);
                if (this.results.get("validResult").equals("false")) {
                    File f = new File("results/" + curQueryName + " " + strategy + "relax_" + this.relax + ".error.txt");
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(os);
                    e.printStackTrace(ps);
                    ps.flush();
                    FileUtils.write(f, os.toString("UTF8"));
                    this.error = true;
                }
            } finally {
                if (null != res) {
                    res.close();
                }
                if (null != repo) {
                    repo.shutDown();
                }
            }
    }

    static void multyEvaluate(String queries, int num, String cfgName, ArrayList<String> endpoints, String strategy, QueryRelaxer queryRelaxer, Boolean relax) throws Exception {
        QueryEvaluation qeval = new QueryEvaluation(strategy, queryRelaxer, relax);
        for (int i = 0; i < num; ++i) {
            qeval.evaluate(queries, cfgName, endpoints);
        }
    }

    public String portsToDatasets(String stmtToSources){
        for (Map.Entry<String, String> entry : this.portEndpoints.entrySet()) {
            String port = entry.getKey();
            String endpointName = entry.getValue();
            String stmtSource = "id=sparql_localhost:" + port + "_sparql, type=REMOTE";
            stmtToSources = stmtToSources.replace(stmtSource, endpointName);
        }
        return stmtToSources;
    }

    public String endpointsToDatasets(String endpoints){
        for (Map.Entry<String, String> entry : this.portEndpoints.entrySet()) {
            String port = entry.getKey();
            String endpointName = entry.getValue();
            String endpoint = "http://localhost:" + port + "/sparql";
            endpoints = endpoints.replace(endpoint, endpointName);
        }
        return endpoints;
    }

    public void setStrategy (String strategy) {
        this.strategy = strategy;
    }
}