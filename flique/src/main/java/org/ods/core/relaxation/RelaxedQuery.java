package org.ods.core.relaxation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery>, Cloneable {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private double similarity;
    // to link a relaxedTriple to its OriginalTriple
    private HashMap<TriplePath, TriplePath> originalTriples;

    public RelaxedQuery() {
        super();
        this.similarity = 1.0;
        this.originalTriples = new HashMap<>();
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public HashMap<TriplePath, TriplePath> getOriginalTriples() {
        return this.originalTriples;
    }

    // to call after query is parsed
    public void initOriginalTriples() {
        originalTriples.clear();
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                TriplePath triple;
                while (tps.hasNext()) {
                    triple = tps.next();
                    originalTriples.put(triple, triple);
                }
            }
        });
    }

    public void updateOriginalTriples(TriplePath oldTriple, TriplePath relaxedTriple) {
       TriplePath originalTriple = this.originalTriples.get(oldTriple);
        this.originalTriples.remove(oldTriple);
        this.originalTriples.put(relaxedTriple, originalTriple);
    }


    @Override
    public int compareTo(RelaxedQuery o) {
        if (this.similarity == o.similarity) {
            return this.toString().compareTo(o.toString());
        } else {
            return (this.similarity < o.similarity ? -1 : +1);
        }
    }

    @Override
    public RelaxedQuery clone()  {
        RelaxedQuery clone = new RelaxedQuery();
        QueryFactory.parse(clone, this.serialize(), null, null);
        clone.similarity = this.similarity;
        clone.originalTriples = (HashMap<TriplePath, TriplePath>) this.originalTriples.clone();
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + "_________________________________\nSimilarity:" + this.similarity;
    }
}
