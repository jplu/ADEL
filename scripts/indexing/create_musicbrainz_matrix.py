# -*- coding: utf-8 -*-

import elasticsearch
import requests
import ujson
import codecs
import re

query = u"""{{
    "_source":["link"],
    "query": {{
        "query_string":{{
            "fields": ["{0}"],
            "query": "{1}",
            "default_operator": "AND",
            "fuzziness": "auto"
        }}
    }}
}}"""

props = []
res = requests.get("http://localhost:9200")
final_json = {}

if res.status_code == 200:
    es = elasticsearch.Elasticsearch([{'host': 'localhost', 'port': 9200}])

    with codecs.open('props_musicbrainz.txt', 'r', 'utf-8') as data_file:
        for line in data_file:
            props = line.split('-')

    with codecs.open('dataset_musicbrainz.json', 'r', 'utf-8') as data_file:
        data = ujson.load(data_file)

        for k, v in data.items():
            final_json[k] = {}

            for k2, v2 in v.items():
                good_props = []

                for prop in props:
                    words = []

                    for val in re.sub('[ ]{2,}', ' ', k2.split('----')[0].strip()).split(" "):
                        if '-' not in val and '.' not in val and ')' not in val:
                            words.append(val + '~')
                        else:
                            words.append(val)

                    new_term = ' '.join(words)
                    tmp = query.encode("utf-8").format(prop, new_term.encode("utf-8").split(
                        '----')[0].strip().replace(':', '\\\:').replace('/', '\\\/').replace(
                        '!', '\\\!').replace('"', "\\\""))

                    print(tmp.decode("utf-8"))

                    res = es.search(index="musicbrainz201612", size=10000, body=tmp,
                                    request_timeout=90, doc_type=['entity'])

                    for hit in res['hits']['hits']:
                        if hit["_source"]["link"].replace(u'\u2013', '-') == \
                                k2.split('----')[1]:
                            good_props.append(prop)
                final_json[k][k2] = good_props
    with open('matrix_musicbrainz.json', 'w') as f:
        f.write(ujson.dumps(final_json, sort_keys=True, indent=4, ensure_ascii=False,
                            escape_forward_slashes=False))
