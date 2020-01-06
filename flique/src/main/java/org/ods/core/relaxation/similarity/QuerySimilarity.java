package org.ods.core.relaxation.similarity;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.ods.core.relaxation.RelaxedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;


public class QuerySimilarity {
    protected static final Logger log = LoggerFactory.getLogger(QuerySimilarity.class);

    // compute the similarity of the relaxedQuery compared to originalQuery
    public static void compute(RelaxedQuery originalQuery, RelaxedQuery relaxedQuery, Model summary) {
        relaxedQuery.setSimilarity(0.0);
        ElementWalker.walk(originalQuery.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock originalEl) {
                ElementWalker.walk(relaxedQuery.getQueryPattern(), new ElementVisitorBase() {
                    public void visit(ElementPathBlock relaxedEl) {
                        Iterator<TriplePath> originalTps = originalEl.patternElts();
                        Iterator<TriplePath> relaxedTps = relaxedEl.patternElts();
                        while (originalTps.hasNext() && relaxedTps.hasNext()) {
                            double tripleWeight = 1.0;
                            TriplePath originalTriple = originalTps.next();
                            TriplePath relaxedTriple = relaxedTps.next();
                            // Sim(Q,Q') = P1..n(tripleWeight * sim(tpi, tpi')
                            relaxedQuery.setSimilarity(relaxedQuery.getSimilarity() * (tripleWeight * TriplePatternSimilarity.compute(originalTriple, relaxedTriple, summary)));
                        }
                    }
                });
            }
        });
    }
}