package org.ods.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Generates FLiQue Summaries for a set of federation members (SPARQL endpoints)
 * @author BenjaminM
 */
public class SummaryGenerator {
    static Logger log = LoggerFactory.getLogger(SummaryGenerator.class);
    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        String host = "localhost";
        List<String> endpoints = Arrays.asList(
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
        );
        String outputFile = "summaries/saturated-largeRDFBench-summaries.n3"; // The summary will be generated in this file
        String caliFile = "../cali-ordering/fedbench_cali_ordering.n3"; // CaLi ordering is in this file
        String namedGraph = "http://aksw.org/benchmark";  //can be null. in that case all graph will be considered
        TBSSSummariesGenerator generator = new TBSSSummariesGenerator(outputFile);
        long startTime = System.currentTimeMillis();
        int branchLimit = 1;
        generator.generateSummaries(endpoints, namedGraph, branchLimit);
        Model costfedSummary = ModelFactory.createDefaultModel();
        InputStream costfedSummaryIn = new FileInputStream(outputFile);
        if (costfedSummaryIn == null) {
            log.error("Costfed Summary file not found");
        } else {
            costfedSummary.read(costfedSummaryIn, " ", "N3");
            Model caliOrdering = ModelFactory.createDefaultModel();
            InputStream caliOrderingIn = new FileInputStream(caliFile);
            if (caliOrderingIn == null) {
                log.error("Cali Ordering file not found");
            } else {
                caliOrdering.read(caliOrderingIn, " ", "N3");
            }
            Model fliqueSummary = costfedSummary.union(caliOrdering);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            fliqueSummary.write(outputStream, "N3");
            log.info("Cali Ordering merged in data summary");
            log.info("Data Summaries Generation Time (min): " + (System.currentTimeMillis() - startTime) / (1000 * 60));
            log.info("Data Summaries are successfully stored at " + outputFile);
        }
    }
}
