#!/usr/bin/env python
# -*- coding: utf-8 -*-

import rdflib
import sys
import re
import os


def init(args):
    g = rdflib.Graph()

    if args.url:
        g.parse(args.url, format="turtle")
    else:
        g.parse(args.file, format="turtle")

    qres = g.query("""select distinct ?sentence (str(?tagTxt) as ?txt) where {
            ?sentence a <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context> .
            ?sentence <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString> ?tagTxt .
            }""")

    for row in qres:
        qres2 = g.query("""select distinct (str(?beginIndex) as ?begin) (str(?endIndex) as ?end) ?type (str(?label) as ?str) (str(?anchorOf) as ?mention) ?sameas where {
            ?mention <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext> <""" +
                        row["sentence"] + """> .
            ?mention <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex> ?beginIndex .
            ?mention <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex> ?endIndex .
            ?mention <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?entity .
            ?mention <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?anchorOf .
            ?entity a ?type .
            ?entity <http://www.w3.org/2000/01/rdf-schema#label> ?label .
            OPTIONAL {
                ?entity <http://www.w3.org/2002/07/owl#sameAs> ?sameas .
            }
            FILTER(STRSTARTS(STR(?type),"http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#"))
            } order by asc (?beginIndex)""")
        m = re.search("sentence-([0-9]+)#char=0,[0-9]+$", row["sentence"])

        print (m.group(1) + " => " + row["txt"])

        for row2 in qres2:
            link = row2["sameas"]

            if not row2["sameas"]:
                link = "NIL"
            print (
            "Mention={phrase=" + row2["mention"] + ", startIndex=" + row2["begin"] + ", endIndex=" +
            row2["end"] + "}, Entity={label=" + row2["str"] + ", type=" + row2["type"].split("#")[
                1] + ", link=" + link + "}")


def check_python():
    """Check if a compatible version of Python is installed"""
    if (sys.version_info[:2] > (2, 6) or sys.version_info[:2] > (3, 1)):
        return True

    return False


def file_exists(x):
    if not os.path.isfile(x):
        import argparse
        raise argparse.ArgumentTypeError("{0} is not a file".format(x))
    return x


def main():
    if check_python():
        import argparse

        parser = argparse.ArgumentParser(
            description="Display in a human reading way the sentences and their annotations from a NIF file.",
            prog="checknif")
        group = parser.add_mutually_exclusive_group(required=True)

        parser.add_argument('--version', action='version', version='%(prog)s 1.0.0')
        group.add_argument('-f', '--file', type=file_exists, help="NIF file")
        group.add_argument('-u', '--url', help="URL where the NIF file is")
        parser.set_defaults(func=init)

        args = parser.parse_args()

        args.func(args)
    else:
        print("To use this script Python 2.7, Python 3.2 or higher has to be installed.")


if __name__ == '__main__':
    main()
