package org.ods.core.relaxation;

import com.fluidops.fedx.algebra.StatementSource;

import org.apache.jena.rdf.model.Model;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.ods.core.relaxation.strategy.QueryRelaxer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryRelaxationLattice {
    protected static final Logger log = LoggerFactory.getLogger(QueryRelaxationLattice.class);
    private RelaxedQuery originalQuery;
    private QueryRelaxer queryRelaxer;
    private PriorityQueue<RelaxedQuery> priorityQueue;
    private Model ontology;
    private Model summary;
    private double minSimilarity;
    private Map<StatementPattern, List<StatementSource>> stmtToSources;
    private ArrayList<String> endpoints;
    private QuerySimilarity querySimilarity;

    public QueryRelaxationLattice(RelaxedQuery originalQuery, Model ontology, Model summary, Map<StatementPattern, List<StatementSource>> stmtToSources, double minSimilarity, QueryRelaxer queryRelaxer, ArrayList<String> endpoints) {
        this.priorityQueue = new PriorityQueue<>(Collections.reverseOrder());
        this.originalQuery = originalQuery;
        this.ontology = ontology;
        this.summary = summary;
        this.stmtToSources = stmtToSources;
        this.endpoints = endpoints;
        this.minSimilarity = minSimilarity;
        this.queryRelaxer = queryRelaxer;
        this.querySimilarity = new QuerySimilarity(this.endpoints, this.summary);
        ArrayList<RelaxedQuery> relaxedQueries = this.queryRelaxer.relax(this.originalQuery ,this.originalQuery ,this.ontology, this.querySimilarity);
        for (RelaxedQuery relaxedQuery : relaxedQueries) {
            querySimilarity.compute(relaxedQuery);
            if (relaxedQuery.getSimilarity() >= this.minSimilarity) { this.priorityQueue.add(relaxedQuery); }
        }
    }

    public boolean hasNext() {
        return (this.priorityQueue.size() > 0);
    }

    public RelaxedQuery next() {
        RelaxedQuery nextMostSimilarQuery = this.priorityQueue.poll();
        if (nextMostSimilarQuery != null) {
            ArrayList<RelaxedQuery> relaxedQueries = this.queryRelaxer.relax(this.originalQuery, nextMostSimilarQuery, this.ontology, this.querySimilarity);
            for (RelaxedQuery relaxedQuery : relaxedQueries) {
               this.querySimilarity.compute(relaxedQuery);
                if (relaxedQuery.getSimilarity() >= this.minSimilarity) { this.priorityQueue.add(relaxedQuery); }
            }
        } else {
            throw new NoSuchElementException("RelaxedLattice has no more relaxed queries to generate");
        }
        return nextMostSimilarQuery;
    }

    public int sizeOfRemaining() {
        return this.priorityQueue.size();
    }
}
