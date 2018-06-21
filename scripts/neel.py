# coding=utf-8

"""
    neel
    ======================

    Use this script to do or reproduce experiments on NEEL challenge. This script is compatible with Python 2.7, 3.2, 3.3, 3.4, 3.5 and 3.6.

    Examples:

    Init NEEL2014
    $ python scripts/neel.py init -y 2014

    Init NEEL2015 for test dataset
    $ python scripts/neel.py init -y 2015

    Init NEEL2015 for dev dataset
    $ python scripts/neel.py init -y 2015 -d

    Init NEEL2016 for test dataset
    $ python scripts/neel.py init -y 2016

    Init NEEL2016 for dev dataset
    $ python scripts/neel.py init -y 2016 -d

    Train NER for the NEEL2015 test dataset
    $ python scripts/neel.py train-ner -j /path/to/stanford.jar -y 2015

    Train NER for the NEEL2015 dev dataset
    $ python scripts/neel.py train-ner -j /path/to/stanford.jar -y 2015 -d

    Train NER for the NEEL2016 test dataset
    $ python scripts/neel.py train-ner -j /path/to/stanford.jar -y 2016

    Train NER for the NEEL2016 dev dataset
    $ python scripts/neel.py train-ner -j /path/to/stanford.jar -y 2016 -d

    Test NER over the NEEL2015 test dataset
    $ python scripts/neel.py test-ner -j /path/to/stanford.jar -y 2015

    Test NER over the NEEL2015 dev dataset
    $ python scripts/neel.py test-ner -j /path/to/stanford.jar -y 2015 -d

    Test NER over the NEEL2016 test dataset
    $ python scripts/neel.py test-ner -j /path/to/stanford.jar -y 2016

    Test NER over the NEEL2016 dev dataset
    $ python scripts/neel.py test-ner -j /path/to/stanford.jar -y 2016 -d

    Test ADEL over the NEEL2014 test dataset
    $ python scripts/neel.py adel -y 2014

    Test ADEL over the NEEL2015 test dataset
    $ python scripts/oke.py adel -y 2015

    Test ADEL over the NEEL2015 dev dataset
    $ python scripts/oke.py adel -y 2015 -d

    Test ADEL over the NEEL2016 test dataset
    $ python scripts/oke.py adel -y 2016

    Test ADEL over the NEEL2016 dev dataset
    $ python scripts/oke.py adel -y 2016 -d
"""

import sys
import os
import subprocess
import codecs
import tailer


def run_command(command, print_output=True):
    if print_output:
        print(command)

    subprocess.check_call(command, shell=True)


def init(args):
    if args.year == 2015 or args.year == 2016:
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools tac2conll -ia " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-gold.tsv") + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training.conll") + " -s neel" + str(args.year) + " -it " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-tweets.tsv"))
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools tac2conll -ia " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev-gold.tsv") + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev.conll") + " -s neel" + str(args.year) + " -it " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev-tweets.tsv"))

        if not args.dev:
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools tac2conll -ia " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-gold.tsv") + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-short-test.conll") + " -s neel" + str(args.year) + " -it " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-short-test-tweets.tsv"))

            with codecs.open(os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-dev.conll"), 'w', 'utf-8') as wfd:
                for f in [os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training.conll"), os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev.conll")]:
                    with codecs.open(f, 'r', 'utf-8') as fd:
                        wfd.write(fd.read())
                        wfd.write("\n")
    elif args.year == 2014:
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools neel20142tac -ia " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-gold.tsv") + " -oa " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/neel" + str(args.year) + ".tac") + " -s neel" + str(args.year) + " -it " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-tweets.tsv"))
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar tools neel20142tac -ia " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-gold.tsv") + " -oa " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/neel" + str(args.year) + ".tac") + " -s neel" + str(args.year) + " -it " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-tweets.tsv"))


def train_ner(args):
    if args.year == 2015 or args.year == 2016:
        if args.dev:
            run_command("java -Xmx12g -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -prop " + os.path.abspath('scripts/stanford.prop') + " -trainFile " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training.conll") + " -serializeTo " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + ".ser.gz"))
        else:
            run_command("java -Xmx12g -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -prop " + os.path.abspath('scripts/stanford.prop') + " -trainFile " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + "-training-dev.conll") + " -serializeTo " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + ".ser.gz"))
    else:
        print ("Training a NER is only available for NEEL2015 and NEEL2016")


