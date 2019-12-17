package org.ods.core.relaxation;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.apache.jena.vocabulary.RDFS.subClassOf;
import static org.apache.jena.vocabulary.RDF.type;

class TriplePatternRelaxer {
    protected static final Logger log = LoggerFactory.getLogger(TriplePatternRelaxer.class);

    public static ArrayList<TriplePath> relax(TriplePath triple, Model summary, Model ontology) {
        ArrayList<TriplePath> relaxedTriples = new ArrayList<>();
        if (triple.getPredicate().getURI().equals(type.getURI())) {
           TriplePath relaxedTriple = typeRelaxation(triple, ontology);
           if (relaxedTriple != null) {relaxedTriples.add(relaxedTriple);}
           log.info(triple.toString());
           log.info(relaxedTriples.toString());
        }
        return relaxedTriples;
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
