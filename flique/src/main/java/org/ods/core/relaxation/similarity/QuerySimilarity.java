package org.ods.core.relaxation;

public class QuerySimilarity {
    RelaxedQuery originalQuery;

    public QuerySimilarity(RelaxedQuery originalQuery) {
        this.originalQuery = originalQuery;
    }

    public double compute(RelaxedQuery relaxedQuery) {
        return 1.0;
    }
}
