package org.ods.core.relaxation.similarity;

import org.ods.core.relaxation.RelaxedQuery;

import java.util.ArrayList;

public class TriplePatternSimilarity {
    RelaxedQuery originalTriplePattern;
    // store originalTP cardinalities in variable during instanciation

    public TriplePatternSimilarity() {
    }

    public double compute(RelaxedQuery relaxedQuery) {
        return 1.0;
    }
}
