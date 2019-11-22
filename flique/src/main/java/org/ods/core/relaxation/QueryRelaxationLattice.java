package org.ods.core.relaxation;

import org.apache.jena.query.QueryFactory;

import java.util.ArrayList;
import java.util.TreeSet;

public class QueryRelaxationLattice {
    private ArrayList<TreeSet> levels;

    public QueryRelaxationLattice(String originalQuery) {
        TreeSet<RelaxedQuery> firstLevel = new TreeSet<>();
        RelaxedQuery query = new RelaxedQuery();
        QueryFactory.parse(query, originalQuery, null, null);
        firstLevel.add(query);
        this.levels.add(firstLevel);
    }

    public TreeSet<RelaxedQuery> getLevel(int level) {
        return this.levels.get(level);
    }
}
