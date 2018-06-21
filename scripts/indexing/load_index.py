# -*- coding: utf-8 -*-
import ujson
import elasticsearch
import json_lines


props = set()

with json_lines.open('index_dbpedia201510.json') as data_file:
    es = elasticsearch.Elasticsearch([{'host': 'localhost', 'port': 9200}],
                                     timeout=60)

    if es.indices.exists(index="dbpedia201510"):
        es.indices.delete(index="dbpedia201510")

    body = '{"settings": {"index.auto_expand_replicas": "1-all","index.number_of_shards": 1}}'

    es.indices.create(index="dbpedia201510", body=body)

    for line in data_file:
        dico = ujson.loads(line, encoding='utf-8')

        for k, v in dico.items():
            props.update(v.keys())
            es.index(index="dbpedia201510", doc_type="entity", id=k, body=v, timeout='60s')

props.remove('pagerank')
props.remove('link')

with open('props.txt', 'w') as f:
    f.write("-".join(props))
