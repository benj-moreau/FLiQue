package org.ods.core.relaxation;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.ods.core.relaxation.similarity.QuerySimilarity;

import java.util.ArrayList;
import java.util.List;

public class QueryRelaxer {
    private Model ontology;
    private RelaxedQuery originalQuery;
    private QuerySimilarity querySimilarity;
    private ArrayList<TriplePatternRelaxer> triplePatternRelaxers;

    public QueryRelaxer(RelaxedQuery originalQuery,Model ontology, Model summary){
        this.ontology = ontology;
        this.originalQuery = originalQuery;
        this.querySimilarity = new QuerySimilarity(originalQuery, summary);
        this.triplePatternRelaxers = new ArrayList<>();
        List<Triple> triples = this.originalQuery.getTriples();
        for (Triple triple: this.originalQuery.getTriples()) {
            this.triplePatternRelaxers.add(new TriplePatternRelaxer(triple));
        }
    }
}
