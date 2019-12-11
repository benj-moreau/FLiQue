package org.ods.core.relaxation;

import org.apache.jena.graph.Triple;

public class TriplePatternRelaxer {
    public Triple originalTriple;

    TriplePatternRelaxer(Triple originalTriple){
        this.originalTriple = originalTriple;
    }
}
