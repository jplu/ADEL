# -*- coding: utf-8 -*-
import codecs
import ujson
import rdflib
from rdflib.plugins.sparql import prepareQuery
import logging

logging.basicConfig()

tweets = {}
res = {}

query2 = prepareQuery("""SELECT DISTINCT * WHERE{
    ?s nif:anchorOf ?mention .
    ?s itsrdf:taIdentRef ?link .
    FILTER(!strStarts(str(?link), "http://aksw.org")) .
}""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
              "itsrdf": "http://www.w3.org/2005/11/its/rdf#"})

graph = rdflib.Graph()

graph.parse('../../data/oke2017/task3/train/dataset_task_3.ttl', format='n3')

res["OKE2017-T3-training"] = {}

for row in graph.query(query2):
    res["OKE2017-T3-training"]["----".join((row['mention'].toPython().strip(),
                                            row['link'].toPython().strip()))] = []

with codecs.open('dataset_musicbrainz.json', 'w', 'utf-8') as f:
    f.write(ujson.dumps(res, sort_keys=True, indent=4, ensure_ascii=False, escape_forward_slashes=False).decode("utf-8"))
