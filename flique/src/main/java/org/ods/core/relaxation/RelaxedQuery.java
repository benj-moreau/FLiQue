package org.ods.core.relaxation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery>, Cloneable {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private double similarity;
    private ArrayList<String> relaxationsLog;

    public RelaxedQuery() {
        super();
        this.similarity = 0;
        this.relaxationsLog = new ArrayList<>();
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
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

    @Override
    public RelaxedQuery clone()  {
        RelaxedQuery clone = new RelaxedQuery();
        QueryFactory.parse(clone, this.serialize(), null, null);
        clone.similarity = this.similarity;
        clone.relaxationsLog = (ArrayList<String>) this.relaxationsLog.clone();
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + "_________________________________\nSimilarity:" + this.similarity;
    }
}
