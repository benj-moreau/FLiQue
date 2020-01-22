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

    public ArrayList<RelaxedQuery> relax(RelaxedQuery originalQuery, RelaxedQuery queryToRelax, Model ontology, QuerySimilarity querySimilarity) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        ElementWalker.walk(queryToRelax.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    TriplePath relaxedTriple = TriplePatternRelaxer.relax(triple, ontology, querySimilarity);
                    if (relaxedTriple != null) {
                        RelaxedQuery relaxedQuery = queryToRelax.clone();
                        switchTriple(relaxedQuery, triple, relaxedTriple);
                        relaxedQuery.setNeedToEvaluate(OBFSCheckNecessity(triple, relaxedTriple, querySimilarity));
                        relaxedQuery.incrementLevel();
                        relaxedQueries.add(relaxedQuery);
                    }
                }
            }
        });
        return relaxedQueries;
    }

    private Boolean OBFSCheckNecessity(TriplePath originalTriple,TriplePath relaxedTriple,QuerySimilarity querySimilarity) {
        if (!originalTriple.getSubject().equals(relaxedTriple.getSubject())) {
            // subject has been relaxed
            return true;
        } else if (!originalTriple.getPredicate().equals(relaxedTriple.getPredicate())) {
            // predicate has been relaxed
            if (!relaxedTriple.getPredicate().isVariable()) {
                // it's a property relaxation
                String propertyURI = originalTriple.getPredicate().getURI();
                String superPropertyURI = relaxedTriple.getPredicate().getURI();
                return querySimilarity.getTriplesNumber(superPropertyURI) > querySimilarity.getTriplesNumber(propertyURI);
            }
        } else {
            // object has been relaxed
            if (!relaxedTriple.getObject().isVariable()) {
                // it's a class relaxation
                String classURI = originalTriple.getObject().getURI();
                String superClassURI = relaxedTriple.getObject().getURI();
                return querySimilarity.getInstancesNumber(superClassURI) > querySimilarity.getInstancesNumber(classURI);
            }
        }
        return true;
    }
}
