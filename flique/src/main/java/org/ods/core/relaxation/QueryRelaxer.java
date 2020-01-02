package org.ods.core.relaxation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

class QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxer.class);

    public static ArrayList<RelaxedQuery> relax(RelaxedQuery query, Model ontology, Model summary) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    ArrayList<TriplePath> relaxedTriples = TriplePatternRelaxer.relax(triple, summary, ontology);
                    for (TriplePath relaxedTriple : relaxedTriples) {
                        RelaxedQuery relaxedQuery = query.clone();
                        switchTriple(relaxedQuery, triple, relaxedTriple);
                        relaxedQueries.add(relaxedQuery);
                    }
                }
            }
        });
        log.info(relaxedQueries.toString());
        return relaxedQueries;
    }

    private static void switchTriple(RelaxedQuery query, TriplePath oldTriple, TriplePath relaxedTriple) {
        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                ListIterator<TriplePath> tps = el.getPattern().iterator();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    if (triple.equals(oldTriple)) {
                        tps.remove();
                        tps.add(relaxedTriple);
                        break;
                    }
                }
            }
        });
    }
}
