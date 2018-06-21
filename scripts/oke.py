# -*- coding: utf-8 -*-

"""
    oke
    ======================
 
    Use this script to do or reproduce experiments on OKE challenge. This script is compatible with Python 2.7, 3.2, 3.3, 3.4, 3.5 and 3.6.

    Examples:

    Init OKE2017 task1 with x-validation
    $ python scripts/oke.py init -y 2017 -t 1 -x

    Init OKE2017 task1 without x-validation
    $ python scripts/oke.py init -y 2017 -t 1

    Init OKE2017 task2 with x-validation
    $ python scripts/oke.py init -y 2017 -t 2 -x

    Init OKE2017 task2 without x-validation
    $ python scripts/oke.py init -y 2017 -t 2

    Init OKE2017 task3 with x-validation
    $ python scripts/oke.py init -y 2017 -t 3 -x

    Init OKE2017 task3 without x-validation
    $ python scripts/oke.py init -y 2017 -t 3

    Init OKE2016 task1 with x-validation
    $ python scripts/oke.py -y 2016 -t 1 -x

    Init OKE2016 task1 without x-validation
    $ python scripts/oke.py -y 2016 -t 1

    Init OKE2015 task1 with x-validation
    $ python scripts/oke.py -y 2015 -t 1

    Init OKE2015 task1 without x-validation
    $ python scripts/oke.py -y 2015 -t 1

    Train NER over OKE2017 task3 with x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -x -y 2017 -t 3

    Train NER over OKE2017 task3 without x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -y 2017 -t 3

    Train NER over OKE2016 task1 with x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -x -y 2016 -t 1

    Train NER over OKE2016 task1 without x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -y 2016 -t 1

    Train NER over OKE2015 task1 with x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -x -y 2015 -t 1

    Train NER over OKE2015 task1 without x-validation
    $ python scripts/oke.py train-ner -j /path/to/stanford.jar -y 2015 -t 1

    Test NER over OKE2017 task3 with x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -x -y 2017 -t 3

    Test NER over OKE2017 task3 without x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -y 2017 -t 3

    Test NER over OKE2016 task1 with x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -x -y 2016 -t 1

    Test NER over OKE2016 task1 without x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -y 2016 -t 1

    Test NER over OKE2015 task1 with x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -x -y 2015 -t 1

    Test NER over OKE2015 task1 without x-validation
    $ python scripts/oke.py test-ner -j /path/to/stanford.jar -y 2015 -t 1

    Test ADEL over OKE2017 task1 with x-validation
    $ python scripts/oke.py adel -x -y 2017 -t 1

    Test ADEL over OKE2017 task1 without x-validation
    $ python scripts/oke.py adel -y 2017 -t 1

    Test ADEL over OKE2017 task2 with x-validation
    $ python scripts/oke.py adel -x -y 2017 -t 2

    Test ADEL over OKE2017 task2 without x-validation
    $ python scripts/oke.py adel -y 2017 -t 2

    Test ADEL over OKE2017 task3 with x-validation
    $ python scripts/oke.py adel -x -y 2017 -t 3

    Test ADEL over OKE2017 task3 without x-validation
    $ python scripts/oke.py adel -y 2017 -t 3

    Test ADEL over OKE2016 task1 with x-validation
    $ python scripts/oke.py adel -x -y 2016 -t 1

    Test ADEL over OKE2016 task1 without x-validation
    $ python scripts/oke.py adel -y 2016 -t 1

    Test ADEL over OKE2015 task1 with x-validation
    $ python scripts/oke.py adel -x -y 2015 -t 1

    Test ADEL over OKE2015 task1 without x-validation
    $ python scripts/oke.py adel -y 2015 -t 1
"""

import sys
import codecs
import os
import shutil
import subprocess
import copy
import rdflib
from rdflib.plugins.sparql import prepareQuery
import random
import tempfile
import re
import tailer


def k_fold_cross_validation(items, k, randomize=False):
    if randomize:
        items = list(items)
        random.shuffle(items)

    slices = [items[i::k] for i in range(k)]

    for i in range(k):
        validation = slices[i]
        training = [item for s in slices if s is not validation for item in s]
        yield training, validation

"""
def split_conll(inFile):
    sentences = []

    with open(inFile) as f:
        lines = f.read().splitlines()
        sentence = []
        for line in lines:
            sentence.append(line)
            if not line:
                sentences.append(sentence)
                sentence = []

    return sentences


def write_dataset(dataset, name):
    tmp_dataset = copy.deepcopy(dataset)

    with open(name + ".conll", 'w') as f:
        for sentences in tmp_dataset:
            sentences.pop()
            f.writelines('\n'.join(sentences))
            f.writelines('\n\n')
"""


