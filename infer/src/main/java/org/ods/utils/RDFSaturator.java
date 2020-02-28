package org.ods.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Paths;


/**
 * Saturate an existing RDF dataset
 * @author BenjaminM
 */
public class RDFSaturator {
    static Logger log = LoggerFactory.getLogger(RDFSaturator.class);
    public static void main(String[] args) throws FileNotFoundException {
        Model schema = FileManager.get().loadModel("ontology/ontology.n3");
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
        reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel,
                ReasonerVocabulary.RDFS_SIMPLE);
        Reasoner boundReasoner = reasoner.bindSchema(schema);
        File benchmark_folder = new File("datasets/");
        File[] listOfBench = benchmark_folder.listFiles((dir, name) -> !name.equals(".DS_Store"));
        for (File bench : listOfBench) {
            log.info(bench.getPath());
            File[] listOfDatasets = bench.listFiles((dir, name) -> (name.toLowerCase().startsWith("data-")));
            for (File datasetDir : listOfDatasets) {
                log.info(datasetDir.getPath());
                String toLoadFilePath = datasetDir.getPath() + "/virtuoso/" + "toLoad/";
                File toLoadFolder = new File(toLoadFilePath);
                File[] rdfGraphs = toLoadFolder.listFiles((dir, name) -> !name.equals(".DS_Store") && !name.contains("inf_"));
                for (File rdfGraph : rdfGraphs) {
                    try {
                        String filepath = rdfGraph.getPath();
                        String filename = Paths.get(filepath).getFileName().toString();
                        log.info(filename + " is being inferred");
                        Model data = FileManager.get().loadModel(filepath);
                        InfModel infmodel = ModelFactory.createInfModel(boundReasoner, data);
                        FileOutputStream infFile = new FileOutputStream(toLoadFilePath + "/inf_" + filename, false);
                        RDFDataMgr.write(infFile, infmodel, RDFFormat.TURTLE_BLOCKS);
                        /*
                        File file = new File(filepath);
                        if (file.delete()) {
                            log.info("Deleted the file " + filepath);
                        } else {
                            log.info("Failed to delete " + filepath);
                        }
                        */
                    } catch (Throwable e) {
                        log.info(rdfGraph.getName() + " " + e.toString());
                    }
                }
            }
        }
    }
}
