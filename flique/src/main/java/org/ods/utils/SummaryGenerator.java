package org.ods.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.quetsal.util.TBSSSummariesGenerator;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *  Generate FLiQue Summaries for a set of federation members (SPARQL endpoints)
 * @author BenjaminM
 */
public class SummaryGenerator {

    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        List<String> endpoints = Arrays.asList("http://localhost:8890/sparql"
//                "http://localhost:8891/sparql"
//                "http://localhost:8892/sparql",
//                "http://localhost:8893/sparql",
//                "http://localhost:8894/sparql",
//                "http://localhost:8895/sparql",
//                "http://localhost:8896/sparql",
//                "http://localhost:8897/sparql",
//                "http://localhost:8898/sparql",
//                "http://localhost:8899/sparql"
        );
        String outputFile = "summaries/fedbench.n3";
        String namedGraph = null;  //can be null. in that case all graph will be considered
        TBSSSummariesGenerator generator = new TBSSSummariesGenerator(outputFile);
        long startTime = System.currentTimeMillis();
        int branchLimit =1;
        generator.generateSummaries(endpoints, namedGraph, branchLimit);
        System.out.println("Data Summaries Generation Time (min): "+ (System.currentTimeMillis() - startTime) / (1000*60));
        System.out.print("Data Summaries are successfully stored at "+ outputFile);
    }

}
