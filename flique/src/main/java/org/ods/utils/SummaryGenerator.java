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
        List<String> endpoints = Arrays.asList(
                "http://localhost:8881/sparql",
                "http://localhost:8882/sparql",
                "http://localhost:8883/sparql"
        );
        String outputFile = "summaries/paper_licensed_summary.n3"; // The summary will be generated in this file
        String caliFile = "../cali-ordering/paper_cali_ordering.n3"; // CaLi ordering is in this file
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
