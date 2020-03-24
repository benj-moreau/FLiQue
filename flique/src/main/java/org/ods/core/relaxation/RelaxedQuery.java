package org.ods.core.relaxation;

import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.exception.FedXRuntimeException;
import com.fluidops.fedx.structures.QueryInfo;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery>, Cloneable {
    protected static final Logger log = LoggerFactory.getLogger(RelaxedQuery.class);

    private double similarity;
    // to link a relaxedTriple to its OriginalTriple
    private HashMap<TriplePath, TriplePath> originalTriples;
    private int level;
    private boolean needToEvaluate;
    // Minimal Failling Subqueries
    private ArrayList<ArrayList<TriplePath>> MFSs;
    private boolean sourceSelected;
    private TupleQueryResult res;

    public RelaxedQuery() {
        super();
        this.level = 0;
        this.similarity = 1.0;
        this.originalTriples = new HashMap<>();
        this.needToEvaluate = true;
        this.MFSs = new ArrayList<>();
        this.sourceSelected = false;
        this.res = null;
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
        clone.sourceSelected = false;
        clone.res = null;
        return clone;
    }

    public RelaxedQuery clone(ArrayList<TriplePath> triples)  {
        RelaxedQuery clone = this.clone();
        ElementWalker.walk(clone.getQueryPattern(), new ElementVisitorBase() {
            public void visit(ElementPathBlock el) {
                Iterator<TriplePath> tps = el.patternElts();
                while (tps.hasNext()) {
                    TriplePath triple = tps.next();
                    if (!triples.contains(triple)) {
                        tps.remove();
                        break;
                    }
                }
            }
        });
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
                    TupleQueryResult res = evalQuery.mayHaveAResult(repo);
                    if (res != null && res.hasNext()){
                        MFS.add(triple);
                    }
                }
            }
        });
        return MFS;
    }

    public ArrayList<ArrayList<TriplePath>> FindAllMFS(SailRepository repo) {
        ArrayList<TriplePath> MFS = this.findAnMFS(repo);
        ArrayList<ArrayList<TriplePath>> pxss = this.pxss(MFS);
        ArrayList<ArrayList<TriplePath>> MFSs = new ArrayList<>();MFSs.add(MFS);
        ArrayList<ArrayList<TriplePath>> XSSs = new ArrayList<>();
        while (!pxss.isEmpty()) {
            ArrayList<TriplePath> pxs = pxss.get(0);
            RelaxedQuery query = this.clone(pxs);
            TupleQueryResult res = query.mayHaveAResult(repo);
            if (res != null && res.hasNext()) {
                if (!XSSs.contains(pxs)) { XSSs.add(pxs); }
                pxss.remove(pxs);
            } else {
                ArrayList<TriplePath> MFS2 = query.findAnMFS(repo);
                if (!MFSs.contains(MFS2)) { MFSs.add(MFS2); }
                for (ListIterator<ArrayList<TriplePath>> itr = pxss.listIterator(); itr.hasNext();) {
                    ArrayList<TriplePath> px = itr.next();
                    itr.remove();
                    if (px.containsAll(MFS2)) {
                        // MFS included in px
                        RelaxedQuery query2 = this.clone(px);
                        ArrayList<ArrayList<TriplePath>> pxssToAdd = new ArrayList<>();
                        for (ArrayList<TriplePath> px2 : query2.pxss(MFS2)) {
                            Boolean px2Included = false;
                            for (ArrayList<TriplePath> px1 : pxss) {
                                if (px1.containsAll(px2)) {
                                    px2Included = true;
                                    break;
                                }
                            }
                            if (!px2Included) {
                                pxssToAdd.add(px2);
                            }
                        }
                        pxssToAdd.forEach(itr::add);
                    }
                }
            }
        }
        log.info("MFS" + MFSs.toString());
        return MFSs;
    }

    protected ArrayList<ArrayList<TriplePath>> pxss(ArrayList<TriplePath> MFS) {
        ArrayList<ArrayList<TriplePath>> pxss = new ArrayList<>();
        ArrayList<TriplePath> queryTriples = this.getTriples();
        if (queryTriples.size() > 1) {
            for (TriplePath MFSTriple : MFS) {
                ArrayList<TriplePath> pxs = new ArrayList<>(queryTriples);
                pxs.remove(MFSTriple);
                pxss.add(pxs);
            }
        }
        return pxss;
    }

    public TupleQueryResult mayHaveAResult(SailRepository repo) {
        if (this.sourceSelected) { return this.res; }
        TupleQuery query = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, this.serialize());
        this.sourceSelected = true;
        try {
            res = query.evaluate();
            Map<StatementPattern, List<StatementSource>> stmtToSources = QueryInfo.queryInfo.get().getSourceSelection().getStmtToSources();
            for (Map.Entry<StatementPattern, List<StatementSource>> stmtToSource : stmtToSources.entrySet()) {
                if (stmtToSource.getValue().isEmpty()) {
                    res.close();
                    return null;
                }
            }
            return res;
        } catch (FedXRuntimeException ex) {
            log.warn(ex.getMessage());
            if (res != null) { res.close(); }
            return null;
        }
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