def test_ner(args):
    if args.year == 2015 or args.year == 2016:
        if args.dev:
            run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -ner.useSUTime false -loadClassifier " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + ".ser.gz") + " -testFile " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev.conll") + " > " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/stanford.conll"))

            print('\n'.join([str(element) for element in tailer.tail(open(os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/stanford.conll")), 8)]))
        else:
            run_command("java -cp " + args.stanford_jar + " edu.stanford.nlp.ie.crf.CRFClassifier -ner.useSUTime false -loadClassifier " + os.path.abspath("datasets/NEEL" + str(args.year) + "/train/NEEL" + str(args.year) + ".ser.gz") + " -testFile " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-short-test.conll") + " > " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/stanford.conll"))

            print('\n'.join([str(element) for element in tailer.tail(open(os.path.abspath("datasets/NEEL" + str(args.year) + "/test/stanford.conll")), 8)]))
    else:
        print ("Testing a NER is only available for NEEL2015 and NEEL2016")


def adel(args):
    if args.year == 2015 or args.year == 2016:
        if args.dev:
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar nerd -i " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev-tweets.tsv") + " -if tsv -s neel" + str(args.year) + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/results.tac"))
            run_command("python -m neleval evaluate -m all -f tab -g " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/NEEL" + str(args.year) + "-dev-gold.tsv") + " " + os.path.abspath("datasets/NEEL" + str(args.year) + "/dev/results.tac"))
        else:
            run_command("java -jar target/adel-1.0-SNAPSHOT.jar nerd -i " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-short-test-tweets.tsv") + " -if tsv -s neel" + str(args.year) + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/results.tac"))
            run_command("python -m neleval evaluate -m all -f tab -g " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-gold.tsv") + " " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/results.tac"))
    elif args.year == 2014:
        run_command("java -jar target/adel-1.0-SNAPSHOT.jar nerd -i " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-tweets.tsv") + " -if tsv -s neel" + str(args.year) + " -o " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/results.tsv"))
        run_command("python -m neleval evaluate -m all -f tab -g " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/NEEL" + str(args.year) + "-test-gold.tsv") + " " + os.path.abspath("datasets/NEEL" + str(args.year) + "/test/results.tac"))


def check_python():
    """Check if a compatible version of Python is installed"""
    if sys.version_info[:2] > (2, 6) or sys.version_info[:2] > (3, 1):
        return True

    return False


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

        parser = argparse.ArgumentParser(description="Run and train ADEL over the NEEL datasets", prog="neel")

        parser.add_argument('--version', action='version', version='%(prog)s 1.0.0')

        subparsers = parser.add_subparsers(help='sub-commands')

        parser_init = subparsers.add_parser("init", help="NEEL init")

        parser_init.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding NEEL edition")
        parser_init.add_argument("-d", "--dev", action="store_true", help="Init only the Dev dataset")
        parser_init.set_defaults(func=init)

        parser_train_ner = subparsers.add_parser("train-ner", help="Train the NER model over NEEL")

        parser_train_ner.add_argument("-j", "--stanford-jar", required=True, type=file_exists, help="Location of the Stanford CoreNLP jar")
        parser_train_ner.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding OKE edition")
        parser_train_ner.add_argument("-d", "--dev", action="store_true", help="Train over the Dev dataset")
        parser_train_ner.set_defaults(func=train_ner)

        parser_test_ner = subparsers.add_parser("test-ner", help="Test the NER model over NEEL")

        parser_test_ner.add_argument("-j", "--stanford-jar", required=True, type=file_exists, help="Location of the Stanford CoreNLP jar")
        parser_test_ner.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding NEEL edition")
        parser_test_ner.add_argument("-d", "--dev", action="store_true", help="Test over the Dev dataset")
        parser_test_ner.set_defaults(func=test_ner)

        parser_adel = subparsers.add_parser("adel", help="Run ADEL over NEEL")

        parser_adel.add_argument("-y", "--year", required=True, type=int, help="The year of the corresponding NEEL edition")
        parser_adel.add_argument("-d", "--dev", action="store_true", help="Test over the Dev dataset")
        parser_adel.set_defaults(func=adel)

        args = parser.parse_args()

        args.func(args)
    else:
        print("To use this script Python 2.7, Python 3.2 or higher has to be installed.")


if __name__ == '__main__':
    main()
