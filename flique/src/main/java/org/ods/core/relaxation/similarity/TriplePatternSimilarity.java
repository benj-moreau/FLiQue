package org.ods.core.relaxation.similarity;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static org.apache.jena.vocabulary.RDF.type;

class TriplePatternSimilarity {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternSimilarity.class);

    public static double compute(TriplePath originalTriple, TriplePath relaxedTriple, Model summary, HashMap<String, Integer> federationClassStatistics, HashMap<String, Integer> federationPropertyStatistics, ArrayList<String> endpoints) {
        return (subjectSimilarity(originalTriple.getSubject(), relaxedTriple.getSubject()) +
                predicateSimilarity(originalTriple.getPredicate(), relaxedTriple.getPredicate(), summary, federationPropertyStatistics, endpoints) +
                objectSimilarity(originalTriple.getObject(), relaxedTriple.getObject(), summary, federationClassStatistics, endpoints)
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

    private static double predicateSimilarity(Node originalPredicate, Node relaxedPredicate, Model summary, HashMap<String, Integer> federationPropertyStatistics, ArrayList<String> endpoints) {
        if (originalPredicate.equals(relaxedPredicate)) {
            return 1.0;
        } else if (relaxedPredicate.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            // it's a property relaxation
            String originalPropertyURI = originalPredicate.getURI();
            String superPropertyURI = relaxedPredicate.getURI();
            int totalTriples = getTriplesNumber(summary, federationPropertyStatistics, endpoints);
            return - Math.log(getTriplesNumber(superPropertyURI, summary, federationPropertyStatistics, endpoints)/ (double) totalTriples)/
                    - Math.log(getTriplesNumber(originalPropertyURI, summary, federationPropertyStatistics, endpoints)/ (double) totalTriples);
        }
    }

    private static double objectSimilarity(Node originalObject, Node relaxedObject, Model summary, HashMap<String, Integer> federationClassStatistics, ArrayList<String> endpoints) {
        if (originalObject.equals(relaxedObject)) {
            return 1.0;
        } else if (relaxedObject.isVariable()) {
            // it's a simple relaxation
            return 0.0;
        } else {
            // it's a type (class) relaxation
            String originalClassURI = originalObject.getURI();
            String superClassURI = relaxedObject.getURI();
            int totalInstances = getInstancesNumber(summary, federationClassStatistics, endpoints);
            return - Math.log(getInstancesNumber(superClassURI, summary, federationClassStatistics, endpoints)/ (double) totalInstances)/
                    - Math.log(getInstancesNumber(originalClassURI, summary, federationClassStatistics, endpoints)/ (double) totalInstances);
        }
    }

    public static int getTriplesNumber(String propertyURI, Model summary, HashMap<String, Integer> federationPropertyStatistics, ArrayList<String> endpoints) {
        if (!federationPropertyStatistics.containsKey(propertyURI)) {
            int number = 0;
            StmtIterator endpointsIter = summary.listStatements(
                    null,
                    ResourceFactory.createProperty("http://aksw.org/quetsal/url"),
                    (RDFNode) null);
            while (endpointsIter.hasNext()) {
                Statement endpointStmt = endpointsIter.next();
                String endpoint = endpointStmt.getObject().toString();
                if (endpoints.contains(endpoint)) {
                    Resource endpointNode = endpointStmt.getSubject();
                    NodeIterator capabilitiesIter = summary.listObjectsOfProperty(endpointNode, ResourceFactory.createProperty("http://aksw.org/quetsal/capability"));
                    while (capabilitiesIter.hasNext()) {
                        Resource capabilityNode = capabilitiesIter.nextNode().asResource();
                        if (summary.contains(capabilityNode, ResourceFactory.createProperty("http://aksw.org/quetsal/predicate"), ResourceFactory.createResource(propertyURI))) {
                            NodeIterator tripleNumbers = summary.listObjectsOfProperty(capabilityNode, ResourceFactory.createProperty("http://aksw.org/quetsal/triples"));
                            while (tripleNumbers.hasNext()) {
                                number += tripleNumbers.next().asLiteral().getInt();
                            }
                        }
                    }
                }
            }
            federationPropertyStatistics.put(propertyURI, number);
        }
        return federationPropertyStatistics.get(propertyURI);
    }

    public static int getTriplesNumber(Model summary, HashMap<String, Integer> federationPropertyStatistics, ArrayList<String> endpoints) {
        if (!federationPropertyStatistics.containsKey("totalTriples")) {
            int number = 0;
            StmtIterator endpointsIter = summary.listStatements(
                    null,
                    ResourceFactory.createProperty("http://aksw.org/quetsal/url"),
                    (RDFNode) null);
            while (endpointsIter.hasNext()) {
                Statement endpointStmt = endpointsIter.next();
                String endpoint = endpointStmt.getObject().toString();
                if (endpoints.contains(endpoint)) {
                    Resource endpointNode = endpointStmt.getSubject();
                    NodeIterator tripleNumbers = summary.listObjectsOfProperty(endpointNode, ResourceFactory.createProperty("http://aksw.org/quetsal/totalTriples"));
                    while (tripleNumbers.hasNext()) {
                        number += tripleNumbers.next().asLiteral().getInt();
                    }
                }
            }
            federationPropertyStatistics.put("totalTriples", number);
        }
        return federationPropertyStatistics.get("totalTriples");
    }

    public static int getInstancesNumber(String classURI, Model summary, HashMap<String, Integer> federationClassStatistics, ArrayList<String> endpoints) {
        if (!federationClassStatistics.containsKey(classURI)) {
            int number = 0;
            StmtIterator endpointsIter = summary.listStatements(
                    null,
                    ResourceFactory.createProperty("http://aksw.org/quetsal/url"),
                    (RDFNode) null);
            while (endpointsIter.hasNext()) {
                Statement endpointStmt = endpointsIter.next();
                String endpoint = endpointStmt.getObject().toString();
                if (endpoints.contains(endpoint)) {
                    Resource endpointNode = endpointStmt.getSubject();
                    NodeIterator capabilitiesIter = summary.listObjectsOfProperty(endpointNode, ResourceFactory.createProperty("http://aksw.org/quetsal/capability"));
                    while (capabilitiesIter.hasNext()) {
                        Resource capabilityNode = capabilitiesIter.nextNode().asResource();
                        if (summary.contains(capabilityNode, ResourceFactory.createProperty("http://aksw.org/quetsal/predicate"), type)) {
                            NodeIterator ClassIter = summary.listObjectsOfProperty(capabilityNode, ResourceFactory.createProperty("http://aksw.org/quetsal/topObjs"));
                            while (ClassIter.hasNext()) {
                                Resource objNode = ClassIter.next().asResource();
                                if (summary.contains(objNode, ResourceFactory.createProperty("http://aksw.org/quetsal/object"), ResourceFactory.createResource(classURI))) {
                                    NodeIterator instancesNumbers = summary.listObjectsOfProperty(objNode, ResourceFactory.createProperty("http://aksw.org/quetsal/card"));
                                    while (instancesNumbers.hasNext()) {
                                        number += instancesNumbers.next().asLiteral().getInt();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            federationClassStatistics.put(classURI, number);
        }
        return federationClassStatistics.get(classURI);
    }

    public static int getInstancesNumber(Model summary, HashMap<String, Integer> federationClassStatistics, ArrayList<String> endpoints) {
        if (!federationClassStatistics.containsKey("distinctSbjs")) {
            int number = 0;
            StmtIterator endpointsIter = summary.listStatements(
                    null,
                    ResourceFactory.createProperty("http://aksw.org/quetsal/url"),
                    (RDFNode) null);
            while (endpointsIter.hasNext()) {
                Statement endpointStmt = endpointsIter.next();
                String endpoint = endpointStmt.getObject().toString();
                if (endpoints.contains(endpoint)) {
                    Resource endpointNode = endpointStmt.getSubject();
                    NodeIterator instancesNumbers = summary.listObjectsOfProperty(endpointNode, ResourceFactory.createProperty("http://aksw.org/quetsal/totalSbj"));
                    while (instancesNumbers.hasNext()) {
                        number += instancesNumbers.next().asLiteral().getInt();
                    }
                }
            }
            federationClassStatistics.put("distinctSbjs", number);
        }
        return federationClassStatistics.get("distinctSbjs");
    }
}
