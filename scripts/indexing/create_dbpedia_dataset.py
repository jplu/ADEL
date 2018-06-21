# -*- coding: utf-8 -*-
import codecs
import json
import rdflib
from rdflib.plugins.sparql import prepareQuery
import logging
from bs4 import BeautifulSoup
import requests
import sys
import wordsegment

logging.basicConfig()

if sys.maxunicode < 0x10ffff:
    print ("Python should be compiled with with unicode UCS-4.")
    sys.exit(1)


def get_user_mention(mention):
    response = requests.get('http://twitter.com/' + mention)
    soup = BeautifulSoup(response.content, "html.parser")

    if "Twitter /" in soup.title.string:
        return ""
    else:
        return soup.title.string.split('(')[0].strip()


def segment(text):
    well_formed = ' '.join(wordsegment.segment(text))
    count = 0
    space_count = 0

    for char in well_formed:
        if " " != char:
            if text[count].isupper():
                well_formed = well_formed[:count + space_count] + well_formed[count + space_count].upper() + well_formed[count + space_count + 1:]
            count += 1
        else:
            space_count += 1

    return well_formed

res = {}
nils_res = {}
res["NEEL2016-dev"] = {}
nils_res["NEEL2016-dev"] = {}
neel_dev2016 = {}
nils_neel_dev2016 = {}
neel_dev2016["NEEL2016-dev"] = {}
nils_neel_dev2016["NEEL2016-dev"] = {}
documents = {}

with codecs.open('../../datasets/NEEL2016/dev/NEEL2016-dev-tweets.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/NEEL2016/dev/NEEL2016-dev-gold.tsv', 'r', 'utf-8') as gold:
    for line in gold:
        if not line.split('\t')[3].startswith("NIL"):
            start = int(line.split('\t')[1])
            end = int(line.split('\t')[2])
            link = line.split('\t')[3]
            document = documents[line.split('\t')[0]]

            if not line.split('\t')[3].startswith("NIL"):
                if document[start-1] == "@":
                    user_mention = get_user_mention(document[start:end])
                    if not user_mention:
                        res["NEEL2016-dev"]["----".join(("@" + document[start:end], link))] = []
                        neel_dev2016["NEEL2016-dev"]["----".join(("@" + document[start:end], link))] = []
                    else:
                        res["NEEL2016-dev"]["----".join((user_mention, link))] = []
                        neel_dev2016["NEEL2016-dev"]["----".join((user_mention, link))] = []
                elif document[start-1] == "#":
                    res["NEEL2016-dev"]["----".join((segment(document[start:end]), link))] = []
                    neel_dev2016["NEEL2016-dev"]["----".join((segment(document[start:end]), link))] = []
                else:
                    res["NEEL2016-dev"]["----".join((document[start:end], link))] = []
                    neel_dev2016["NEEL2016-dev"]["----".join((document[start:end], link))] = []
        else:
            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])
                if not user_mention:
                    nils_res["NEEL2016-dev"]["----".join(("@" + document[start:end], "NIL"))] = []
                    nils_neel_dev2016["NEEL2016-dev"]["----".join(("@" + document[start:end], "NIL"))] = []
                else:
                    nils_res["NEEL2016-dev"]["----".join((user_mention, "NIL"))] = []
                    nils_neel_dev2016["NEEL2016-dev"]["----".join((user_mention, "NIL"))] = []
            elif document[start-1] == "#":
                nils_res["NEEL2016-dev"]["----".join((segment(document[start:end]), "NIL"))] = []
                nils_neel_dev2016["NEEL2016-dev"]["----".join((segment(document[start:end]), "NIL"))] = []
            else:
                nils_res["NEEL2016-dev"]["----".join((document[start:end], "NIL"))] = []
                nils_neel_dev2016["NEEL2016-dev"]["----".join((document[start:end], "NIL"))] = []

documents = {}
res["NEEL2016-test"] = {}
nils_res["NEEL2016-test"] = {}
neel_test2016 = {}
nils_neel_test2016 = {}
neel_test2016["NEEL2016-test"] = {}
nils_neel_test2016["NEEL2016-test"] = {}

