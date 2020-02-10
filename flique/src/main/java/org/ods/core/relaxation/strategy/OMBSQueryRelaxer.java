package org.ods.core.relaxation.strategy;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.ods.core.relaxation.RelaxedQuery;
import org.ods.core.relaxation.TriplePatternRelaxer;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

import static org.ods.core.relaxation.strategy.OBFSQueryRelaxer.OBFSCheckNecessity;

public class OMBSQueryRelaxer extends QueryRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(OMBSQueryRelaxer.class);
    private ArrayList<ArrayList<TriplePath>> MFSs = null;

    public ArrayList<RelaxedQuery> relax(RelaxedQuery originalQuery, RelaxedQuery queryToRelax, Model ontology, QuerySimilarity querySimilarity) {
        ArrayList<RelaxedQuery> relaxedQueries = new ArrayList<>();
        if (MFSs == null) {
            // first exec, MFSs should contains MFS of original query
            this.MFSs = originalQuery.FindAllMFS(repo);
        }
        ElementWalker.walk(queryToRelax.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    TriplePath relaxedTriple = TriplePatternRelaxer.relax(triple, ontology, querySimilarity);
                    if (relaxedTriple != null) {
                        RelaxedQuery relaxedQuery = queryToRelax.clone();
                        switchTriple(relaxedQuery, triple, relaxedTriple);
                        relaxedQuery.setNeedToEvaluate(OBFSCheckNecessity(triple, relaxedTriple, querySimilarity) && OMBSCheckNecessity(relaxedQuery, MFSs));
                        relaxedQuery.incrementLevel();
                        relaxedQueries.add(relaxedQuery);
                    }
                }
            }
        });
        return relaxedQueries;
    }

    protected static Boolean OMBSCheckNecessity(RelaxedQuery relaxedQuery, ArrayList<ArrayList<TriplePath>> MFSs) {
        ArrayList<TriplePath> triples = relaxedQuery.getTriples();
        for ( ArrayList<TriplePath> mfs : MFSs ) {
            if (triples.containsAll(mfs)) {
                return false;
            }
        }
        return true;
    }
}
