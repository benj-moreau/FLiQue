package org.ods.core.relaxation;

import org.apache.jena.query.QueryFactory;

import org.apache.jena.rdf.model.Model;
import org.ods.core.relaxation.QueryRelaxer;

import java.util.ArrayList;
import java.util.TreeSet;

public class QueryRelaxationLattice {
    private ArrayList<TreeSet> levels = new ArrayList<>();
    private Model ontology;
    private Model summary;

    public QueryRelaxationLattice(String originalQuery, Model ontology, Model summary) {
        TreeSet<RelaxedQuery> firstLevel = new TreeSet<>();
        RelaxedQuery query = new RelaxedQuery();
        QueryFactory.parse(query, originalQuery, null, null);
        firstLevel.add(query);
        this.levels.add(firstLevel);
        this.ontology = ontology;
        this.summary = summary;
    }

    public TreeSet getLevel(int level) {
        return this.levels.get(level);
    }

    public TreeSet nextLevel() {
        TreeSet<RelaxedQuery> nextLevel = new TreeSet<>();
        TreeSet<RelaxedQuery> previousLevel =  this.levels.get(this.levels.size() - 1);
        for (RelaxedQuery previousQuery : previousLevel) {
            // relacher la requete (3x ?)
            // verifier si pruned
            // sinon ajouter a nextLevel
            break;
        }
        this.levels.add(nextLevel);
        return nextLevel;
    }


}