with codecs.open('../../datasets/NEEL2016/test/NEEL2016-test-tweets.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/NEEL2016/test/NEEL2016-test-gold.tsv', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        link = line.split('\t')[3]
        document = documents[line.split('\t')[0]]

        if not line.split('\t')[3].startswith("NIL"):
            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])
                if not user_mention:
                    res["NEEL2016-test"]["----".join(("@" + document[start:end], link))] = []
                    neel_test2016["NEEL2016-test"]["----".join(("@" + document[start:end], link))] = []
                else:
                    res["NEEL2016-test"]["----".join((user_mention, link))] = []
                    neel_test2016["NEEL2016-test"]["----".join((user_mention, link))] = []
            elif document[start-1] == "#":
                res["NEEL2016-test"]["----".join((segment(document[start:end]), link))] = []
                neel_test2016["NEEL2016-test"]["----".join((segment(document[start:end]), link))] = []
            else:
                res["NEEL2016-test"]["----".join((document[start:end], link))] = []
                neel_test2016["NEEL2016-test"]["----".join((document[start:end], link))] = []
        else:
            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])
                if not user_mention:
                    nils_res["NEEL2016-test"]["----".join(("@" + document[start:end], "NIL"))] = []
                    nils_neel_test2016["NEEL2016-test"]["----".join(("@" + document[start:end], "NIL"))] = []
                else:
                    nils_res["NEEL2016-test"]["----".join((user_mention, "NIL"))] = []
                    nils_neel_test2016["NEEL2016-test"]["----".join((user_mention, "NIL"))] = []
            elif document[start-1] == "#":
                nils_res["NEEL2016-test"]["----".join((segment(document[start:end]), "NIL"))] = []
                nils_neel_test2016["NEEL2016-test"]["----".join((segment(document[start:end]), "NIL"))] = []
            else:
                nils_res["NEEL2016-test"]["----".join((document[start:end], "NIL"))] = []
                nils_neel_test2016["NEEL2016-test"]["----".join((document[start:end], "NIL"))] = []


documents = {}
res["NEEL2016-training"] = {}
nils_res["NEEL2016-training"] = {}
neel_training2016 = {}
nils_neel_training2016 = {}
neel_training2016["NEEL2016-training"] = {}
nils_neel_training2016["NEEL2016-training"] = {}

with codecs.open('../../datasets/NEEL2016/train/NEEL2016-training-tweets.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/NEEL2016/train/NEEL2016-training-gold.tsv', 'r', 'utf-8') as gold:
    for line in gold:
        if not line.split('\t')[3].startswith("NIL"):
            start = int(line.split('\t')[1])
            end = int(line.split('\t')[2])
            link = line.split('\t')[3]
            document = documents[line.split('\t')[0]]

            if not line.split('\t')[3].startswith("NIL"):
                if document[start-1] == "@":
                    user_mention = get_user_mention(document[start:end])
                    if not user_mention:
                        res["NEEL2016-training"]["----".join(("@" + document[start:end], link))] = []
                        neel_training2016["NEEL2016-training"]["----".join(("@" + document[start:end], link))] = []
                    else:
                        res["NEEL2016-training"]["----".join((user_mention, link))] = []
                        neel_training2016["NEEL2016-training"]["----".join((user_mention, link))] = []
                elif document[start-1] == "#":
                    res["NEEL2016-training"]["----".join((segment(document[start:end]), link))] = []
                    neel_training2016["NEEL2016-training"]["----".join((segment(document[start:end]), link))] = []
                else:
                    res["NEEL2016-training"]["----".join((document[start:end], link))] = []
                    neel_training2016["NEEL2016-training"]["----".join((document[start:end], link))] = []
        else:
            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])
                if not user_mention:
                    nils_res["NEEL2016-training"]["----".join(("@" + document[start:end], "NIL"))] = []
                    nils_neel_training2016["NEEL2016-training"]["----".join(("@" + document[start:end], "NIL"))] = []
                else:
                    nils_res["NEEL2016-training"]["----".join((user_mention, "NIL"))] = []
                    nils_neel_training2016["NEEL2016-training"]["----".join((user_mention, "NIL"))] = []
            elif document[start-1] == "#":
                nils_res["NEEL2016-training"]["----".join((segment(document[start:end]), "NIL"))] = []
                nils_neel_training2016["NEEL2016-training"]["----".join((segment(document[start:end]), "NIL"))] = []
            else:
                nils_res["NEEL2016-training"]["----".join((document[start:end], "NIL"))] = []
                nils_neel_training2016["NEEL2016-training"]["----".join((document[start:end], "NIL"))] = []

documents = {}
res["NEEL2014-training"] = {}
neel_training2014 = {}
neel_training2014["NEEL2014-training"] = {}

with codecs.open('../../datasets/NEEL2014/train/NEEL2014-training-tweets.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/NEEL2014/train/neel2014.tac', 'r', 'utf-8') as gold:
    for line in gold:
        if not line.split('\t')[3].startswith("NIL"):
            start = int(line.split('\t')[1])
            end = int(line.split('\t')[2])
            link = line.split('\t')[3]
            document = documents[line.split('\t')[0]]

            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])

                if not user_mention:
                    res["NEEL2014-training"]["----".join(("@" + document[start:end], link))] = []
                    neel_training2014["NEEL2014-training"]["----".join(("@" + document[start:end], link))] = []
                else:
                    res["NEEL2014-training"]["----".join((user_mention, link))] = []
                    neel_training2014["NEEL2014-training"]["----".join((user_mention, link))] = []
            elif document[start-1] == "#":
                res["NEEL2014-training"]["----".join((segment(document[start:end]), link))] = []
                neel_training2014["NEEL2014-training"]["----".join((segment(document[start:end]), link))] = []
            else:
                res["NEEL2014-training"]["----".join((document[start:end], link))] = []
                neel_training2014["NEEL2014-training"]["----".join((document[start:end], link))] = []

