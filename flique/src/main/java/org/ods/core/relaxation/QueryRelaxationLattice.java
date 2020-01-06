package org.ods.core.relaxation;

import com.fluidops.fedx.algebra.StatementSource;
import org.apache.jena.query.QueryFactory;

import org.apache.jena.rdf.model.Model;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class QueryRelaxationLattice {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxationLattice.class);

    private ArrayList<TreeSet> levels = new ArrayList<>();
    private Model ontology;
    private Model summary;
    Map<StatementPattern, List<StatementSource>> stmtToSources;

    public QueryRelaxationLattice(String originalQuery, Model ontology, Model summary, Map<StatementPattern, List<StatementSource>> stmtToSources) {
        TreeSet<RelaxedQuery> firstLevel = new TreeSet<>();
        RelaxedQuery query = new RelaxedQuery();
        QueryFactory.parse(query, originalQuery, null, null);
        firstLevel.add(query);
        this.levels.add(firstLevel);
        this.ontology = ontology;
        this.summary = summary;
        this.stmtToSources = stmtToSources;
    }

    public TreeSet getLevel(int level) {
        return this.levels.get(level);
    }

    public TreeSet<RelaxedQuery> nextLevel() {
        TreeSet<RelaxedQuery> nextLevel = new TreeSet<>();
        TreeSet<RelaxedQuery> previousLevel =  this.levels.get(this.levels.size() - 1);
        for (RelaxedQuery previousQuery : previousLevel) {
            nextLevel.addAll(QueryRelaxer.relax((RelaxedQuery) this.levels.get(0).first() ,previousQuery ,this.ontology, this.summary));
            // relacher la requete (3x ?)
            // verifier si pruned
            // sinon ajouter a nextLevel
        }
        log.info(nextLevel.toString());
        this.levels.add(nextLevel);
        return nextLevel;
    }
}
