# -*- coding: utf-8 -*-

import codecs
import ujson
import requests
import elasticsearch
from elasticsearch import helpers
import time
import re
import operator
import collections
import sys
import itertools

query = u"""
{{
  "from": 0,
  "size": 10000,
  "_source":["link"],
  "query": {{
    "bool": {{
      "must": {{
        "query_string": {{
          "fields": ["dbo_wikiPageWikiLinkText","dbo_wikiPageRedirects","rdfs_label","dbo_pseudonym"],
          "query": "{0}",
          "default_operator": "OR",
          "fuzziness": "auto"
        }}
      }},
      "filter": {{
        "bool": {{
          "should": [
            {{
              "terms": {{
                "rdf_type.keyword":["dbo_Place", "dbo_Person", "dbo_Award", "dbo_Organisation", "dbo_EthnicGroup", "dbo_Event", "dbo_MeanOfTransportation", "dbo_Holiday", "dbo_CelestialBody", "dbo_Work"]
              }}
            }},
            {{
              "bool": {{
                "must_not": {{
                  "exists": {{
                    "field": "rdf_type"
                  }}
                }}
              }}
            }}
          ],
          "minimum_should_match": 1
        }}
      }}
    }}
  }}
}}
"""

query2 = u"""
{{
  "from": 0,
  "size": 10000,
  "_source":["link"],
  "query": {{
    "bool": {{
      "must": {{
        "query_string": {{
          "fields": ["dbo_wikiPageWikiLinkText","dbo_wikiPageRedirects","rdfs_label","dbo_pseudonym"],
          "query": "{0}",
          "default_operator": "OR",
          "fuzziness": "auto"
        }}
      }},
      "filter": {{
        "bool": {{
          "should": [
            {{
              "terms": {{
                "rdf_type.keyword":["http://dbpedia.org/ontology/Place", "http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/Organisation"]
              }}
            }},
            {{
              "bool": {{
                "must_not": {{
                  "exists": {{
                    "field": "rdf_type"
                  }}
                }}
              }}
            }}
          ],
          "minimum_should_match": 1
        }}
      }}
    }}
  }}
}}
"""

query3 = u"""
{{
  "from": 0,
  "size": 10000,
  "_source":["link"],
  "query": {{
    "bool": {{
      "must": {{
        "query_string": {{
          "fields": ["dbo_wikiPageWikiLinkText","dbo_wikiPageRedirects","rdfs_label","dbo_pseudonym"],
          "query": "{0}",
          "default_operator": "OR",
          "fuzziness": "auto"
        }}
      }}
    }}
  }}
}}
"""

query4 = u"""
{{
  "from": 0,
  "size": 10000,
  "_source":["link"],
  "query": {{
    "bool": {{
      "must": {{
        "query_string": {{
          "fields": ["dbo_wikiPageWikiLinkText","dbo_wikiPageRedirects","rdfs_label","dbo_pseudonym"],
          "query": "{0}",
          "default_operator": "AND",
          "fuzziness": "auto"
        }}
      }}
    }}
  }}
}}
"""

es = elasticsearch.Elasticsearch([{'host': 'localhost', 'port': 80, 'url_prefix': 'es'}])


def combination_words(term):
    final_str = ""
    for i in range(1, len(term.split()) + 1):
        for value in itertools.combinations(term.split(), i):
            tmp = "\""
            for index in range(0, i):
                tmp += value[index] + " "
            tmp = tmp[:-1] + "\""
            final_str += tmp + ","
    return final_str[:-1]


def search(term):
    words = []

    for val in re.sub('[ ]{2,}', ' ', term.strip()).split(" "):
        if '-' not in val and '.' not in val and ')' not in val:
            words.append(val + '~')
        else:
            words.append(val)

    new_term = ' '.join(words)
    #new_term = term #+ "*"
    #new_term = term

    #combination = combination_words(term)
    """
    tmp = query.encode("utf-8").format(new_term.strip().replace(':', '\\\:').replace('/', '\\\/')
                                       .replace('!', '\\\!'), combination, combination, combination,
                                       combination)
    """
    tmp = query2.encode("utf-8").format(new_term.strip().replace(':', '\\\:').replace('/', '\\\/')
                                       .replace('!', '\\\!'))
    #print(new_term.strip().replace(':', '\\\:').replace('/', '\\\/').replace('!', '\\\!'))

    res_query = es.search(index="dbpedia201510", doc_type='entity', body=tmp, request_timeout=120)
    #res_query = elasticsearch.helpers.scan(client=es, query=ujson.loads(tmp), index="dbpedia201510",
    #                                       doc_type='entity')


    links = []
    #dico = {}
    #print (res_query)
    for hit in res_query["hits"]["hits"]:
        #print(hit)
        links.append(hit["_source"]["link"])

    #    dico[hit["_source"]["link"].replace(u'\u2013', '-')] = float(hit["_source"]["pagerank"])
    #sorted_links = collections.OrderedDict(sorted(dico.items(), key=operator.itemgetter(1),
    # reverse=True))
    #links = [val for val in sorted_links]

    if len(links) == 0:
        links.append("NIL")

    return links

with codecs.open('dataset_oke_test2015.json', 'r', 'utf-8') as data_file:
    data = ujson.load(data_file)
    total = 0
    datasets = {}
    min_index = 0
    max_index = 0
    average_candidates = 0

    for k, v in data.items():
        print (k)

        good = 0
        not_good = 0

        for k2, v2 in v.items():
            total += 1
            links = search(k2.split("----")[0].encode("utf-8"))

            average_candidates += len(links)

            if k2.split("----")[1] in links:
                good += 1

                if max_index == 0:
                    max_index = links.index(k2.split("----")[1].encode("utf-8")) + 1
                if min_index == 0:
                    min_index = links.index(k2.split("----")[1].encode("utf-8")) + 1

                max_index = max(max_index, links.index(k2.split("----")[1]) + 1)
                min_index = min(min_index, links.index(k2.split("----")[1]) + 1)

            else:
                not_good += 1

                print("search: " + k2.split("----")[0] + " ===> not good")
        datasets[k] = str(good) + "," + str(not_good)

    print("total entities: " + str(total))
    print("min index: " + str(min_index))
    print("max index: " + str(max_index))
    print("average number of candidates: " + str(average_candidates / total))

    for k, v in datasets.items():
        total_dataset = int(v.split(',')[0]) + int(v.split(',')[1])
        pourcentage_ok = (float(v.split(',')[0]) * 100) / total_dataset
        pourcentage_notok = (float(v.split(',')[1]) * 100) / total_dataset

        print(k + " contains " + str(total_dataset) + " entities: " + v.split(',')[0] +
               " OK (%.2f" % pourcentage_ok + "%) and " + v.split(',')[1] +
               " not OK (%.2f" % pourcentage_notok + "%)")
