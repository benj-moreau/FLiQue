package org.ods.core.relaxation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.ods.start.QueryEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

class TriplePatternRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(QueryEvaluation.class);

    public static ArrayList<TriplePath> relax(TriplePath triple, Model summary, Model ontology) {
        ArrayList<TriplePath> relaxedTriples = new ArrayList<>();
        return relaxedTriples;
    }
}
