package org.ods.core.relaxation;

public class QueryRelaxer {

    public QueryRelaxer(){}

    public static RelaxedQuery trucRelaxation(RelaxedQuery query) {
        RelaxedQuery relaxedQuery = (RelaxedQuery) query.clone();
        return relaxedQuery;
    }
}
