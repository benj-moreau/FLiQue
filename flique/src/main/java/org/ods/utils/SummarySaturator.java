package org.ods.utils;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Saturate an existing FliQuE summary
 * @author BenjaminM
 */
public class SummarySaturator {
    static Logger log = LoggerFactory.getLogger(SummarySaturator.class);
    public static void main(String[] args) throws IOException {
        String outputFile = "summaries/test.n3"; // The saturated summary will be generated in this file
        String summaryFile = "summaries/complete-largeRDFBench-summaries.n3";
        String ontologies = "ontologies/"; // CaLi ordering is in this file
        Model outputModel = RDFDataMgr.loadModel(summaryFile);
        /*
        Model ontoModel = ModelFactory.createDefaultModel();
        final File folder = new File(ontologies);
        for (final File f : folder.listFiles()) {
            ontoModel.read(f.getCanonicalPath());
        }
        StmtIterator stmtIterator = outputModel.listStatements((Resource) null ,ResourceFactory.createProperty("http://aksw.org/quetsal/object"), (RDFNode) null);
        List<Statement> inferedStmt = new LinkedList<>();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.next();
            Resource subjectNode   = stmt.getSubject();
            RDFNode clss = stmt.getObject();
            NodeIterator subClassIter = ontoModel.listObjectsOfProperty((Resource) clss, ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"));
            while (subClassIter.hasNext()) {
                RDFNode superClass = subClassIter.nextNode();
                inferedStmt.add(ResourceFactory.createStatement(subjectNode, ResourceFactory.createProperty("http://aksw.org/quetsal/object"), superClass));
            }
        }
        outputModel.add(inferedStmt);
        */
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        RDFDataMgr.write(out, outputModel, RDFFormat.TTL);
        return;
    }
}
