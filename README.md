# Notice: Repository Deprecation
This repository is deprecated and no longer actively maintained. It contains outdated code examples or practices that do not align with current MongoDB best practices. While the repository remains accessible for reference purposes, we strongly discourage its use in production environments.
Users should be aware that this repository will not receive any further updates, bug fixes, or security patches. This code may expose you to security vulnerabilities, compatibility issues with current MongoDB versions, and potential performance problems. Any implementation based on this repository is at the user's own risk.
For up-to-date resources, please refer to the [MongoDB Developer Center](https://mongodb.com/developer).


# Atlas/Lucene Search Analysis

## Introduction

Atlas Search uses [Lucene Analyzers](https://docs.atlas.mongodb.com/reference/atlas-search/analyzers/) to control how the index sets search terms, e.g., where to break up word groupings and whether to consider punctuation. However, without an intimate knowledge of the various Lucene Analyzers, it can be difficult to select the appropriate analyzer for a given field when creating a search index. Inspired by the [Analysis Screen](https://lucene.apache.org/solr/guide/8_6/analysis-screen.html) in Apache Solr, this utility provides two simple ways -- a CLI and a UI -- to test how various analyzers will process a given text.

**Note:** see this [Atlas feature request](https://feedback.mongodb.com/forums/924868-atlas-search/suggestions/41501065-analzye-endpoint-or-analysis-screen)

## Build

The UI for this tool is implemented as a [Vaadin](https://vaadin.com/) web application. The CLI is implemented as a simple POJO. To build these tools, execute the command `mvn clean package` from the directory containing `pom.xml`.

## Run

Use the command `mvnw -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false"` from the directory containing `pom.xml` to launch the Web UI on port 8080.

![Web UI](src/main/resources/vaadin_app.png)

Use the command `mvn -f pom-cli.xml exec:java -Dexec.args="<options>"` with the appropriate options to run the CLI. Use the `-h` or `--help` options to see usage instructions.

```bash
usage: mvn exec:java -Dexec.args="<options>"
 -h, --help               Prints this message
 -a, --analyzer <arg>     Lucene analyzer to use (defaults to 'Standard').
                         Use 'list' for supported analyzer names.
 -d, --definition <arg>   Index definition file containing custom analyzer
 -t, --text <arg>         Input text to analyze
 -f, --file <arg>         Input text file to analyze
 -l, --language <arg>     Language code (used with '--analyzer=Language'
                         only.  Use 'list' for supported language codes.
 -n, --name <arg>         Custom analyzer name
 -o, --operator <arg>     Query operator to use (defaults to 'text'). Use
                         'list' for supported operator names.
 -k, --tokenizer <arg>    Tokeniser to use with autocomplete operator
                         (defaults to 'edgeGram'). Use 'list' for
                         supported tokenizer names.
 -m, --minGrams <arg>     Minimum number of characters per indexed sequence
                         to use with autocomplete operator (defaults to
                         '2').
 -x, --maxGrams <arg>     Maximum number of characters per indexed sequence
                         to use with autocomplete operator (defaults to
                         '3').
```

You can also use `java -cp lib/ -jar <path-to>/atlas-search-analysis-0.0.1.jar <options>` (Java 11 or later) to run the CLI.

## Examples

### Analyze text using the `lucene.simple` analyzer

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="-a simple -t 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.core.SimpleAnalyzer
[hello] [my] [name] [is] [roy] [kiesler]
```

### Analyze text using the `lucene.standard` analyzer

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="--analyzer standard --text 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.standard.StandardAnalyzer
[hello] [my] [name.is] [roy] [kiesler]
```

### Analyze text using the `lucene.whitespace` analyzer

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="--analyzer whitespace -t 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.core.WhitespaceAnalyzer
[hello] [my-name.is] [Roy/Kiesler]
```

### Analyze text using the `lucene.language` English analyzer

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="--analyzer language --language en --text 'running a race'"
Using org.apache.lucene.analysis.en.EnglishAnalyzer
[run] [race]
```

### Analyze a text file using the `lucene.language` French analyzer

```bash
cat <<EOF >> french.txt
bonjour je m'appelle Roy Kiesler
EOF

mvn -q exec:java -Dexec.args="-a language -l fr -f french.txt"
Using org.apache.lucene.analysis.fr.FrenchAnalyzer
[bonjou] [apel] [roy] [kiesl]
```

### Analyze text using a custom analyzer

**Sample 1**

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="-a custom -t 'ROCKY II is better than Rocky V' -d index_roman.json -n romanAnalyzer"
Using org.apache.lucene.analysis.custom.CustomAnalyzer
[rocki] [2] [better] [rocki] [5]
```

**Sample 2**

```bash
mvn -f pom-cli.xml -q exec:java -Dexec.args="-a custom -t '<div><p>This is an <a href="foo.com">HTML</a> test</p></div>' -d index_html.json -n htmlStrippingAnalyzer"
Using org.apache.lucene.analysis.custom.CustomAnalyzer
[p] [This] [is] [an] [a] [href] [foo.com] [HTML] [a] [test] [p]
```

### Analyze text using autocomplete

**Sample 1**

```bash
mvn -f pom-cli.xml exec:java -Dexec.args="-t 'Ribeira Charming Duplex' -o autocomplete"
Using org.apache.lucene.analysis.custom.CustomAnalyzer
Autocomplete - nGram, minGram(2), maxGram(3)
[Ri] [Rib] [ib] [ibe] [be] [bei] [ei] [eir] [ir] [ira] [ra] [ra ] [a ] [a C] [ C] [ Ch] [Ch] [Cha] [ha] [har] [ar] [arm] [rm] [rmi] [mi] [min] [in] [ing] [ng] [ng ] [g ] [g D] [ D] [ Du] [Du] [Dup] [up] [upl] [pl] [ple] [le] [lex] [ex]
```

**Sample 2**

```bash
mvn -f pom-cli.xml exec:java -Dexec.args="-t 'Ribeira Charming Duplex' -o autocomplete -k edgeGram -m 2 -x 15"
Using org.apache.lucene.analysis.custom.CustomAnalyzer
Autocomplete - edgeNGram, minGram(2), maxGram(15)
[Ri] [Rib] [Ribe] [Ribei] [Ribeir] [Ribeira] [Ribeira ] [Ribeira C] [Ribeira Ch] [Ribeira Cha] [Ribeira Char] [Ribeira Charm] [Ribeira Charmi] [Ribeira Charmin]
```
