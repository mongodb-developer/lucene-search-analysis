# Atlas/Lucene Search Analysis

## Introduction

Atlas Search uses [Lucene Analyzers](https://docs.atlas.mongodb.com/reference/atlas-search/analyzers/) to control how the index sets search terms, e.g., where to break up word groupings and whether to consider punctuation. However, without an intimate knowledge of the various Lucene Analyzers, it can be difficult to select the appropriate analyzer for a given field when creating a search index. Inspired by the [Analysis Screen](https://lucene.apache.org/solr/guide/8_6/analysis-screen.html) in Apache Solr, this CLI utility provide a simple way to test how various analyzers will process a given text.

**Note:** see this [Atlas feature request](https://feedback.mongodb.com/forums/924868-atlas-search/suggestions/41501065-analzye-endpoint-or-analysis-screen)

## Build

This tool is implemented as a Java Maven project. To build it, execute the command `mvn clean package` from the directory containing `pom.xml`.

## Run

Use the command `mvn exec:java -Dexec.args="<options>"` with the appropriate options.

```
usage: mvn exec:java -Dexec.args="<options>"
 -a,--analyzer <arg>   Lucene analyzer to use (defaults to 'Standard')
 -f,--file <arg>       Input text file to analyze
 -h,--help             Prints this message
 -l,--language <arg>   Language code (used with '--analyzer Language' only
 -t,--text <arg>       Input text to analyze
```

You can also use `java -cp lib/ -jar <path-to>/atlas-search-analysis-0.0.1.jar <options>` (Java 11 or later)

## Examples

### Analyze text using the `lucene.simple` analyzer

```bash
mvn -q exec:java -Dexec.args="-a simple -t 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.core.SimpleAnalyzer
[hello] [my] [name] [is] [roy] [kiesler]
```

### Analyze text using the `lucene.standard` analyzer

```bash
mvn -q exec:java -Dexec.args="--analyzer standard --text 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.standard.StandardAnalyzer
[hello] [my] [name.is] [roy] [kiesler]
```

### Analyze text using the `lucene.whitespace` analyzer

```bash
mvn -q exec:java -Dexec.args="--analyzer whitespace -t 'hello my-name.is Roy/Kiesler'"
Using org.apache.lucene.analysis.core.WhitespaceAnalyzer
[hello] [my-name.is] [Roy/Kiesler]
```

### Analyze text using the `lucene.language` English analyzer

```bash
mvn -q exec:java -Dexec.args="--analyzer language --language en --text 'running a race'"
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
