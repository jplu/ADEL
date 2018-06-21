# -*- coding: utf-8 -*-
import codecs
import logging
import sys

logging.basicConfig()

gold_annotations = {}
tweets = {}

if sys.maxunicode < 0x10ffff:
    print ("Python should be compiled with with unicode UCS-4.")
    sys.exit(1)


with codecs.open('datasets/LeMonde/test/lemonde.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        tweets[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]
        gold_annotations[line.replace('\n', '').split('\t')[0]] = []

with codecs.open('datasets/LeMonde/test/annotations.tsv', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        tweet = tweets[line.split('\t')[0]]
        gold_annotations[line.split('\t')[0]].append(tweet[start:end] + "----" + str(start)
                                                     + "----" + str(end))

with codecs.open('annotated_lemonde.tac', 'w', 'utf-8') as out:
    for k, v in gold_annotations.items():
        txt = tweets[k]

        sorted_lst = sorted(v, key=lambda colonnes: int(colonnes.split("----")[1]))
        for i in range(0, len(sorted_lst)):
            if i == 0:
                if int(sorted_lst[i].split("----")[1]) == 0:
                    txt = "[[" + sorted_lst[i].split("----")[0] + "]]" +\
                          txt[int(sorted_lst[i].split("----")[2]):]
                else:
                    txt = txt[:int(sorted_lst[i].split("----")[1])] +\
                          "[[" + sorted_lst[i].split("----")[0] + "]]"\
                          + txt[int(sorted_lst[i].split("----")[2]):]
            else:
                txt = txt[:int(sorted_lst[i].split("----")[1]) + i * 4] +\
                      "[[" + sorted_lst[i].split("----")[0] + "]]" +\
                      txt[int(sorted_lst[i].split("----")[2]) + i * 4:]
        txt += "\n"
        out.write(k + "\t" + txt)
