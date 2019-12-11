package org.ods.core.relaxation;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;

import java.util.ArrayList;
import java.util.List;

public class RelaxedQuery extends Query implements Comparable<RelaxedQuery> {

    private float similarity;
    private ArrayList<String> relaxationsLog;

    public RelaxedQuery() {
        super();
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

    public List<Triple> getTriples(){
        Op alg = Algebra.compile(this);
        MyOpVisitorBase visitor = new MyOpVisitorBase();
        alg.visit(visitor);
        return visitor.triples;
    }

    class MyOpVisitorBase extends OpVisitorBase
    {
        public List<Triple> triples;
        @Override
        public void visit(final OpBGP opBGP) {
            List<Triple> triples = opBGP.getPattern().getList();
            this.triples = opBGP.getPattern().getList();
        }
    }
}
