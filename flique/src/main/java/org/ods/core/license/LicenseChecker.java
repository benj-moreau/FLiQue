package org.ods.core.license;

import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.SourceSelection;
import org.apache.jena.rdf.model.*;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class LicenseChecker {
    protected static final Logger log = LoggerFactory.getLogger(LicenseChecker.class);

    public Model summary =  ModelFactory.createDefaultModel() ;


    /**
     * Constructor
     *
     * @param summaryPath Path to FLiQuE summary
     */
    public LicenseChecker(String summaryPath) {
        try {
            InputStream summaryIn = new FileInputStream(summaryPath);
            summary.read(summaryIn, " ", "N3");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if all licenses of sources are compatible.
     * Returns a report that represent license conflicts.
     * */
    public int evaluate(SourceSelection sourceSelection, EndpointManager endpointManager) {
        Map<StatementPattern, List<StatementSource>> stmtToSources = sourceSelection.getStmtToSources();
        stmtToSources.forEach((stmt, sources) -> {
            sources.forEach(source -> {
                String endpointURL = endpointManager.getEndpoint(source.getEndpointID()).getEndpoint();
                String license = getLicense(endpointURL);
                List<String> compatibleLicenses = getCompatibleLicenses(license);
            });
        });
        return 1;
    }

    private String getLicense(String endpoint) {
        Resource license = (Resource) this.summary.listObjectsOfProperty(ResourceFactory.createResource(endpoint), ResourceFactory.createProperty("http://www.w3.org/ns/odrl/2/hasPolicy")).next();
        return license.getURI();
    }

    private List<String> getCompatibleLicenses(String license) {
        List<String> compatibleLicenses = new ArrayList<>();
        compatibleLicenses.add(license);
        NodeIterator iter = this.summary.listObjectsOfProperty(ResourceFactory.createResource(license), ResourceFactory.createProperty("http://schema.theodi.org/odrs#compatibleWith"));
        while (iter.hasNext()) {
            Resource compatibleLicense = (Resource) iter.next();
            compatibleLicenses.add(compatibleLicense.getURI());
        }
        return compatibleLicenses;
    }
}
