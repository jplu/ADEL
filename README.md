# ADEL
(Source code will be available soon).

# Introduction
ADEL is a robust and efficient entity linking framework that is adaptive to text genres and language, entity types for the classification task and referent knowledge base for the linking task. A demo is available [online](http://adel.eurecom.fr/api/).

# Libraries

* [Dropwizard 1.0.6](http://www.dropwizard.io)
* [Jena 3.2.0](https://jena.apache.org/)
* [Elasticsearch REST Client 5.2.1](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
* [Lucene 6.4.1](http://lucene.apache.org/core/)
* [JsonPath 2.2.0](https://github.com/jayway/JsonPath)
* [simmetrics 4.1.1](https://github.com/mpkorstanje/simmetrics)
* [jsoup 1.10.2](https://jsoup.org/)
* [subtitleConverter 1.0.3](https://github.com/JDaren/subtitleConverter)
* [juniversalchardet 2.0.0](https://github.com/albfernandez/juniversalchardet)

# Requirements

* Java 1.8
* Maven 3.0.5 minimum
* Elasticsearch 5.x minimum
* At least one NLP system with an ADEL Web API compliant (such as [Stanford CoreNLP](https://github.com/jplu/stanfordNLPRESTAPI))

# Maven Compilation

To compile ADEL, use the following Maven command:

```
mvn -U clean package
```

The fat JAR will be available in the *target* directory.

# Usage

```
usage: java -jar adel.jar
       [-h] [-v] {server,check,extract,link,nerd} ...

positional arguments:
  {server,check,extract,link,nerd}
                         available commands

optional arguments:
  -h, --help             show this help message and exit
  -v, --version          show the application version and exit
```

There is two ways to use this wrapper: via a Web API or via CLI.

## CLI

The first way is via CLI with six possible sub-commands, **extract**, **link** and **nerd**.

### Extract

To use the **extract** CLI:

```
sage: java -jar adel.jar
       extract [-of {nif,brat,conll,naf}] [-if {raw,srt,ttml}] [-s SETTING] [-l LANG] [-o OFILE] [-n {true,false}] [-h] (-t TEXT | -i IFILE | -u URL) [file]

Only extract and type entities

positional arguments:
  file                   application configuration file

optional arguments:
  -of {nif,brat,conll,naf}, --output-format {nif,brat,conll,naf}
                         the output format for the annotations (default: nif)
  -if {raw,srt,ttml}, --input-format {raw,srt,ttml}
                         the input format for the text (default: raw)
  -s SETTING, --setting SETTING
                         Select the setting (default: default)
  -l LANG, --language LANG
                         Select the language (default: en)
  -o OFILE, --output-file OFILE
                         Output file name which will contain the annotations
  -n {true,false}, --nif {true,false}
                         Says if the input text/file/URL is in NIF (default: false)
  -h, --help             show this help message and exit

inputs:
  -t TEXT, --text TEXT   text to analyse
  -i IFILE, --input-file IFILE
                         Input file name which contain the text to process
  -u URL, --url URL      URL to process
```

### Link

To use the **link** CLI:

```
usage: java -jar adel.jar
       link [-f {nif,brat,tac,naf}] [-s SETTING] [-l LANG] [-o OFILE] [-n {true,false}] [-h] (-t TEXT | -i IFILE | -u URL) [file]

Only linking entities

positional arguments:
  file                   application configuration file

optional arguments:
  -f {nif,brat,tac,naf}, --format {nif,brat,tac,naf}
                         the output format for the annotations (default: tac)
  -s SETTING, --setting SETTING
                         Select the setting (default: default)
  -l LANG, --language LANG
                         Select the language (default: en)
  -o OFILE, --output-file OFILE
                         Output file name which will contain the annotations
  -n {true,false}, --nif {true,false}
                         Says if the input text/file/URL is in NIF (default: false)
  -h, --help             show this help message and exit

inputs:
  -t TEXT, --text TEXT   text to analyse
  -i IFILE, --input-file IFILE
                         Input file name which contain the text to process
  -u URL, --url URL      URL to process
```

### Nerd

To use the **nerd** CLI:

```
usage: java -jar adel.jar
       nerd [-of {nif,brat,tac,naf}] [-if {raw,srt,ttml}] [-s SETTING] [-l LANG] [-o OFILE] [-n {true,false}] [-h] (-t TEXT | -i IFILE | -u URL) [file]

Extract, type and link entities

positional arguments:
  file                   application configuration file

optional arguments:
  -of {nif,brat,tac,naf}, --output-format {nif,brat,tac,naf}
                         the output format for the annotations (default: tac)
  -if {raw,srt,ttml}, --input-format {raw,srt,ttml}
                         the input format for the text (default: raw)
  -s SETTING, --setting SETTING
                         Select the setting (default: default)
  -l LANG, --language LANG
                         Select the language (default: en)
  -o OFILE, --output-file OFILE
                         Output file name which will contain the annotations
  -n {true,false}, --nif {true,false}
                         Says if the input text/file/URL is in NIF (default: false)
  -h, --help             show this help message and exit

inputs:
  -t TEXT, --text TEXT   text to analyse
  -i IFILE, --input-file IFILE
                         Input file name which contain the text to process
  -u URL, --url URL      URL to process
```

## Web API

The second way is via a Web API:

```
usage: java -jar adel.jar
       server [-h] [file]

Runs the Dropwizard application as an HTTP server

positional arguments:
  file                   application configuration file

optional arguments:
  -h, --help             show this help message and exit
```

The format in the HTTP header is respectively **text/turtle;charset=utf-8** for RDF Turtle and 
**application/json;charset=utf-8** in case of errors. The
documentation for the API is available on the [wiki](https://github.com/jplu/ADEL/wiki/API-documentation).

## Configuration

The CLI commands and the Web service use the same Dropwizard configuration file.

## Create a New Profile

In order to create your own ADEL profile you need to put your profile file in YAML into
the folder *profiles* and you must respect the following naming **language_name**. Where **language** must be the
language for which ADEL will be set, and **name** is the name you want to give to this profile.

# Opening an issue

If you find a bug, have trouble following the documentation or have a question about the project you
can create an issue. There’s nothing to it and whatever issue you’re having, you’re likely not the
only one, so others will find your issue helpful, too. To open an issue:

* Please, check before to see if not someone else has already had the same issue.
* Be clear in detailing how to reproduce the bug.
* Include system details.
* In case it is an error, paste the error output.

# Team

**Owner**: Julien Plu ([@jplu](https://github.com/jplu))

**Maintainers and Collaborators**:

* Julien Plu (main contact) ([@jplu](https://github.com/jplu))
* Giuseppe Rizzo ([@giusepperizzo](https://github.com/giusepperizzo))
* Raphaël Troncy ([@rtroncy](https://github.com/rtroncy))

# License

All the content of this repository is licensed under the terms of the Apache license Version 2.0.
