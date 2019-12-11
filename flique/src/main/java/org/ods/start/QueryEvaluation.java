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
		String queries = "L8";
	
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
		String fileName;
		String fedName;
		Date date;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		for (String curQueryName : qnames)
		{
			date = new Date();
			fedName = "Federation " + curQueryName;
			fileName = "results/" + formatter.format(date) + "_" + curQueryName + "-results.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			long startTime = System.currentTimeMillis();
			execute(curQueryName, cfgName, endpoints, writer, fedName);
			long runTime = System.currentTimeMillis() - startTime;
			log.info(curQueryName + ": Query execution time (msec): "+ runTime);
			writer.close();
		}
	}

	public void execute(String curQueryName, String cfgName, List<String> endpoints,BufferedWriter writer, String fedName) throws Exception {
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
			writer.write(stmtToSources.toString() + "\n\n");
			log.info(stmtToSources.toString());
			// 1. Check licenses
			LicenseChecker licenseChecker = new LicenseChecker("summaries/largeRDFBench.n3");
			EndpointManager endpointManager = queryInfo.getFedXConnection().getEndpointManager();
			Set<String> consistentLicenses = licenseChecker.getConsistentLicenses(sourceSelection, endpointManager);
			if (consistentLicenses.isEmpty()) {
				// a license compatible with licenses of sources does not exists
				// We need to eliminate sources
				licenseChecker.getEndpointlicenseConflicts();
				ArrayList<String> sourcesToRemove = licenseChecker.getSourcesToRemove();
				//remove endpoints
				ArrayList<String> newEndpoints = new ArrayList<>(endpoints);
				newEndpoints.removeAll(sourcesToRemove);
				// recursive call.. we restart a query execution with the new federation
				execute(curQueryName, cfgName, newEndpoints, writer, fedName + "'");
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
}