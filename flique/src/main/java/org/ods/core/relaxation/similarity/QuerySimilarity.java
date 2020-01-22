package org.ods.core.relaxation.similarity;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.ods.core.relaxation.RelaxedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class QuerySimilarity {
    protected static final Logger log = LoggerFactory.getLogger(QuerySimilarity.class);

    private ArrayList<String> endpoints;
    private Model summary;
    private HashMap<String, Integer> federationClassStatistics;
    private HashMap<String, Integer> federationPropertyStatistics;

    public QuerySimilarity(ArrayList<String> endpoints, Model summary) {
        this.endpoints = endpoints;
        this.federationClassStatistics = new HashMap<>();
        this.federationPropertyStatistics = new HashMap<>();
        this.summary = summary;
    }

    // compute the similarity of the relaxedQuery compared to originalQuery
    public void compute(RelaxedQuery relaxedQuery) {
        relaxedQuery.setSimilarity(1.0);
        TriplePath relaxedTriple;
        TriplePath originalTriple;
        double tripleWeight = 1.0;
        for (Map.Entry<TriplePath, TriplePath> entry : relaxedQuery.getOriginalTriples().entrySet()) {
            relaxedTriple = entry.getKey();
            originalTriple = entry.getValue();
            // Sim(Q,Q') = P1..n(tripleWeight * sim(tpi, tpi')
            relaxedQuery.setSimilarity(relaxedQuery.getSimilarity() * (tripleWeight * TriplePatternSimilarity.compute(
                    originalTriple,
                    relaxedTriple,
                    summary,
                    federationClassStatistics,
                    federationPropertyStatistics,
                    endpoints)
                    ));
        }
    }

    public int getTriplesNumber(String propertyURI) {
        return TriplePatternSimilarity.getTriplesNumber(propertyURI, this.summary, this.federationPropertyStatistics, this.endpoints);
    }

    public int getInstancesNumber(String classURI) {
        return TriplePatternSimilarity.getInstancesNumber(classURI, this.summary, this.federationClassStatistics, this.endpoints);
    }
}