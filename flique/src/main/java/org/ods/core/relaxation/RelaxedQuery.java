package org.ods.core.relaxation;

import org.apache.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery>, Cloneable {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private float similarity;
    private ArrayList<String> relaxationsLog;

    public RelaxedQuery() {
        super();
        this.similarity = 0;
        relaxationsLog = new ArrayList<>();
    }

    public RelaxedQuery(Query query) {
        super(query);
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
}
