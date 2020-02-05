package org.ods.core.relaxation;

import com.fluidops.fedx.exception.FedXRuntimeException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery>, Cloneable {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private double similarity;
    // to link a relaxedTriple to its OriginalTriple
    private HashMap<TriplePath, TriplePath> originalTriples;
    private int level;
    private boolean needToEvaluate;
    // Minimal Failling Subqueries
    private ArrayList<ArrayList<TriplePath>> MFSs;

    public RelaxedQuery() {
        super();
        this.level = 0;
        this.similarity = 1.0;
        this.originalTriples = new HashMap<>();
        this.needToEvaluate = true;
        this.MFSs = new ArrayList<>();
    }

    public double getSimilarity() {
        return this.similarity;
    }

    public int getLevel() {
        return level;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public void incrementLevel() {
        this.level += 1;
    }

    public boolean needToEvaluate() {
        return this.needToEvaluate;
    }

    public void setNeedToEvaluate(Boolean needToEvaluate) {
        this.needToEvaluate = needToEvaluate;
    }

    public HashMap<TriplePath, TriplePath> getOriginalTriples() {
        return this.originalTriples;
    }

    public ArrayList<ArrayList<TriplePath>> getMFSs() { return this.MFSs; }

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
            return this.serialize().compareTo(o.serialize());
        } else {
            return (this.similarity < o.similarity ? -1 : +1);
        }
    }

    @Override
    public RelaxedQuery clone()  {
        RelaxedQuery clone = new RelaxedQuery();
        QueryFactory.parse(clone, this.serialize(), null, null);
        clone.similarity = this.similarity;
        clone.level = this.getLevel();
        clone.originalTriples = (HashMap<TriplePath, TriplePath>) this.originalTriples.clone();
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() +
                "_________________________________\nSimilarity:" + this.similarity +
                " Level:" + this.getLevel() + " Evaluation: " + this.needToEvaluate;
    }

    private ArrayList<TriplePath> findAnMFS(SailRepository repo) {
        RelaxedQuery query = this.clone();
        ArrayList<TriplePath> MFS = new ArrayList<>();
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    query.removeTriple(triple);
                    RelaxedQuery evalQuery = query.clone();
                    evalQuery.addTriple(MFS);
                    if (evalQuery.hasAtLeastOneResult(repo)){
                        MFS.add(triple);
                    }
                }
            }
        });
        return MFS;
    }

    public ArrayList<ArrayList<TriplePath>> FindAllMFS(SailRepository repo) {
        ArrayList<ArrayList<TriplePath>> MFSs = new ArrayList<>();

        return MFSs;
    }

    public boolean hasAtLeastOneResult(SailRepository repo) {
        TupleQuery query = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, this.serialize());
        try {
            TupleQueryResult res = query.evaluate();
            if (res.hasNext()) {
                return true;
            }
        } catch(QueryEvaluationException | FedXRuntimeException ex){
            log.error(ex.getMessage());
        }
        return false;
    }

    public void removeTriple(TriplePath triple) {
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple2 = tps.next();
                    if (triple.equals(triple2)) {
                        tps.remove();
                        break;
                    }
                }
            }
        });
    }

    public void addTriple(TriplePath triple) {
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                el.addTriple(triple);
            }
        });
    }

    public void addTriple(ArrayList<TriplePath> triples) {
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                for (TriplePath triple : triples) {
                    el.addTriple(triple);
                }
            }
        });
    }

    public ArrayList<TriplePath> getTriples() {
        ArrayList<TriplePath> triples = new ArrayList<>();
        ElementWalker.walk(this.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    triples.add(triple);
                }
            }
        });
        return triples;
    }
}
