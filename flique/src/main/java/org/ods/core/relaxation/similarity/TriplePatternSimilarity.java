package org.ods.core.relaxation.similarity;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


class TriplePatternSimilarity {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternSimilarity.class);

    public static double compute(TriplePath originalTriple, TriplePath relaxedTriple, Model summary, HashMap<String, Integer> federationClassStatistics, HashMap<String, Integer> federationPropertyStatistics) {
        return (subjectSimilarity(originalTriple.getSubject(), relaxedTriple.getSubject()) +
                predicateSimilarity(originalTriple.getPredicate(), relaxedTriple.getPredicate(), summary, federationPropertyStatistics) +
                objectSimilarity(originalTriple.getObject(), relaxedTriple.getObject(), summary, federationClassStatistics)
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

    private static double predicateSimilarity(Node originalPredicate, Node relaxedPredicate, Model summary, HashMap<String, Integer> federationPropertyStatistics) {
        if (originalPredicate.equals(relaxedPredicate)) {
            return 1.0;
        } else if (relaxedPredicate.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            // it's a property relaxation
            String originalPropertyURI = originalPredicate.getURI();
            String superPropertyURI = relaxedPredicate.getURI();
            int totalTriples = getTriplesNumber(summary, federationPropertyStatistics);
            return - Math.log(getTriplesNumber(superPropertyURI, summary, federationPropertyStatistics)/ (double) totalTriples)/
                    - Math.log(getTriplesNumber(originalPropertyURI, summary, federationPropertyStatistics)/ (double) totalTriples);
        }
    }

    private static double objectSimilarity(Node originalObject, Node relaxedObject, Model summary, HashMap<String, Integer> federationClassStatistics) {
        if (originalObject.equals(relaxedObject)) {
            return 1.0;
        } else if (relaxedObject.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            // it's a type (class) relaxation
            String originalClassURI = originalObject.getURI();
            String superClassURI = relaxedObject.getURI();
            int totalInstances = getInstancesNumber(summary, federationClassStatistics);
            return - Math.log(getInstancesNumber(superClassURI, summary, federationClassStatistics)/ (double) totalInstances)/
                    - Math.log(getInstancesNumber(originalClassURI, summary, federationClassStatistics)/ (double) totalInstances);
        }
    }

    private static int getTriplesNumber(String propertyURI, Model summary, HashMap<String, Integer> federationPropertyStatistics) {
        if (!federationPropertyStatistics.containsKey(propertyURI)) {
            int number = 0;
            ResIterator subjects = summary.listSubjectsWithProperty(ResourceFactory.createProperty("http://aksw.org/quetsal/predicate"), ResourceFactory.createResource(propertyURI));
            while (subjects.hasNext()) {
                Resource subject = subjects.next();
                NodeIterator tripleNumbers = summary.listObjectsOfProperty(subject, ResourceFactory.createProperty("http://aksw.org/quetsal/triples"));
                while (tripleNumbers.hasNext()) {
                    number += tripleNumbers.next().asLiteral().getInt();
                }
            }
            federationPropertyStatistics.put(propertyURI, number);
        }
        return federationPropertyStatistics.get(propertyURI);
    }

    private static int getTriplesNumber(Model summary, HashMap<String, Integer> federationPropertyStatistics) {
        if (!federationPropertyStatistics.containsKey("totalTriples")) {
            int number = 0;
            NodeIterator tripleNumbers = summary.listObjectsOfProperty(ResourceFactory.createProperty("http://aksw.org/quetsal/totalTriples"));
            while (tripleNumbers.hasNext()) {
                number += tripleNumbers.next().asLiteral().getInt();
            }
            federationPropertyStatistics.put("totalTriples", number);
        }
        return federationPropertyStatistics.get("totalTriples");
    }

    private static int getInstancesNumber(String classURI, Model summary, HashMap<String, Integer> federationClassStatistics) {
        if (!federationClassStatistics.containsKey(classURI)) {
            int number = 0;
            ResIterator subjects = summary.listSubjectsWithProperty(ResourceFactory.createProperty("http://aksw.org/quetsal/object"), ResourceFactory.createResource(classURI));
            while (subjects.hasNext()) {
                Resource subject = subjects.next();
                NodeIterator instancesNumbers = summary.listObjectsOfProperty(subject, ResourceFactory.createProperty("http://aksw.org/quetsal/card"));
                while (instancesNumbers.hasNext()) {
                    number += instancesNumbers.next().asLiteral().getInt();
                }
            }
            federationClassStatistics.put(classURI, number);
        }
        return federationClassStatistics.get(classURI);
    }

    private static int getInstancesNumber(Model summary, HashMap<String, Integer> federationClassStatistics) {
        if (!federationClassStatistics.containsKey("distinctSbjs")) {
            int number = 0;
            NodeIterator instancesNumbers = summary.listObjectsOfProperty(ResourceFactory.createProperty("http://aksw.org/quetsal/distinctSbjs"));
            while (instancesNumbers.hasNext()) {
                number += instancesNumbers.next().asLiteral().getInt();
            }
            federationClassStatistics.put("distinctSbjs", number);
        }
        return federationClassStatistics.get("distinctSbjs");
    }
}
