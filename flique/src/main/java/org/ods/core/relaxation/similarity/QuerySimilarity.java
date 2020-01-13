package org.ods.core.relaxation.similarity;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.ods.core.relaxation.RelaxedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class QuerySimilarity {
    protected static final Logger log = LoggerFactory.getLogger(QuerySimilarity.class);

    // compute the similarity of the relaxedQuery compared to originalQuery
    public static void compute(RelaxedQuery relaxedQuery, Model summary) {
        relaxedQuery.setSimilarity(1.0);
        TriplePath relaxedTriple;
        TriplePath originalTriple;
        double tripleWeight = 1.0;
        for (Map.Entry<TriplePath, TriplePath> entry : relaxedQuery.getOriginalTriples().entrySet()) {
            relaxedTriple = entry.getKey();
            originalTriple = entry.getValue();
            // Sim(Q,Q') = P1..n(tripleWeight * sim(tpi, tpi')
            relaxedQuery.setSimilarity(relaxedQuery.getSimilarity() * (tripleWeight * TriplePatternSimilarity.compute(originalTriple, relaxedTriple, summary)));
        }
    }
}