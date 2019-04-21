# ADEL
ADEL is a robust and efficient entity linking framework that is adaptive to text genres and
language, entity types for the classification task and referent knowledge base for the linking task.
A demo of the version 1 is available [online](http://adel.eurecom.fr/api/).

The version 2 of ADEL is still under development and then do not provide all the features from 
the version 1. The version 2 is a deep recoding of ADEL compared to the version 1. Currently, only 
the recognition is available, thus in case you want all the features, use the version 1 that you can
find in the release tab, or through the v1.0.0 tag.

## Used frameworks
* Spring Boot 2.1.4
* Spring Cloud 2.1.1
* Logbook Spring Boot 1.13.0
* Lombok 1.18.6
* Reflections 0.9.11
* Hibernate Validator 6.1.0
* Twitter Text 3.0.1
* Emoji Java 4.0.2
* Javatuples 1.2
* Asciitable 0.3.2
* Apache Jena 3.10.0
* Junit 5.4.2
* Stanford CoreNLP 3.9.2
* Guava 27.1
* Jsoup 1.11.3
* Spring Shell 2.0.1
* Springfox 2.9.2

## Requirements
* Java 11 and earlier
* Maven 3.5.0 and earlier
* Docker 1.9 and earlier (necessary only if you want to dockerize ADEL)

## Maven Compilation
To compile ADEL, use the following Maven command:
```text
mvn -U clean package
```

The fat JARs will be available in *adel-shell/target* directory for the shell, and 
*adel-api/target* for the API.

## Summary

Please note that supporting services (Config and Discovery Server) must be started before any other
application (adel-api, adel-api-gateway or adel-shell). If everything goes well, you can access the
following services at given location:
* Discovery Server - [http://localhost:8761]()
* Config Server - [http://localhost:8888]()
* API Gateway - [http://localhost:9004]()
* ADEL API - random port, check Eureka Dashboard (Discovery Server)
* Admin Server (Spring Boot Admin) - [http://localhost:9090]()
* Hystrix Dashboard for Circuit Breaker pattern - [http://localhost:7979]() - On the home page is a 
form where you can enter the URL for an event stream to monitor, for example the api-gateway service
running locally: `http://localhost:8080/actuator/hystrix.stream`.

## NER Annotators
Currently only [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) is available as NER 
annotator.

### Add a new annotator
To add a new annotator you have to create a class in the [implementation package](adel-recognition/src/main/java/fr/eurecom/adel/recognition/implementation)
in the [adel-recognition](adel-recognition) module. This class has to be annotated with the 
annotation `@Name` in order to make it available with this name in the classpath. Next, your 
class has to implements the `AnnotatorRepository` [interface](adel-recognition/src/main/java/fr/eurecom/adel/recognition/domain/repositories/AnnotatorRepository.java).
Once your classe has been implemented and named, you can use this name in your profile.

## ADEL profile
A profile for ADEL has to be in YAML or usual properties file.
 
### Definition
A profile looks like this:
```yaml
recognition:
  tweetnormalization:
    usermention: HTTPQuery
    hashtag: Dictionary
    activate: true
  annotators:
    - annotator: StanfordCoreNLP
      name: fullpipeline
      address: classpath:stanford-full-en.properties
      tags: [PERSON,LOCATION]
      from: CoNLL
      tokenizer: true
  mentionoverlapping: Merge
  typeoverlapping:
    method: MajorityVoting
    to: CoNLL0203
    priority: [fullpipeline]
```

This example of profile contains the following properties (they are all mandatory except the 
*tags* property):
* *recognition*: contains the properties for configuring the recognition pipeline
    - *tweetnormalization*: contains the properties for configuring the tweets normalization
        - *usermention*: is the name of the approach used for retrieving the real name of a user 
        mention (@julienplu -> Julien Plu) (available: HTTPQuery)
        - *hashtag*: is the name of the approach used to normalize the hashtags (#imhere -> i m 
        here) (available: Dictionary)
        - *activate*: says if the tweet normalization process is activated or not
    - *annotators*: contains the properties for configuring a list of annotators
        - *annotator*: is the type of annotator (available: StanfordCoreNLP)
        - *name*: is the unique name given to this annotator
        - *address*: is the address of a configuration file (here a usual configuration file for 
        Stanford CoreNLP)
        - *tags*: only property to be optional. Represents the list of entity types to keep. If 
        absent all the entity types will be kept.
        - *from*: the vocabulary that the used model will provide (available: CoNLL, DBpedia, 
        Musicbrainz, NEEL, MUC, DUL)
        - *tokenizer*: says if this annotator has to be used as tokenizer or not (only one allowed)
    - *mentionoverlapping*: is the name of the approach used to resolve the overlaps over the 
    mentions
    (available: Merge)
    - *typeoverlapping*: contains the properties for configuring the type overlapping
        - *method*: is the name of the approach used to resolve the overlaps over the types 
        (available: MajorityVoting)
        - *to*: the vocabulary into which all the entity types will be translated (available: CoNLL,
        DBpedia, Musicbrainz, NEEL, MUC, DUL)
        - *priority*: a list of the annotators name that represents the priority order in which 
        the types are kept. It means that the types given by the first annotator in the list will
        the more kept and the other will be ignored.
    
### Create a New Profile
In order to make your own ADEL profile you need to follow the Spring Boot profile principle as 
detailed in their [documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
You can take as example the existing profiles in the example [profile file](https://github.com/jplu/ADEL-config/blob/master/adel.yaml).
All the configuration files are grouped into a separated [Git repository](https://github.com/jplu/ADEL-config)
and fetched by the config-server module.

## Shell Usage
First of all run the Config Server:
```text
java -jar adel-config-server/target/adel-config-server-2.0.0.jar
```

ADEL comes with its own shell and CLI interface. To start the shell run:
```text
java -jar adel-shell/target/adel-shell-2.0.0.jar --spring.profiles.active=<profile-name>
```

Once connected to the shell you have some commands available.

### Commands for NER
The commands available for NER are: *convert*, *ner*, *ner-eval*, and *ner-test*.

#### Convert
This command allows you to convert a file from *NIF* or *TAC* format into *CoNLL*, *TAC* or *NIF.
```text
adel-shell:>help convert


NAME
        convert - Convert a dataset from a format to another

SYNOPSYS
        convert [--from] string  [--to] string  [--input-annotations] string  

OPTIONS
        --from  string
                
                [Mandatory]
                [Must be one of [NIF, TAC]]

        --to  string
                
                [Mandatory]
                [Must be one of [CoNLL, NIF, TAC]]

        --input-annotations  string
                
                [Mandatory]
                [Must exists]
                [Must be a file]
                [Must be readable]
```

#### Ner
This command allows to you to annotate a text from a text file or given as parameter and get the 
results in *CoNLL*, *NIF* or *TAC* format.
```text
adel-shell:>help ner


NAME
        ner - Recognize entities from an input text or an input text file

SYNOPSYS
        ner [[--text] string]  [[--input-file] string]  [[--format] string]  [[--output-file] string]  [[--output-text] string]  [--force]  [--print]  [--all-annotators]  

OPTIONS
        --text  string
                Text to process
                [Optional, default = ""]

        --input-file  string
                Text file to process
                [Optional, default = ""]
                [Must exists]
                [Must be a file]
                [Must be readable]

        --format  string
                
                [Optional, default = CoNLL]
                [Must be one of [CoNLL, NIF, TAC]]

        --output-file  string
                
                [Optional, default = ""]
                [Must be a file]
                [Must be writable]

        --output-text  string
                Contains the text for the TAC format
                [Optional, default = ""]
                [Must be a file]
                [Must be writable]

        --force Delete the file represented by --output-file
                [Optional, default = false]

        --print Print the result
                [Optional, default = false]

        --all-annotators        Write the output of each annotator in a file
                [Optional, default = false]
```

Example with a CoNLL output:
```text
ner --text "Barack Obama was born in Hawaii. He was elected president in 2008." --output-file ./output.conll --print
```

Or with a NIF output:
```text
ner --text "Barack Obama was born in Hawaii. He was elected president in 2008." --output-file ./output.nif --print --format NIF
```

Or with a TAC output:
```text
ner --text "Barack Obama was born in Hawaii. He was elected president in 2008." --output-file ./output.tac --print --format TAC --output-text ./output.txt
```

#### Ner-score
This command allows you to score your recognition output.
```text
adel-shell:>help ner-score 


NAME
        ner-score - NER evaluation over a dataset

SYNOPSYS
        ner-score [--input-file] string  [[--gold] string]  [[--format] string]  

OPTIONS
        --input-file  string
                
                [Mandatory]
                [Must exists]
                [Must be a file]
                [Must be readable]

        --gold  string
                
                [Optional, default = ]
                [Must exists]
                [Must be a file]
                [Must be readable]

        --format  string
                
                [Optional, default = CoNLL]
                [Must be one of [CoNLL, NIF, TAC]]
```

Before to run the example, apply the following command line in a standard shell:
```text
cut -d' ' -f2 output.conll | paste -d' ' output.conll - | sed 's/^[ \t]*//;s/[ \t]*$//' > new_output.conll
```

And then in the ADEL shell, as example with a CoNLL output:
```text
ner-score --input-file ./new_output.conll
```

Or with a NIF output:
```text
ner-score --input-file ./output.nif --gold ./output.nif --format NIF
```

Or with a TAC output:
```text
ner-score --input-file ./output.tac --gold ./output.tac --format TAC
```

#### Ner-test
This command allows you to test the recognition against a given dataset.
```text
adel-shell:>help ner-test 


NAME
        ner-test - Test the recognition over a dataset

SYNOPSYS
        ner-test [--input-file] string  [[--gold] string]  [--output-file] string  [--force]  [[--format] string]  [--print]  

OPTIONS
        --input-file  string
                
                [Mandatory]
                [Must exists]
                [Must be a file]
                [Must be readable]

        --gold  string
                Gold standard file for a TAC/NIF dataset
                [Optional, default = ]
                [Must exists]
                [Must be a file]
                [Must be readable]

        --output-file  string
                
                [Mandatory]
                [Must be writable]

        --force Delete the file represented by --output-file
                [Optional, default = false]

        --format  string
                
                [Optional, default = CoNLL]
                [Must be one of [CoNLL, CoNLL0203, NIF, TAC]]

        --print 
                [Optional, default = false]
```

Example with a CoNLL dataset:
```text
ner-test --input-file ./output.conll --output-file ./ann.conll --print
```

Or with a NIF dataset:
```text
ner-test --input-file ./output.nif --output-file ./ann.nif --format NIF --print --gold ./output.nif
```

Or with a TAC dataset:
```text
ner-test --input-file ./output.txt --output-file ./ann.tac --format TAC --print --gold ./output.tac
```


## REST API
The second way to use ADEL is through a Web API. In order to get a working API, services has to be
ran in a specific order:
1. The Config Server:

    ```text
    java -jar adel-config-server/target/adel-config-server-2.0.0.jar
    ```

2. The Discovery Server (Eureka Server)

    ```text
    java -jar adel-discovery-server/target/adel-discovery-server-2.0.0.jar
    ```

3. The ADEL API

    ```text
    java -jar adel-api/target/adel-api-2.0.0.jar --spring.profiles.active=<profile-name>
    ```
    Replace `profile-name` by the profile you want to use. You can run as many profile as you want.

4. The API Gateway (ZUUL Server):

    ```text
    java -jar adel-api-gateway/target/adel-api-gateway-2.0.0.jar
    ```

Example:
```text
curl -XPOST -H "Content-Type: application/json" --data '{"text": "Barack Obama was born in Hawaii. He was elected president in 2008."}' http://localhost:9004/adel/api/v2/recognize
```

Or an example to apply ADEL over a NIF content:
```text
curl -XPOST -H "Content-Type: application/x-turtle;charset=utf-8" -d @output.nif http://localhost:9004/adel/api/v2/recognize/nif
```

Optionally two others services can be ran as well:
1. The Admin Server (Spring Boot Admin)

    ```text
    java -jar adel-admin-server/target/adel-admin-server-2.0.0.jar
    ```

2. The Hystrix Dashboard (circuit breaker):

    ```text
    java -jar adel-hystrix-dashboard/target/adel-hystrix-dashboard-2.0.0.jar
    ```

The documentation of the API is automatically available on the
[root path](http://localhost:9004/adel) through Swagger UI.

## Dockerizing ADEL
The Docker images of ADEL can be created with the following command line:
```text
mvn docker:build
```

By default the Docker image for the API will run the *en* profile. To create a Docker image of 
the API with another profile you have to specify the name of the profile in the build command line:
```text
mvn docker:build -Dprofile <profile>
```

Where you have to replace `<profile>` with the name of the wanted profile. Finally, you can run 
ADEL with the following Docker command lines (here with the *en* profile):
```
docker run -d --network host jplu/adel-config-server:2.0.0 adel-config-server
docker run -d --network host jplu/adel-discovery-server:2.0.0 adel-discovery-server
docker run -d --network host jplu/adel-api-en:2.0.0 adel-en
docker run -d --network host jplu/adel-api-gateway:2.0.0 adel-api-gateway
```

## Opening an issue
If you find a bug, have trouble following the documentation or have a question about the project you
can create an issue. There’s nothing to it and whatever issue you’re having, you’re likely not the
only one, so others will find your issue helpful, too. To open an issue:

* Please, check before to see if not someone else has already had the same issue.
* Be clear in detailing how to reproduce the bug.
* Include system details.
* In case it is an error, paste the error output.

## Team
**Owner**: Julien Plu ([@jplu](https://github.com/jplu))

**Maintainers and Collaborators**:

* Julien Plu (main contact) ([@jplu](https://github.com/jplu))
* Giuseppe Rizzo ([@giusepperizzo](https://github.com/giusepperizzo))
* Raphaël Troncy ([@rtroncy](https://github.com/rtroncy))

## Citation
If you use this entity linking framework in your research, please cite this journal paper:
```bibtex
@article{plu2019journal,
  year =        {2019},
  title =       {{ADEL}: {AD}aptable {E}ntity {L}inking : {A} hybrid approach to link entities with linked data for information extraction},
  author =      {{P}lu, {J}ulien and  {R}izzo, {G}iuseppe and  {T}roncy, {R}apha{\"e}l},
  journal =     {{S}emantic {W}eb {J}ournal ({SWJ}), {S}pecial {I}ssue on {L}inked {D}ata for {I}nformation {E}xtraction, 2019},
  url =  	{http://jplu.github.io/publications/Plu_Troncy_Rizzo-SWJ2019}
}
```

Or my thesis:
```bibtex
@phdthesis{plu2018thesis,
  year =        {2018},
  title =       {{K}nowledge extraction in web media : {A}t the frontier of {NLP}, machine learning and semantics},
  author =      {{P}lu, {J}ulien},
  school =      {{T}hesis},
  month =       {12},
  url =  	{http://jplu.github.io/publications/Plu-thesis.pdf}
}
```

## License
All the content of this repository is licensed under the terms of the Apache license Version 2.0.