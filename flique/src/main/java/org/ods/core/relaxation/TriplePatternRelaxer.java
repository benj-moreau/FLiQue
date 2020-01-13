package org.ods.core.relaxation;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.PathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.ArrayList;

import static org.apache.jena.vocabulary.RDFS.subClassOf;
import static org.apache.jena.vocabulary.RDF.type;
import static org.apache.jena.vocabulary.RDFS.subPropertyOf;

class TriplePatternRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternRelaxer.class);

    public static ArrayList<TriplePath> relax(TriplePath triple, Model summary, Model ontology) {
        ArrayList<TriplePath> relaxedTriples = new ArrayList<>();
        if (!triple.getPredicate().isVariable()) {
            if (triple.getPredicate().getURI().equals(type.getURI())) {
                //rdf:type -> type relaxation
                TriplePath relaxedTriple = typeRelaxation(triple, ontology);
                if (relaxedTriple != null) {relaxedTriples.add(relaxedTriple);}
            } else {
                // predicate is not a variable and not a rdf:type -> property relaxation
                TriplePath relaxedTriple = propertyRelaxation(triple, ontology);
                if (relaxedTriple != null) {relaxedTriples.add(relaxedTriple);}
            }
        }
        // simple relaxation
        relaxedTriples.addAll(simpleRelaxation(triple));
        return relaxedTriples;
    }

    private static ArrayList<TriplePath> simpleRelaxation(TriplePath originalTriple) {
        ArrayList<TriplePath> relaxedTriples = new ArrayList<>();
        if (!originalTriple.getSubject().isVariable()) {
            // on subject
            relaxedTriples.add(new TriplePath(generateUniqueVariable() ,originalTriple.getPath(), originalTriple.getObject()));
        }
        if (!originalTriple.getPredicate().isVariable()) {
            // on predicate
            // if property is relaxed to a variable, we also relax object to a variable.
            relaxedTriples.add(new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(generateUniqueVariable()), generateUniqueVariable()));
        }
        if (!originalTriple.getObject().isVariable()) {
            // on object
            if (!originalTriple.getPredicate().isVariable() && originalTriple.getPredicate().getURI().equals(type.getURI())) {
                // if class is relaxed to a variable, then we do the same for rdf:type predicate.
                relaxedTriples.add(new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(generateUniqueVariable()), generateUniqueVariable()));
            } else {
                relaxedTriples.add(new TriplePath(originalTriple.getSubject(), originalTriple.getPath(), generateUniqueVariable()));
            }
        }
        return relaxedTriples;
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
