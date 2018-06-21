# -*- coding: utf-8 -*-

import codecs

tweets = {}
gold_annotations = {}
system_annotations = {}

with codecs.open('datasets/OKE2017/task3/test/oke2017_3.tsv', 'r', 'utf-8') as txt:
    for line in txt:
        tweets[line.replace('\n', '').split('\t')[0]] = line.replace('\n', '').split('\t')[1]
        gold_annotations[line.replace('\n', '').split('\t')[0]] = []
        system_annotations[line.replace('\n', '').split('\t')[0]] = []

with codecs.open('datasets/OKE2017/task3/test/oke2017_3.tac', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])
        """
        if line.split('\t')[3].startswith("NIL"):
            link = "NIL"
        else:
            link = line.split('\t')[3]
        """
        link = line.split('\t')[3]
        tweet = tweets[line.split('\t')[0]]
        ent_type = line.split('\t')[5].replace("\n", "")
        #ent_type = "NO_TYPE"
        gold_annotations[line.split('\t')[0]].append(tweet[start:end] + "----" + str(start)
                                                     + "----" + str(end) + "----" + ent_type
                                                     + "----" + link)

with codecs.open('output_oke20173.tac', 'r', 'utf-8') as gold:
    for line in gold:
        start = int(line.split('\t')[1])
        end = int(line.split('\t')[2])

        if line.split('\t')[3].startswith("NIL"):
            link = "NIL"
        else:
            link = line.split('\t')[3]

        tweet = tweets[line.split('\t')[0]]
        ent_type = line.split('\t')[5].replace("\n", "")
        #ent_type = "NO_TYPE"
        system_annotations[line.split('\t')[0]].append(tweet[start:end] + "----" + str(start)
                                                       + "----" + str(end) + "----" + ent_type
                                                       + "----" + link)

"""
for k, v in tweets.items():
    print (k + " => " + v)
    gold = gold_annotations[k]
    for val in gold:
        split_ann = val.split("----")
        print ("Entity{phrase='" + split_ann[0] + "', startPosition=" + split_ann[1]
               + ", endPosition=" + split_ann[2] + ", " + "type='" + split_ann[3]
               + "', link='" + split_ann[4] + "'}")
"""

for k, v in tweets.items():
    gold_annotations[k].sort(key=lambda x: int(x.split("----")[1]))
    system_annotations[k].sort(key=lambda x: int(x.split("----")[1]))

    gold = gold_annotations[k]
    system = system_annotations[k]
    same_size = False

    if len(gold) != len(system):
        print (k + " => " + v)
        print ("========= GS =========")
        for val in gold:
            split_ann = val.split("----")
            print ("Entity{phrase='" + split_ann[0] + "', startPosition=" + split_ann[1]
                   + ", endPosition=" + split_ann[2] + ", " + "type='" + split_ann[3]
                   + "', link='" + split_ann[4] + "'}")
        print ("========= SYSTEM =========")
        for val in system:
            split_ann = val.split("----")
            print ("Entity{phrase='" + split_ann[0] + "', startPosition=" + split_ann[1]
                   + ", endPosition=" + split_ann[2] + ", " + "type='" + split_ann[3]
                   + "', link='" + split_ann[4] + "'}")
    else:
        same_size = True

    if same_size:
        inside = True
        for val in system:
            if len([x for x in gold if ''.join(map(str, val.split("----")[0:3])) in ''.join(map(str, x.split("----")[0:3]))]) == 0:
                inside = False

        if not inside:
            print (k + " => " + v)
            print ("========= GS =========")
            for val in gold:
                split_ann = val.split("----")
                print ("Entity{phrase='" + split_ann[0] + "', startPosition=" + split_ann[1]
                       + ", endPosition=" + split_ann[2] + ", " + "type='" + split_ann[3]
                       + "', link='" + split_ann[4] + "'}")
            print ("========= SYSTEM =========")
            for val in system:
                split_ann = val.split("----")
                print ("Entity{phrase='" + split_ann[0] + "', startPosition=" + split_ann[1]
                       + ", endPosition=" + split_ann[2] + ", " + "type='" + split_ann[3]
                       + "', link='" + split_ann[4] + "'}")

