package org.ods.core.relaxation.similarity;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class TriplePatternSimilarity {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternSimilarity.class);

    public static double compute(TriplePath originalTriple, TriplePath relaxedTriple, Model summary) {
        return (subjectSimilarity(originalTriple.getSubject(), relaxedTriple.getSubject()) +
                predicateSimilarity(originalTriple.getPredicate(), relaxedTriple.getPredicate(), summary) +
                objectSimilarity(originalTriple.getObject(), relaxedTriple.getObject(), summary)
        )/3;
    }

    private static double subjectSimilarity(Node originalSubject, Node relaxedSubject) {
        if (originalSubject.equals(relaxedSubject)) {
            return 1.0;
        } else {
            // it's a simple relaxation
            return 0.0;
        }
    }

    private static double predicateSimilarity(Node originalPredicate, Node relaxedPredicate, Model summary) {
        if (originalPredicate.equals(relaxedPredicate)) {
            return 1.0;
        } else if (relaxedPredicate.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            // it's a property relaxation
            String originalPropertyURI = originalPredicate.getURI();
            String superPropertyURI = relaxedPredicate.getURI();
            int totalTriples = getTriplesNumber(summary);
            return (- Math.log(getTriplesNumber(superPropertyURI, summary)/totalTriples)/
                    - Math.log(getTriplesNumber(originalPropertyURI, summary)/totalTriples));
        }
    }

    private static double objectSimilarity(Node originalObject, Node relaxedObject, Model summary) {
        if (originalObject.equals(relaxedObject)) {
            return 1.0;
        } else if (relaxedObject.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            log.info(originalObject + " not similar to " + relaxedObject);
            // it's a type (class) relaxation
            String originalClassURI = originalObject.getURI();
            String superClassURI = relaxedObject.getURI();
            int totalInstances = getInstancesNumber(summary);
            return (- Math.log(getInstancesNumber(superClassURI, summary)/totalInstances)/
                    - Math.log(getInstancesNumber(originalClassURI, summary)/totalInstances));
        }
    }

    private static int getTriplesNumber(String propertyURI, Model summary) {
        int number = 0;
        ResIterator subjects = summary.listSubjectsWithProperty(ResourceFactory.createProperty("http://aksw.org/quetsal/predicate"), ResourceFactory.createResource(propertyURI));
        while (subjects.hasNext()) {
            Resource subject = subjects.next();
            NodeIterator tripleNumbers = summary.listObjectsOfProperty(subject, ResourceFactory.createProperty("http://aksw.org/quetsal/triples"));
            while (tripleNumbers.hasNext()) {
                number += tripleNumbers.next().asLiteral().getInt();
            }
        }
        return number;
    }

    private static int getTriplesNumber(Model summary) {
        int number = 100000;
        return number;
    }

    private static int getInstancesNumber(String classURI, Model summary) {
        int number = 100000;
        return number;
    }

    private static int getInstancesNumber(Model summary) {
        int number = 100000;
        return number;
    }
}
