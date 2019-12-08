from rdflib import Graph, URIRef, RDFS
import os
import copy


def get_super_classes_transitive(original_class=None, super_class=None):
    super_classes = []
    if super_class:
        super_classes.append(super_class)
    else:
        super_class = original_class
    for new_super_class in ontology.objects(super_class, RDFS.subClassOf):
        super_classes += get_super_classes_transitive(super_class=new_super_class)
    return super_classes


def add_all_super_classes(summary, saturated_summary):
    for node, entity_class in summary.subject_objects(URIRef("http://aksw.org/quetsal/object")):
        super_classes = get_super_classes_transitive(entity_class)
        for super_class in super_classes:
            saturated_summary.add((node, URIRef("http://aksw.org/quetsal/object"), super_class))


def get_super_properties_transitive(original_property=None, super_property=None):
    super_properties = []
    if super_property:
        super_properties.append(super_property)
    else:
        super_property = original_property
    for new_super_property in ontology.objects(super_property, RDFS.subPropertyOf):
        super_properties += get_super_properties_transitive(super_property=new_super_property)
    return super_properties


def add_all_super_properties(summary, saturated_summary):
    for node, property in summary.subject_objects(URIRef("http://aksw.org/quetsal/predicate")):
        super_properties = get_super_properties_transitive(property)
        for super_property in super_properties:
            print(str(super_property))
            saturated_summary.add((node, URIRef("http://aksw.org/quetsal/predicate"), super_property))


summaryFile = "../../../../../../summaries/complete-largeRDFBench-summaries.n3"
outputFile = "../../../../../../summaries/saturated-largeRDFBench-summaries.n3"
ontologiesDir = "../../../../../../ontologies/"

summary = Graph().parse(summaryFile, format='n3')
saturated_summary = copy.deepcopy(summary)

ontology = Graph()
for r, d, f in os.walk(ontologiesDir):
    for file in f:
        if file.lower().endswith('.rdf') or file.lower().endswith('.xml') or file.lower().endswith('.owl'):
            ontology.parse(os.path.join(r, file))

add_all_super_classes(summary, saturated_summary)
add_all_super_properties(summary, saturated_summary)

# add super properties
saturated_summary.serialize(destination=outputFile, format='n3')