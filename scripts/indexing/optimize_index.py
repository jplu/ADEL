# -*- coding: utf-8 -*-

import json
import codecs

json1 = """{
    "NEEL2016-dev": {
        "Earth----http://dbpedia.org/resource/Earth": ["P1"],
        "EtihadAirways----http://dbpedia.org/resource/Etihad_Airways": ["P2","P3"],
        "FBI----http://dbpedia.org/resource/Federal_Bureau_of_Investigation": ["P3"]
    }
}"""

json2 = """{
    "NEEL2016-dev": {
        "Earth----http://dbpedia.org/resource/Earth": ["P1","P4","P6","P7"],
        "EtihadAirways----http://dbpedia.org/resource/Etihad_Airways": ["P1"],
        "FBI----http://dbpedia.org/resource/Federal_Bureau_of_Investigation": ["P4","P5","P6"],
        "FauziaKasuri----http://dbpedia.org/resource/Fauzia_Kasuri": ["P2"]
    }
}"""

courant = set()
temp = set()
final = set()
#dico1 = json.loads(json1)
dico1 = {}
dico2 = json.loads(json2)

with codecs.open('matrix.json', 'r', 'utf-8') as data_file:
    dico1 = json.load(data_file, encoding='utf-8')

for k, v in dico1.items():
    for couple, props in v.items():
        for elem in props:
            courant.add(elem)
        if len(courant) == 1 and len(courant.intersection(final)) == 0:
            final = final.union(courant)
        elif len(courant.intersection(final)) == 0 and len(courant.intersection(temp)) > 0:
            temp.add(next(iter(courant.intersection(temp))))
            #
        else:
            temp = courant
        courant = set()
if len(temp) > 0:
    final.add(next(iter(temp)))

print (final)
