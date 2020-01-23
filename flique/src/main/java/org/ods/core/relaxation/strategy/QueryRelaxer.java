package org.ods.core.relaxation.strategy;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.ods.core.relaxation.RelaxedQuery;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.ListIterator;

public abstract class QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxer.class);

    public abstract ArrayList<RelaxedQuery> relax(RelaxedQuery originalQuery, RelaxedQuery queryToRelax, Model ontology, QuerySimilarity querySimilarity);

    protected static void switchTriple(RelaxedQuery query, TriplePath oldTriple, TriplePath relaxedTriple) {
        ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                ListIterator<TriplePath> tps = el.getPattern().iterator();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    if (triple.equals(oldTriple)) {
                        tps.remove();
                        if (!isSPO(relaxedTriple)) {
                            // SPO's are deleted from query
                            tps.add(relaxedTriple);
                        }
                        query.updateOriginalTriples(oldTriple, relaxedTriple);
                        break;
                    }
                }
            }
        });
    }

    protected static Boolean isSPO(TriplePath triple){
        if (!triple.getSubject().isVariable()) {
            return false;
        } else if (!triple.getPredicate().isVariable()) {
            return false;
        } else if (!triple.getObject().isVariable()) {
            return false;
        }
        return true;
    }
}
