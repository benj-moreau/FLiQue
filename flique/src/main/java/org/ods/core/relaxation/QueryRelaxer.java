package org.ods.core.relaxation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

class QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxer.class);

    public static ArrayList<RelaxedQuery> relax(RelaxedQuery query, Model ontology, Model summary) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        for (TriplePath triple : query.getTriples()) {
            log.info(triple.toString());
            ArrayList<TriplePath> relaxedTriples = TriplePatternRelaxer.relax(triple, summary, ontology);
        }
        return relaxedQueries;
    }
}
