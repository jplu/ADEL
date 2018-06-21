# -*- coding: utf-8 -*-

import SPARQLWrapper
import json
import os
import codecs
import urllib2

sparql = SPARQLWrapper.SPARQLWrapper("http://localhost:7780/sparql")

sparql.setTimeout(500)
sparql.setReturnFormat(SPARQLWrapper.JSON)


def process_query1():
    dico = {}
    limit = 1
    offset = 0

    if os.path.exists('offset'):
        with codecs.open('offset', encoding='utf-8') as f:
            for line in f:
                offset = int(line)

    while True:
        query = """
        PREFIX mo: <http://purl.org/ontology/mo/>
        PREFIX dbo: <http://dbpedia.org/ontology/>
        SELECT DISTINCT ?s ?id ?pr
        FROM <http://musicbrainz.org> WHERE {
            ?s mo:musicbrainz_guid ?id .
            ?s dbo:wikiPageRank ?pr .
        } limit """ + str(limit) + """ offset """ + str(offset)

        sparql.setQuery(query)

        ok = True

        while ok:
            try:
                results = sparql.query().convert()
                ok = False
            except (SPARQLWrapper.SPARQLExceptions.EndPointNotFound, urllib2.HTTPError), e:
                print("again")
                continue

        if not results["results"]["bindings"]:
            break

        for result in results["results"]["bindings"]:
            dico[str(result["id"]["value"])] = {}
            dico[str(result["id"]["value"])]['link'] = result["s"]["value"]
            dico[str(result["id"]["value"])]['pagerank'] = float(result["pr"]["value"])

        process_query2(dico)

        with codecs.open('offset', 'w', encoding='utf-8') as f:
            offset += limit
            f.write(str(offset))

        dico.clear()


def process_query2(dico):
    link = ''.join([val['link'] for key, val in dico.items()])
    key = ''.join([key for key, val in dico.items()])
    query = """
    PREFIX dbo: <http://dbpedia.org/ontology/>
    SELECT DISTINCT ?p (GROUP_CONCAT(DISTINCT ?o;separator="-----") AS ?vals)
    FROM <http://musicbrainz.org> WHERE {
        {
            <""" + link + """> ?p ?o .
            FILTER(DATATYPE(?o) = xsd:string || LANG(?o) = "en") .
        } UNION {
            VALUES ?p {rdf:type} .
            <""" + link + """> ?p ?o .
            FILTER(CONTAINS(str(?o), "http://purl.org/ontology/mo/")) .
        }
    }
    """

    sparql.setQuery(query)

    ok = True

    while ok:
        try:
            results = sparql.query().convert()
            ok = False
        except (SPARQLWrapper.SPARQLExceptions.EndPointNotFound, urllib2.HTTPError), e:
            continue

    for result in results['results']['bindings']:
        dico[key][result["p"]["value"]
            .replace('http://purl.org/ontology/mo/', 'mo_')
            .replace('http://purl.org/dc/elements/1.1/', 'dc_')
            .replace('http://www.w3.org/2002/07/owl#', 'owl_')
            .replace('http://purl.org/NET/c4dm/event.owl#', 'event_')
            .replace('http://www.w3.org/2000/01/rdf-schema#', 'rdfs_')
            .replace('http://www.w3.org/2004/02/skos/core#', 'skos_')
            .replace('http://open.vocab.org/terms/', 'terms_')
            .replace('http://xmlns.com/foaf/0.1/', 'foaf_')
            .replace('http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf_')] = \
            result["vals"]["value"].split('-----')

    with codecs.open('index_musicbrainz201612.json', 'a', encoding='utf-8') as f:
        f.write(json.dumps(dico, ensure_ascii=False))
        f.write("\n")


def main():
    process_query1()
    os.remove('offset')

if __name__ == '__main__':
    main()
