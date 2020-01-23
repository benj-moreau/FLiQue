package org.ods.core.relaxation;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.PathFactory;
import org.ods.core.relaxation.similarity.QuerySimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.apache.jena.vocabulary.RDFS.subClassOf;
import static org.apache.jena.vocabulary.RDF.type;
import static org.apache.jena.vocabulary.RDFS.subPropertyOf;

public class TriplePatternRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternRelaxer.class);

    public static TriplePath relax(TriplePath triple, Model ontology, QuerySimilarity querySimilarity) {
        TriplePath relaxedTriple = null;
        if (!triple.getPredicate().isVariable()) {
            if (triple.getPredicate().getURI().equals(type.getURI())) {
                //rdf:type -> type relaxation
                relaxedTriple = typeRelaxation(triple, ontology);
            } else {
                // predicate is not a variable and not a rdf:type -> property relaxation
                relaxedTriple = propertyRelaxation(triple, ontology);
            }
        }
        // simple relax
        if (relaxedTriple == null) {relaxedTriple = simpleRelaxation(triple);}
        return relaxedTriple;
    }

    private static TriplePath simpleRelaxation(TriplePath originalTriple) {
        TriplePath relaxedTriple = null;
        if (!originalTriple.getObject().isVariable()) {
            // on object
            if (!originalTriple.getPredicate().isVariable() && originalTriple.getPredicate().getURI().equals(type.getURI())) {
                // if class is relaxed to a variable, then we do the same for rdf:type predicate.
                relaxedTriple = new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(generateUniqueVariable()), generateUniqueVariable());
            } else {
                relaxedTriple = new TriplePath(originalTriple.getSubject(), originalTriple.getPath(), generateUniqueVariable());
            }
        }
        else if (!originalTriple.getSubject().isVariable()) {
            // on subject
            relaxedTriple = new TriplePath(generateUniqueVariable() ,originalTriple.getPath(), originalTriple.getObject());
        }
        else if (!originalTriple.getPredicate().isVariable()) {
            // on predicate
            relaxedTriple = new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(generateUniqueVariable()), originalTriple.getObject());
        }
        return relaxedTriple;
    }

    private static TriplePath propertyRelaxation(TriplePath originalTriple, Model ontology) {
        String propertyURI = originalTriple.getPredicate().getURI();
        NodeIterator superPropertyIter = ontology.listObjectsOfProperty(ResourceFactory.createResource(propertyURI), subPropertyOf);
        if (superPropertyIter.hasNext()) {
            RDFNode superProperty = superPropertyIter.next();
            return new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(NodeFactory.createURI(superProperty.asResource().getURI())), originalTriple.getObject());
        } else {
            return null;
        }
    }

    private static TriplePath typeRelaxation(TriplePath originalTriple, Model ontology) {
        String classURI = originalTriple.getObject().getURI();
        NodeIterator superClassIter = ontology.listObjectsOfProperty(ResourceFactory.createResource(classURI), subClassOf);
        if (superClassIter.hasNext()) {
            RDFNode superClass = superClassIter.next();
            return new TriplePath(originalTriple.getSubject(), originalTriple.getPath(), NodeFactory.createURI(superClass.asResource().getURI()));
        } else {
            return null;
        }
    }

    private static Node generateUniqueVariable() {
        String varName = UUID.randomUUID().toString().replace("-", "");
        return NodeFactory.createVariable(varName);
    }
}
