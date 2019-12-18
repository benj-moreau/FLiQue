package org.ods.core.relaxation;

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
        if (triple.getPredicate().getURI().equals(type.getURI())) {
            //rdf:type -> type relaxation
           TriplePath relaxedTriple = typeRelaxation(triple, ontology);
           if (relaxedTriple != null) {relaxedTriples.add(relaxedTriple);}
        } else if (!triple.getPredicate().isVariable()) {
            // predicate is not a variable and not a rdf:type -> property relaxation
            TriplePath relaxedTriple = propertyRelaxation(triple, ontology);
            if (relaxedTriple != null) {relaxedTriples.add(relaxedTriple);}
        }
        // simple relaxation
        relaxedTriples.addAll(simpleRelaxation(triple));
        return relaxedTriples;
    }

    private static ArrayList<TriplePath> simpleRelaxation(TriplePath originalTriple) {
        ArrayList<TriplePath> relaxedTriples = new ArrayList<>();
        String varName;
        if (!originalTriple.getSubject().isVariable()) {
            // on subject
            varName = UUID.randomUUID().toString();
            relaxedTriples.add(new TriplePath(NodeFactory.createVariable(varName) ,originalTriple.getPath(), originalTriple.getObject()));
        }
        if (!originalTriple.getPredicate().isVariable()) {
            // on predicate
            varName = UUID.randomUUID().toString();
            relaxedTriples.add(new TriplePath(originalTriple.getSubject(), PathFactory.pathLink(NodeFactory.createVariable(varName)), originalTriple.getObject()));
        }
        if (!originalTriple.getObject().isVariable()) {
            // on object
            varName = UUID.randomUUID().toString();
            relaxedTriples.add(new TriplePath(originalTriple.getSubject() ,originalTriple.getPath(), NodeFactory.createVariable(varName)));
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
}