documents = {}
res["NEEL2014-test"] = {}
neel_test2014 = {}
neel_test2014["NEEL2014-test"] = {}

with codecs.open('../../datasets/NEEL2014/test/NEEL2014-test-tweets.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/NEEL2014/test/neel2014.tac', 'r', 'utf-8') as gold:
    for line in gold:
        if not line.split('\t')[3].startswith("NIL"):
            start = int(line.split('\t')[1])
            end = int(line.split('\t')[2])
            link = line.split('\t')[3]
            document = documents[line.split('\t')[0]]

            if document[start-1] == "@":
                user_mention = get_user_mention(document[start:end])

                if not user_mention:
                    res["NEEL2014-test"]["----".join(("@" + document[start:end], link))] = []
                    neel_test2014["NEEL2014-test"]["----".join(("@" + document[start:end], link))] = []
                else:
                    res["NEEL2014-training"]["----".join((user_mention, link))] = []
                    neel_test2014["NEEL2014-test"]["----".join((user_mention, link))] = []
            elif document[start-1] == "#":
                res["NEEL2014-test"]["----".join((segment(document[start:end]), link))] = []
                neel_test2014["NEEL2014-test"]["----".join((segment(document[start:end]), link))] = []
            else:
                res["NEEL2014-test"]["----".join((document[start:end], link))] = []
                neel_test2014["NEEL2014-test"]["----".join((document[start:end], link))] = []

query = prepareQuery("""SELECT DISTINCT * WHERE{
    ?s nif:anchorOf ?mention .
    FILTER(!regex(str(?mention), "he|her|his|she|him", "i")) .
    ?s itsrdf:taIdentRef ?ref .
    ?ref owl:sameAs ?link .
}""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
              "itsrdf": "http://www.w3.org/2005/11/its/rdf#",
              "owl": "http://www.w3.org/2002/07/owl#"})

query_nils = prepareQuery("""SELECT DISTINCT * WHERE{
    ?s nif:anchorOf ?mention .
    FILTER(!regex(str(?mention), "he|her|his|she|him", "i")) .
    ?s itsrdf:taIdentRef ?ref .
    FILTER NOT EXISTS{?ref owl:sameAs ?link .}
}""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
              "itsrdf": "http://www.w3.org/2005/11/its/rdf#",
              "owl": "http://www.w3.org/2002/07/owl#"})

res["OKE2015-training"] = {}
nils_res["OKE2015-training"] = {}
oke_training2015 = {}
nils_oke_training2015 = {}
oke_training2015["OKE2015-training"] = {}
nils_oke_training2015["OKE2015-training"] = {}
documents = {}

