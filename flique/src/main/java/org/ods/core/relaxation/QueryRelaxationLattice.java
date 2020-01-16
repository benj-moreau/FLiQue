package org.ods.core.relaxation;

import com.fluidops.fedx.algebra.StatementSource;
import org.apache.jena.query.QueryFactory;

import org.apache.jena.rdf.model.Model;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryRelaxationLattice {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxationLattice.class);
    private RelaxedQuery originalQuery;
    private PriorityQueue<RelaxedQuery> priorityQueue;
    private Model ontology;
    private Model summary;
    private double minSimilarity;
    private Map<StatementPattern, List<StatementSource>> stmtToSources;

    public QueryRelaxationLattice(String originalQuery, Model ontology, Model summary, Map<StatementPattern, List<StatementSource>> stmtToSources, double minSimilarity) {
        this.priorityQueue = new PriorityQueue<>(Collections.reverseOrder());
        this.originalQuery = new RelaxedQuery();
        QueryFactory.parse(this.originalQuery, originalQuery, null, null);
        this.originalQuery.initOriginalTriples();
        this.ontology = ontology;
        this.summary = summary;
        this.stmtToSources = stmtToSources;
        this.minSimilarity = minSimilarity;
        this.priorityQueue.addAll(QueryRelaxer.relax(this.originalQuery ,this.originalQuery ,this.ontology, this.summary, this.minSimilarity));
    }

    public boolean hasNext() {
        return (this.priorityQueue.size() > 0);
    }

    public RelaxedQuery next() {
        RelaxedQuery nextMostSimilarQuery = this.priorityQueue.poll();
        if (nextMostSimilarQuery != null) {
            this.priorityQueue.addAll(QueryRelaxer.relax(this.originalQuery, nextMostSimilarQuery, this.ontology, this.summary, this.minSimilarity));
        } else {
            throw new NoSuchElementException("RelaxedLattice has no more relaxed queries to generate");
        }
        return nextMostSimilarQuery;
    }

    public int sizeOfRemaining() {
        return this.priorityQueue.size();
    }
}