def write_dataset(dataset, name, in_file):
    tmp_dataset = copy.deepcopy(dataset)
    g = rdflib.Graph()
    g_out = rdflib.Graph()

    g.parse(in_file, format="turtle")

    for sentence in tmp_dataset:
        query = prepareQuery("""CONSTRUCT {
            <""" + sentence + """> ?p ?o .
            ?s nif:referenceContext <""" + sentence + """> .
            ?s ?p2 ?o2 .
            ?o2 ?p3 ?o3 .
        } WHERE {
            <""" + sentence + """> ?p ?o .
            ?s nif:referenceContext <""" + sentence + """> .
            ?s ?p2 ?o2
            OPTIONAL {
                ?o2 ?p3 ?o3 .
            }
        }""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
                                       "itsrdf": "http://www.w3.org/2005/11/its/rdf#"})

        for row in g.query(query):
            g_out.add(row)

    g_out.serialize(destination=name + '.ttl', format='turtle')


def sed(pattern, replace, source):
    """
    Reads a source file and writes the result inplace. In each line, replaces pattern with replace.
    """
    with codecs.open(source, 'r', 'utf-8') as fin:
        _, name = tempfile.mkstemp()

        with codecs.open(name, 'w', 'utf-8') as fout:
            for line in fin:
                out = re.sub(pattern, replace, line)

                fout.write(out)

    shutil.move(name, source)


def split_nif(in_file):
    g = rdflib.Graph()
    sentences = []

    g.parse(in_file, format="turtle")

    query = prepareQuery("""SELECT DISTINCT ?s WHERE{
        ?s a nif:Context .
    }""", initNs={"nif": "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#"})

    for row in g.query(query):
        sentences.append(row['s'].toPython().strip())

    return sentences


def create_fold_cross_validation_environment(number, location):
    path_dir = os.path.dirname(os.path.normpath(location)) + '/cross_validation'

    if os.path.exists(path_dir):
        shutil.rmtree(path_dir)

    os.makedirs(path_dir)

    # lines = split_conll(location)
    lines = split_nif(location)
    i = 1

    for training, validation in k_fold_cross_validation(lines, number):
        folder = path_dir + '/' + str(i)

        os.makedirs(folder)

        write_dataset(training, folder + "/train", location)
        write_dataset(validation, folder + "/test", location)

        i += 1


def check_python():
    """Check if a compatible version of Python is installed"""
    if sys.version_info[:2] > (2, 6) or sys.version_info[:2] > (3, 1):
        return True

    return False


def run_command(command, print_output=True):
    if print_output:
        print(command)

    subprocess.check_call(command, shell=True)


def init(args):
    if args.x_fold_validation:
        create_fold_cross_validation_environment(4, "datasets/oke" + str(args.year) + "/task" + str(args.task) + "/train/dataset_task_" + str(args.task) + ".ttl")

        for i in range(1, 5):
            if (args.task == 3 and args.year == 2017) or args.year == 2015 or args.year == 2016:
                run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2conll -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/train.ttl") + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/train.conll") + " -s oke" + str(args.year) + str(args.task))
                run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2conll -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.ttl") + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.conll") + " -s oke" + str(args.year) + str(args.task))
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2tac -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.ttl") + " -oa " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.tac") + " -ot " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".tsv") + " -s oke" + str(args.year) + str(args.task))
    else:
        if (args.task == 3 and args.year == 2017) or args.year == 2015 or args.year == 2016:
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2conll -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/dataset_task_" + str(args.task) + ".ttl") + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + ".conll") + " -s oke" + str(args.year) + str(args.task))
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2conll -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/evaluation-dataset-task" + str(args.task) + ".ttl") + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".conll") + " -s oke" + str(args.year) + str(args.task))
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools nif2tac -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/evaluation-dataset-task" + str(args.task) + ".ttl") + " -oa " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".tac") + " -ot " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".tsv") + " -s oke" + str(args.year) + str(args.task))


def train_ner(args):
    if (args.task == 3 and args.year == 2017) or args.year == 2015 or args.year == 2016:
        if args.x_fold_validation:
            for i in range(1, 5):
                if args.year == 2015 or args.year == 2016:
                    #sed("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Role", "O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/train.conll"))
                    sed(r"^(he|her|his|she|him)	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person", r"\1	O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/train.conll"))

                run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -prop " + os.path.abspath('scripts/stanford.prop') + " -trainFile " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/train.conll") + " -serializeTo " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/OKE" + str(args.year) + "_" + str(args.task) + "_" + str(i) + ".ser.gz"))
        else:
            if args.year == 2015 or args.year == 2016:
                #sed("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Role", "O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + ".conll"))
                sed(r"^(he|her|his|she|him)	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person", r"\1	O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + ".conll"))

            run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -prop " + os.path.abspath('scripts/stanford.prop') + " -trainFile " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + ".conll") + " -serializeTo " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + "_" + str(args.task) + ".ser.gz"))
    else:
        print ("There is nothing to train for the tasks 1 and 2 of OKE 2017")


def test_ner(args):
    if (args.task == 3 and args.year == 2017) or args.year == 2015 or args.year == 2016:
        if args.x_fold_validation:
            for i in range(1, 5):
                if args.year == 2015 or args.year == 2016:
                    #sed("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Role", "O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.conll"))
                    sed(r"^(he|her|his|she|him)	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person", r"\1	O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.conll"))

                run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -ner.useSUTime false -loadClassifier " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/OKE" + str(args.year) + "_" + str(args.task) + "_" + str(i) + ".ser.gz") + " -testFile " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.conll") + " > " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/stanford.conll"))

                print('\n'.join([str(element) for element in tailer.tail(open(os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/stanford.conll")), 6)]))
        else:
            if args.year == 2015 or args.year == 2016:
                #sed("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Role", "O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".conll"))
                sed(r"^(he|her|his|she|him)	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Person", r"\1	O", os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".conll"))

            run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -ner.useSUTime false -loadClassifier " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/OKE" + str(args.year) + "_" + str(args.task) + ".ser.gz") + " -testFile " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/OKE" + str(args.year) + ".conll") + " > " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/stanford.conll"))

            print('\n'.join([str(element) for element in tailer.tail(open(os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/stanford.conll")), 6)]))
    else:
        print ("There is nothing to test for the tasks 1 and 2 of OKE 2017")


def adel(args):
    if args.x_fold_validation:
        for i in range(1, 5):
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar nerd -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.ttl") + " -s oke" + str(args.year) + str(args.task) + str(i) + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/results.tac"))
            run_command("python -m neleval evaluate -m all -f tab -g " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/test.tac") + " " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/train/cross_validation/" + str(i) + "/results.tac"))
    else:
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar nerd -i " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/evaluation-dataset-task" + str(args.task) + ".ttl") + " -s oke" + str(args.year) + str(args.task) + " -o " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/results.tac"))
        run_command("python -m neleval evaluate -m all -f tab -g " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/test.tac") + " " + os.path.abspath("datasets/OKE" + str(args.year) + "/task" + str(args.task) + "/test/results.tac"))


def dir_exists(x):
    if not os.path.isdir(x):
        import argparse
        raise argparse.ArgumentTypeError("{0} is not a directory".format(x))
    return x


def file_exists(x):
    if not os.path.isfile(x):
        import argparse
        raise argparse.ArgumentTypeError("{0} is not a file".format(x))
    return x


def main():
    if check_python():
        import argparse

        parser = argparse.ArgumentParser(description="Run and train ADEL over the OKE dataset", prog="oke")

        parser.add_argument('--version', action='version', version='%(prog)s 1.0.0')

        subparsers = parser.add_subparsers(help='sub-commands')

        parser_init = subparsers.add_parser("init", help="OKE init")

        parser_init.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding OKE edition")
        parser_init.add_argument("-t", "--task", required=True, type=int, help="The number of the task")
        parser_init.add_argument("-x", "--x-fold-validation", action="store_true", help="If X-cross validation is needed or not")
        parser_init.set_defaults(func=init)

        parser_train_ner = subparsers.add_parser("train-ner", help="Train the NER model over OKE")

        parser_train_ner.add_argument("-j", "--stanford-jar", required=True, type=file_exists, help="Location of the Stanford CoreNLP jar")
        parser_train_ner.add_argument("-x", "--x-fold-validation", action="store_true", help="If X-cross validation is needed or not")
        parser_train_ner.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding OKE edition")
        parser_train_ner.add_argument("-t", "--task", required=True, type=int, help="The number of the task")
        parser_train_ner.set_defaults(func=train_ner)

        parser_test_ner = subparsers.add_parser("test-ner", help="Test the NER model over OKE")

        parser_test_ner.add_argument("-j", "--stanford-jar", required=True, type=file_exists, help="Location of the Stanford CoreNLP jar")
        parser_test_ner.add_argument("-x", "--x-fold-validation", action="store_true", help="If X-cross validation is needed or not")
        parser_test_ner.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding OKE edition")
        parser_test_ner.add_argument("-t", "--task", required=True, type=int, help="The number of the task")
        parser_test_ner.set_defaults(func=test_ner)

        parser_adel = subparsers.add_parser("adel", help="Run ADEL over OKE")

        parser_adel.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding OKE edition")
        parser_adel.add_argument("-t", "--task", required=True, type=int, help="The number of the task")
        parser_adel.add_argument("-x", "--x-fold-validation", action="store_true", help="If X-cross validation is needed or not")
        parser_adel.set_defaults(func=adel)

        args = parser.parse_args()

        args.func(args)
    else:
        print("To use this script Python 2.7, Python 3.2 or higher has to be installed.")


if __name__ == '__main__':
    main()
