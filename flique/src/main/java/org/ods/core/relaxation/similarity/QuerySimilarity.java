package org.ods.core.relaxation.similarity;

import org.apache.jena.rdf.model.Model;
import org.ods.core.relaxation.RelaxedQuery;
import org.ods.core.relaxation.similarity.TriplePatternSimilarity;

import java.util.ArrayList;

public class QuerySimilarity {
    private RelaxedQuery originalQuery;
    private Model summary;
    private ArrayList<Double> weight;
    private ArrayList<TriplePatternSimilarity> triplePatternSimilarities;

    public QuerySimilarity(RelaxedQuery originalQuery, Model summary) {
        this.originalQuery = originalQuery;
        this.summary = summary;
    }

    public double compute(RelaxedQuery relaxedQuery) {
        // pour chaque triple pattern sim
        // appeller compute * weight
        // calculer similarity tot
        return 1.0;
    }
}
