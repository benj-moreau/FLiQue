package org.ods.core.relaxation.strategy;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.ods.core.relaxation.RelaxedQuery;
import org.ods.core.relaxation.TriplePatternRelaxer;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

public class OBFSQueryRelaxer extends QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(OBFSQueryRelaxer.class);

    public ArrayList<RelaxedQuery> relax(RelaxedQuery originalQuery, RelaxedQuery queryToRelax, Model ontology, Model summary, double minSimilarity) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        ElementWalker.walk(queryToRelax.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    TriplePath relaxedTriple = TriplePatternRelaxer.relax(triple, summary, ontology);
                    if (relaxedTriple != null) {
                        RelaxedQuery relaxedQuery = queryToRelax.clone();
                        switchTriple(relaxedQuery, triple, relaxedTriple);
                        QuerySimilarity.compute(relaxedQuery, summary);
                        relaxedQuery.incrementLevel();
                        if (relaxedQuery.getSimilarity() >= minSimilarity) {
                            relaxedQueries.add(relaxedQuery);
                        }
                    }
                }
            }
        });
        return relaxedQueries;
    }
}
