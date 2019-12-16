package org.ods.core.relaxation;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery> {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private float similarity;
    private ArrayList<String> relaxationsLog;

    public RelaxedQuery() {
        super();
        this.similarity = 0;
        relaxationsLog = new ArrayList<>();
    }

    public float getSimilarity() {
        return similarity;
    }

    public ArrayList<String> getRelaxations() {
        return relaxationsLog;
    }


    @Override
    public int compareTo(RelaxedQuery o) {
        if (this.similarity == o.similarity) {
            return 0;
        } else {
            return (this.similarity < o.similarity ? -1 : +1);
        }
    }

    public List<TriplePath> getTriples(){
        ArrayList<TriplePath> triples = new ArrayList<>();
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    triples.add(tps.next());
                }
            }
        });
        log.info(triples.toString());
        return triples;
    }
}
