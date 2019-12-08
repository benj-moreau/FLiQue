package org.ods.core.relaxation;

import org.apache.jena.rdf.model.Model;
import org.ods.core.relaxation.similarity.QuerySimilarity;

public class QueryRelaxer {
    private Model ontology;
    private RelaxedQuery originalQuery;
    private QuerySimilarity querySimilarity;

    public QueryRelaxer(RelaxedQuery originalQuery,Model ontology, Model summary){
        this.ontology = ontology;
        this.originalQuery = originalQuery;
        this.querySimilarity = new QuerySimilarity(originalQuery, summary);
    }

    public static RelaxedQuery trucRelaxation(RelaxedQuery query) {
        RelaxedQuery relaxedQuery = (RelaxedQuery) query.clone();
        return relaxedQuery;
    }
}
