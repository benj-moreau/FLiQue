package org.ods.start;

import com.fluidops.fedx.*;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.SourceSelection;
import com.fluidops.fedx.structures.QueryInfo;
import org.aksw.simba.start.QueryProvider;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.ods.core.license.LicenseChecker;
import org.ods.core.relaxation.QueryRelaxationLattice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class QueryEvaluation {
	protected static final Logger log = LoggerFactory.getLogger(QueryEvaluation.class);
	protected static final Model ontology = RDFDataMgr.loadModel("ontologies/ontology.n3");
	protected static final Model summary = RDFDataMgr.loadModel("summaries/saturated-largeRDFBench-summaries.n3");
	private HashMap<String, String> results = new HashMap<>();
	private HashMap<String, String> portEndpoints = new HashMap<>();
	private int nbFed = 0;
	private long licenseCheckTime;
	private long startQueryExecTime = 0;
	static {
		try {
			ClassLoader.getSystemClassLoader().loadClass("org.slf4j.LoggerFactory"). getMethod("getLogger", ClassLoader.getSystemClassLoader().loadClass("java.lang.String")).
			 invoke(null,"ROOT");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	QueryProvider qp;

	public QueryEvaluation() throws Exception {
		qp = new QueryProvider("../queries/");
		this.results.put("Query", null);
		this.results.put("LicenseCheckTime", null);
		this.results.put("FirstResultTime", null);
		this.results.put("nbFederations", null);
		this.results.put("totalExecTime", null);
		//endpoints
		this.portEndpoints.put("8889", "LinkedTCGA-A");
		this.portEndpoints.put("8890", "ChEBI");
		this.portEndpoints.put("8891", "DBPedia-Subset");
		this.portEndpoints.put("8892", "DrugBank");
		this.portEndpoints.put("8893", "Geo Names");
		this.portEndpoints.put("8894", "Jamendo");
		this.portEndpoints.put("8895", "KEGG");
		this.portEndpoints.put("8896", "Linked MDB");
		this.portEndpoints.put("8897", "New York Times");
		this.portEndpoints.put("8898", "Semantic Web Dog Food");
		this.portEndpoints.put("8899", "Affymetrix");
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		String cfgName = args[0];
		// Config file
		String repfile = args.length > 1 ? args[1] : null;
		// Not running FLiQuE if the following is true (false by default)
		Boolean CostFedExec = args.length > 2 ? false : true;
		
		String host = "localhost";
		String queries = "S1 S2 S3 S4 S5 S6 S7 S8 S9 S10 S11 S12 S13 S14 C1 C2 C3 C4 C6 C7 C8 C9 C10 C1 C3 C5 C6 C7 C8 C9 C10 L1 L2 L3 L4 L5 L6 L7 L8 CH1 CH2 CH3 CH4 CH5 CH6 CH7 CH8";
	
		List<String> endpointsMin2 = Arrays.asList(
			 "http://" + host + ":8890/sparql",
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
		);
		
		List<String> endpoints = endpointsMin2;
		
		multyEvaluate(queries, 1, cfgName, endpoints);
		System.exit(0);
	}
	
	public void evaluate(String queries, String cfgName, List<String> endpoints) throws Exception {
		List<String> qnames = Arrays.asList(queries.split(" "));
		String queryFileName;
		String fedName;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		Date date = new Date();
		String execFileName = "results/" + formatter.format(date) + "_execution-times.csv";
		BufferedWriter ExecutionWriter = new BufferedWriter(new FileWriter(execFileName));
		// CSV header
		ExecutionWriter.write(String.join(";", this.results.keySet()) + "\n");
		for (String curQueryName : qnames)
		{
			this.nbFed = 0;
			this.results.put("Query", curQueryName);
			date = new Date();
			fedName = "Federation " + curQueryName;
			queryFileName = "results/" + formatter.format(date) + "_" + curQueryName + "-results.txt";
			BufferedWriter queryWriter = new BufferedWriter(new FileWriter(queryFileName));
			execute(curQueryName, cfgName, endpoints, queryWriter, fedName);
			long totalExecTime = System.currentTimeMillis() - this.startQueryExecTime;
			log.info(curQueryName + ": Query execution time (msec): "+ totalExecTime);
			queryWriter.close();
			this.results.put("nbFederations", Integer.toString(this.nbFed));
			this.results.put("totalExecTime", Long.toString(totalExecTime));
			this.results.put("LicenseCheckTime", Long.toString(this.licenseCheckTime));
			ExecutionWriter.write(String.join(";", this.results.values()) + "\n");
		}
		ExecutionWriter.close();
	}

	public void execute(String curQueryName, String cfgName, List<String> endpoints,BufferedWriter writer, String fedName) throws Exception {
		if (this.nbFed == 0) { this.startQueryExecTime = System.currentTimeMillis(); }
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
			writer.write("--" + fedName + ":\n");
			writer.write(portsToDatasets(stmtToSources.toString()) + "\n\n");
			// 1. Check licenses
			LicenseChecker licenseChecker = new LicenseChecker("summaries/largeRDFBench.n3");
			EndpointManager endpointManager = queryInfo.getFedXConnection().getEndpointManager();
			Set<String> consistentLicenses = licenseChecker.getConsistentLicenses(sourceSelection, endpointManager);
			if (nbFed == 0) {
				this.licenseCheckTime = System.currentTimeMillis() - this.startQueryExecTime;
			}
			this.nbFed += 1;
			if (consistentLicenses.isEmpty()) {
				// a license compatible with licenses of sources does not exists
				// We need to eliminate sources
				licenseChecker.getEndpointlicenseConflicts();
				ArrayList<ArrayList> listSourcesToRemove = licenseChecker.getSourcesToRemove();
				//remove endpoints
				for (ArrayList<String> sourcesToRemove : listSourcesToRemove) {
					ArrayList<String> newEndpoints = new ArrayList<>(endpoints);
					newEndpoints.removeAll(sourcesToRemove);
					fedName += "'";
					writer.write("We removed the following sources: " + endpointsToDatasets(sourcesToRemove.toString()) + " in " + fedName + ".\n");
					// recursive call.. we restart a query execution with the new federation
					execute(curQueryName, cfgName, newEndpoints, writer, fedName);
				}
				return;
			}
			// Here, we resolved all license conflicts
			QueryRelaxationLattice relaxationLattice;
			while (!res.hasNext()) {
				relaxationLattice = new QueryRelaxationLattice(curQuery, ontology, summary);
				log.info("RESULTATS VIDES... IL FAUDRA RELACHER LA REQUETE");
				break;
			}

			// Now we can execute the query with FedX
			long count = 0;
			// TODO Uncomment next to execute query
			while (res.hasNext()) {
				BindingSet row = res.next();
				System.out.println(count+": "+ row);
				count++;
			}
			writer.write(curQueryName + ": Query result have to be protected with one of the following licenses:" + licenseChecker.getLabelLicenses(consistentLicenses) + "\n");
			log.info(curQueryName + ": Query result have to be protected with one of the following licenses:" + licenseChecker.getLabelLicenses(consistentLicenses));
		} catch (Throwable e) {
			e.printStackTrace();
			log.error("", e);
			File f = new File("results/" + curQueryName + ".error.txt");
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			ps.flush();
			FileUtils.write(f, os.toString("UTF8"));
		} finally {
			if (null != res) {
				res.close();
			}

			if (null != repo) {
				repo.shutDown();
			}
		}
	}
	
	static void multyEvaluate(String queries, int num, String cfgName, List<String> endpoints) throws Exception {
		QueryEvaluation qeval = new QueryEvaluation();

		Map<String, List<List<Object>>> result = null;
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
}