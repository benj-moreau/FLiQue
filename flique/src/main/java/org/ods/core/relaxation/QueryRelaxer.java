package org.ods.core.relaxation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

class QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxer.class);

    public static ArrayList<RelaxedQuery> relax(RelaxedQuery originalQuery, RelaxedQuery queryToRelax, Model ontology, Model summary) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        ElementWalker.walk(queryToRelax.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    ArrayList<TriplePath> relaxedTriples = TriplePatternRelaxer.relax(triple, summary, ontology);
                    for (TriplePath relaxedTriple : relaxedTriples) {
                        RelaxedQuery relaxedQuery = queryToRelax.clone();
                        switchTriple(relaxedQuery, triple, relaxedTriple);
                        QuerySimilarity.compute(originalQuery, relaxedQuery, summary);
                        relaxedQueries.add(relaxedQuery);
                    }
                }
            }
        });
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