with codecs.open('../../datasets/OKE2015/task1/train/oke2015.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/OKE2015/task1/train/oke2015.tac', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        link = line.split('\t')[3]
        document = documents[line.split('\t')[0]]

        if not document[start:end].lower() in ["he", "her", "his", "she", "him"]:
            if not line.split('\t')[3].startswith("NIL"):
                res["OKE2015-training"]["----".join((document[start:end], link))] = []
                oke_training2015["OKE2015-training"]["----".join((document[start:end], link))] = []
            else:
                nils_res["OKE2015-training"]["----".join((document[start:end], "NIL"))] = []
                nils_oke_training2015["OKE2015-training"]["----".join((document[start:end], "NIL"))] = []

res["OKE2015-test"] = {}
nils_res["OKE2015-test"] = {}
oke_test2015 = {}
nils_oke_test2015 = {}
oke_test2015["OKE2015-test"] = {}
nils_oke_test2015["OKE2015-test"] = {}
documents = {}

with codecs.open('../../datasets/OKE2015/task1/test/oke2015.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/OKE2015/task1/test/oke2015.tac', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        link = line.split('\t')[3]
        document = documents[line.split('\t')[0]]

        if not document[start:end].lower() in ["he", "her", "his", "she", "him"]:
            if not line.split('\t')[3].startswith("NIL"):
                res["OKE2015-test"]["----".join((document[start:end], link))] = []
                oke_test2015["OKE2015-test"]["----".join((document[start:end], link))] = []
            else:
                nils_res["OKE2015-test"]["----".join((document[start:end], "NIL"))] = []
                nils_oke_test2015["OKE2015-test"]["----".join((document[start:end], "NIL"))] = []

graph = rdflib.Graph()

graph.parse('../../datasets/oke2016/task1/train/dataset_task_1.ttl', format='n3')

res["OKE2016-training"] = {}

for row in graph.query(query):
    res["OKE2016-training"]["----".join((row['mention'].toPython().strip(),
                                      row['link'].toPython().strip()))] = []

graph = rdflib.Graph()

graph.parse('../../datasets/oke2016/task1/test/evaluation-dataset-task1.ttl', format='n3')

res["OKE2016-test"] = {}
nils_res["OKE2016-test"] = {}
oke_test2016 = {}
nils_oke_test2016 = {}
oke_test2016["OKE2016-test"] = {}
nils_oke_test2016["OKE2016-test"] = {}
documents = {}

with codecs.open('../../datasets/OKE2016/task1/test/oke2016.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/OKE2016/task1/test/oke2016.tac', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        link = line.split('\t')[3]
        document = documents[line.split('\t')[0]]

        if not document[start:end].lower() in ["he", "her", "his", "she", "him"]:
            if not line.split('\t')[3].startswith("NIL"):
                res["OKE2016-test"]["----".join((document[start:end], link))] = []
                oke_test2016["OKE2016-test"]["----".join((document[start:end], link))] = []
            else:
                nils_res["OKE2016-test"]["----".join((document[start:end], "NIL"))] = []
                nils_oke_test2016["OKE2016-test"]["----".join((document[start:end], "NIL"))] = []

res["AIDA-test-b"] = {}
nils_res["AIDA-test-b"] = {}
aida_testb = {}
aida_testb["AIDA-test-b"] = {}
documents = {}

with codecs.open('../../datasets/AIDA/test/b/AIDA-testb-text.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        documents[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]

with codecs.open('../../datasets/AIDA/test/b/AIDA-testb-gold-updated.tsv', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        link = line.split('\t')[3]
        document = documents[line.split('\t')[0]]

        res["AIDA-test-b"]["----".join((document[start:end], link))] = []
        aida_testb["AIDA-test-b"]["----".join((document[start:end], link))] = []

query2 = prepareQuery("""SELECT DISTINCT * WHERE{
    ?s nif:anchorOf ?mention .
    ?s itsrdf:taIdentRef ?link .
    FILTER(!strStarts(str(?link), "http://aksw.org")) .
}""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
              "itsrdf": "http://www.w3.org/2005/11/its/rdf#"})

graph = rdflib.Graph()

graph.parse('../../datasets/oke2017/task1/train/dataset_task_1.ttl', format='n3')

res["OKE2017-T1-training"] = {}

for row in graph.query(query2):
    res["OKE2017-T1-training"]["----".join((row['mention'].toPython().strip(),
                                         row['link'].toPython().strip()))] = []

graph = rdflib.Graph()

graph.parse('../../datasets/oke2017/task2/train/dataset_task_2.ttl', format='n3')

res["OKE2017-T2-training"] = {}

for row in graph.query(query2):
    res["OKE2017-T2-training"]["----".join((row['mention'].toPython().strip(),
                                            row['link'].toPython().strip()))] = []

graph = rdflib.Graph()

graph.parse('../../datasets/oke2017/task1/test/evaluation-dataset-task1.ttl', format='n3')

res["OKE2017-T1-test"] = {}

for row in graph.query(query2):
    res["OKE2017-T1-test"]["----".join((row['mention'].toPython().strip(),
                                            row['link'].toPython().strip()))] = []

graph = rdflib.Graph()

graph.parse('../../datasets/oke2017/task2/test/evaluation-dataset-task2.ttl', format='n3')

res["OKE2017-T2-test"] = {}

for row in graph.query(query2):
    res["OKE2017-T2-test"]["----".join((row['mention'].toPython().strip(),
                                            row['link'].toPython().strip()))] = []

with codecs.open('dataset_neel_training2014.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(neel_training2014, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_test2014.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(neel_test2014, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_dev2016.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(neel_dev2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_dev2016_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_neel_dev2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_test2016.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(neel_test2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_test2016_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_neel_test2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_training2016.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(neel_training2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_neel_training2016_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_neel_training2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_training2015.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(oke_training2015, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_training2015_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_oke_training2015, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_test2015.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(oke_test2015, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_test2015_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_oke_test2015, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_test2016.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(oke_test2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_oke_test2016_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_oke_test2016, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_aida_testb.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(aida_testb, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(res, sort_keys=True, indent=4, ensure_ascii=False)))

with codecs.open('dataset_nils.json', 'w', 'utf-8') as f:
    f.write(str(json.dumps(nils_res, sort_keys=True, indent=4, ensure_ascii=False)))