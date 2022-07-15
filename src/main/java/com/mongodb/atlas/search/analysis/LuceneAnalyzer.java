package com.mongodb.atlas.search.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bn.BengaliAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.charfilter.AtlasSearchMappingCharFilterFactory;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.AtlasSearchStopFilterFactory;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer.Builder;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fa.PersianCharFilterFactory;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.icu.ICUFoldingFilterFactory;
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilterFactory;
import org.apache.lucene.analysis.icu.ICUNormalizer2FilterFactory;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.miscellaneous.LengthFilterFactory;
import org.apache.lucene.analysis.miscellaneous.TrimFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.phonetic.DaitchMokotoffSoundexFilterFactory;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.reverse.ReverseStringFilterFactory;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.uk.UkrainianMorfologikAnalyzer;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class LuceneAnalyzer {
	private final static Logger logger = Logger.getLogger(LuceneAnalyzer.class.getName());
	private static final String ANALYZER_HELP     = 
		"Analyzer must be one of [Standard, Simple, Whitespace, Language, Keyword, Custom]";
	private static final String LANG_CODE_HELP    = 
		"Language code must be one of [ar, bg, bn, br, ca, cjk, ckb, cz, da, de, el, en, es, eu, fa, fi, fr, ga, gl, hi, hu, hy, id, it, ja, ko, lt, lv, nl, no, pt, ro, ru, sv, th, tr, ua]";
	private static final String FILTER_TYPE_HELP  =
		"Filter type must be one of [lowercase, length, icuFolding, icuNormalizer, nGram, edgeGram, shingle, regex, snowballStemming, stopword, trim]";
	private static final String MIN_GRAM_HELP     = 
		"minGram value must be an integer greater than or equal to 1";
	private static final String MAX_GRAM_HELP     = 
		"maxGram value must be greater than or equal to the value of minGram";
	private static final String OPERATOR_HELP     = 
		"Operator must be one of [autocomplete, text]";
	private static final String TOKENIZER_HELP    = 
		"Tokenizer must be one of [edgeGram, nGram]";
	private static final String KEY_AUTOCOMPLETE  = "autocomplete";
	private static final String KEY_ANALYZERS     = "analyzers";
	private static final String KEY_TOKENIZER     = "tokenizer";
	private static final String KEY_CHAR_FILTERS  = "charFilters";
	private static final String KEY_TOKEN_FILTERS = "tokenFilters";
	private static final String KEY_MAPPING       = "mapping";
	private static final String KEY_MAPPINGS      = "mappings";
	private static final String KEY_IGNORED_TAGS  = "ignoredTags";
	private static final String KEY_ESCAPED_TAGS  = "escapedTags";
	private static final String KEY_GROUP         = "group";
	private static final String KEY_PATTERN       = "pattern";
	private static final String KEY_TYPE          = "type";
	private static final String KEY_NAME          = "name";
	private static final String LIST_OPT          = "list";
	private static final String KEY_MIN_GRAM      = "minGram";
	private static final String KEY_MAX_GRAM      = "maxGram";
	private static final String KEY_MIN_GRAM_SIZE = "minGramSize";
	private static final String KEY_MAX_GRAM_SIZE = "maxGramSize";
	
	public Analyzer getLanguageAnalyzer(String langCode) {
    	Analyzer analyzer = null;
    	
		switch (langCode) {
			case "lucene.arabic":
			case "ar":
				analyzer = new ArabicAnalyzer();
				break;
				
			case "lucene.armenian":
			case "hy":
				analyzer = new ArmenianAnalyzer();
				break;
				
			case "lucene.basque":
			case "eu":
				analyzer = new BasqueAnalyzer();
				break;
				
			case "lucene.bulgarian":
			case "bg":
				analyzer = new BulgarianAnalyzer();
				break;
				
			case "lucene.bengali":
			case "bn":
				analyzer = new BengaliAnalyzer();
				break;
				
			case "lucene.brazilian":
			case "br":
				analyzer = new BrazilianAnalyzer();
				break;
				
			case "lucene.catalan":
			case "ca":
				analyzer = new CatalanAnalyzer();
				break;
				
			case "lucene.cjk 1":
			case "cjk":
				analyzer = new CJKAnalyzer();
				break;
				
			case "lucene.smartcn 5":
				analyzer = new SmartChineseAnalyzer();
				break;
				
			case "lucene.czech":
			case "cz":
				analyzer = new CzechAnalyzer();
				break;
				
			case "lucene.danish":
			case "da":
				analyzer = new DanishAnalyzer();
				break;
				
			case "lucene.dutch":
			case "nl":
				analyzer = new DutchAnalyzer();
				break;
				
			case "lucene.german":
			case "de":
				analyzer = new GermanAnalyzer();
				break;
				
			case "lucene.greek":
			case "el":
				analyzer = new GreekAnalyzer();
				break;
				
			case "lucene.english":
			case "en":
				analyzer = new EnglishAnalyzer();
				break;
				
			case "lucene.finnish":
			case "fi":
				analyzer = new FinnishAnalyzer();
				break;
				
			case "lucene.french":
			case "fr":
				analyzer = new FrenchAnalyzer();
				break;
				
			case "lucene.galician":
			case "gl":
				analyzer = new GalicianAnalyzer();
				break;
				
			case "lucene.hindi":
			case "hi":
				analyzer = new HindiAnalyzer();
				break;
				
			case "lucene.hungarian":
			case "hu":
				analyzer = new HungarianAnalyzer();
				break;
								
			case "lucene.irish":
			case "ga":
				analyzer = new IrishAnalyzer();
				break;
				
			case "lucene.indonesian":
			case "id":
				analyzer = new IndonesianAnalyzer();
				break;
				
			case "lucene.italian":
			case "it":
				analyzer = new ItalianAnalyzer();
				break;
				
			case "lucene.japanese":
			case "ja":
				analyzer = new JapaneseAnalyzer();
				break;
				
			case "lucene.korean":
			case "ko":
				analyzer = new KoreanAnalyzer();
				break;
				
			case "lucene.lithuanian":
			case "lt":
				analyzer = new LithuanianAnalyzer();
				break;
				
			case "lucene.latvian":
			case "lv":
				analyzer = new LatvianAnalyzer();
				break;
				
			case "lucene.norwegian":
			case "no":
				analyzer = new NorwegianAnalyzer();
				break;
				
			case "lucene.persian":
			case "fa":
				analyzer = new PersianAnalyzer();
				break;
				
			case "lucene.portuguese":
			case "pt":
				analyzer = new PortugueseAnalyzer();
				break;
				
			case "lucene.romanian":
			case "ro":
				analyzer = new RomanianAnalyzer();
				break;
				
			case "lucene.russian":
			case "ru":
				analyzer = new RussianAnalyzer();
				break;
				
			case "lucene.sorani":
			case "ckb":
				analyzer = new SoraniAnalyzer();
				break;
				
			case "lucene.spanish":
			case "es":
				analyzer = new SpanishAnalyzer();
				break;
				
			case "lucene.swedish":
			case "sv":
				analyzer = new SwedishAnalyzer();
				break;
				
			case "lucene.thai":
			case "th":
				analyzer = new ThaiAnalyzer();
				break;
				
			case "lucene.turkish":
			case "tr":
				analyzer = new TurkishAnalyzer();
				break;
				
			case "lucene.ukrainian":
			case "ua":
				analyzer = new UkrainianMorfologikAnalyzer();
				break;
				
		} // end switch langCode
		return analyzer;
    }

	public static void main(String[] args) {
		LuceneAnalyzer tester = new LuceneAnalyzer();
		
		// options
		Option analyzerOpt = Option.builder("a").longOpt("analyzer").hasArg().desc("Lucene analyzer to use (defaults to 'Standard'). Use 'list' for supported analyzer names.").build();
		Option langOpt = Option.builder("l").longOpt("language").hasArg().desc("Language code (used with '--analyzer=Language' only.  Use 'list' for supported language codes.").build();
		Option operatorOpt = Option.builder("o").longOpt("operator").hasArg().desc("Query operator to use (defaults to 'text'). Use 'list' for supported operator names.").build();
		Option tokenizerOpt = Option.builder("k").longOpt("tokenizer").hasArg().desc("Tokeniser to use with autocomplete operator (defaults to 'edgeGram'). Use 'list' for supported tokenizer names.").build();
		Option minGramOpt = Option.builder("m").longOpt("minGrams").hasArg().desc("Minimum number of characters per indexed sequence to use with autocomplete operator (defaults to '2').").build();
		Option maxGramOpt = Option.builder("x").longOpt("maxGrams").hasArg().desc("Maximum number of characters per indexed sequence to use with autocomplete operator (defaults to '3').").build();
		Option textOpt = Option.builder("t").longOpt("text").hasArg().desc("Input text to analyze").build();
		Option fileOpt = Option.builder("f").longOpt("file").hasArg().desc("Input text file to analyze").build();
		Option helpOpt = Option.builder("h").longOpt("help").desc("Prints this message").build();
		Option defOpt  = Option.builder("d").longOpt("definition").hasArg().desc("Index definition file containing custom analyzer").build();
		Option nameOpt = Option.builder("n").longOpt("name").hasArg().desc("Custom analyzer name").build();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		HelpFormatter formatter = new HelpFormatter();

		Options helpOptions = new Options();
		helpOptions.addOption(helpOpt)
			.addOption(analyzerOpt)
			.addOption(langOpt)
			.addOption(nameOpt)
			.addOption(operatorOpt)
			.addOption(tokenizerOpt)
			.addOption(minGramOpt)
			.addOption(maxGramOpt);
		
		OptionGroup inputGroup = new OptionGroup();
		inputGroup.addOption(fileOpt).addOption(textOpt);
		helpOptions.addOptionGroup(inputGroup);
		
		OptionGroup customGroup = new OptionGroup();
		customGroup.addOption(defOpt);
		helpOptions.addOptionGroup(customGroup);
		
		try {
			// parse help
			cmd = parser.parse(helpOptions, args, true);
			if (null != cmd && cmd.hasOption(helpOpt.getOpt())) {
				formatter.printHelp("mvn exec:java -Dexec.args=\"<options>\"", helpOptions);
				System.exit(0);
			} else {
				// parse analyzer
				cmd = parser.parse(helpOptions, args, true);
				String analyzerType = (null != cmd && cmd.hasOption(analyzerOpt.getOpt()) ? cmd.getOptionValue(analyzerOpt.getOpt()) : "standard").toLowerCase();
				if (analyzerType.equals(LIST_OPT)) {
					System.out.println(ANALYZER_HELP);
					System.exit(0);
				}
				
				// parse language code
				cmd = parser.parse(helpOptions, args, true);
				String langCode = (null != cmd && cmd.hasOption(langOpt.getOpt()) ? cmd.getOptionValue(langOpt.getOpt()) : "").toLowerCase();
				if (langCode.equals(LIST_OPT)) {
					System.out.println(LANG_CODE_HELP);
					System.exit(0);
				}
					
				// parse operator and tokenizer options
				cmd = parser.parse(helpOptions, args, true);
				String operatorName = (null != cmd && cmd.hasOption(operatorOpt.getOpt()) ? cmd.getOptionValue(operatorOpt.getOpt()) : "text").toLowerCase();
				if (operatorName.equals(LIST_OPT)) {
					System.out.println(OPERATOR_HELP);
					System.exit(0);
				}
				String tokenizerName = (null != cmd && cmd.hasOption(tokenizerOpt.getOpt()) ? cmd.getOptionValue(tokenizerOpt.getOpt()) : "nGram");
				if (tokenizerName.equals(LIST_OPT)) {
					System.out.println(TOKENIZER_HELP);
					System.exit(0);
				} else if (tokenizerName.equalsIgnoreCase("edgegram")) {
					tokenizerName = "edgeNGram";
				}
				String minGrams = (null != cmd && cmd.hasOption(minGramOpt.getOpt()) ? cmd.getOptionValue(minGramOpt.getOpt()) : "2");
				if (Integer.parseInt(minGrams) < 1) {
					System.out.println(MIN_GRAM_HELP);
					System.exit(0);
				}
				String maxGrams = (null != cmd && cmd.hasOption(maxGramOpt.getOpt()) ? cmd.getOptionValue(maxGramOpt.getOpt()) : "3");
				if (Integer.parseInt(maxGrams) < Integer.parseInt(minGrams)) {
					System.out.println(MAX_GRAM_HELP);
					System.exit(0);
				}
				
				// parse input text
				inputGroup.setRequired(true);
				helpOptions.addOptionGroup(inputGroup);
				cmd = parser.parse(helpOptions, args, true);
				String text = null != cmd && cmd.hasOption(textOpt.getOpt()) ? cmd.getOptionValue(textOpt.getOpt())
					: null != cmd && cmd.hasOption(fileOpt.getOpt()) ? tester.readFile(cmd.getOptionValue(fileOpt.getOpt())) : null;
				if (null == text) {
					if (inputGroup.getSelected().equals(textOpt.getOpt())) {
						throw new ParseException(MessageFormat.format("Option ''{0}'' cannot be null or empty", textOpt.getOpt()));
					} else if (inputGroup.getSelected().equals(fileOpt.getOpt())) {
						throw new ParseException(MessageFormat.format("File ''{0}'' cannot be null or empty", cmd.getOptionValue(fileOpt.getOpt())));
					}
				}
				
				Analyzer analyzer = null;
				if (null != operatorName && operatorName.equalsIgnoreCase(KEY_AUTOCOMPLETE)) {
					analyzer = tester.buildAutocompleteAnalyzer(tokenizerName, minGrams, maxGrams);
				} else {
					switch (analyzerType) {
						case "simple":
							analyzer = new SimpleAnalyzer();
							break;
			
						case "standard":
							analyzer = new StandardAnalyzer();
							break;
			
						case "whitespace":
							analyzer = new WhitespaceAnalyzer();
							break;
			
						case "keyword":
							analyzer = new KeywordAnalyzer();
							break;
							
						case "language":
							analyzer = tester.getLanguageAnalyzer(langCode);
							break;
							
						case "custom":
							customGroup.setRequired(true);
							helpOptions.addOptionGroup(customGroup);
							cmd = parser.parse(helpOptions, args, true);
							
							String selected = customGroup.getSelected();
							if (null == selected || selected.trim().length() == 0) {
								System.err.println("Index definition file is required (-d)");
								System.exit(1);
							}
		
							JsonObject indexDef = null;
							if (customGroup.getSelected().equals(defOpt.getOpt())) {
								indexDef = null != cmd && 
										         cmd.hasOption(defOpt.getOpt()) && 
										         cmd.hasOption(nameOpt.getOpt()) ? 
										        	tester.readJsonFile(cmd.getOptionValue(defOpt.getOpt())) : null;
								if (null == indexDef) {
									throw new ParseException(MessageFormat.format("File ''{0}'' cannot be null or empty", cmd.getOptionValue(defOpt.getOpt())));
								}
							}
							
							analyzer = tester.buildCustomAnalyzer(indexDef, cmd.getOptionValue(nameOpt.getOpt()));
							break;
							
						default:
							System.err.println(MessageFormat.format(
								"Unknown analyzer ''{0}'' -- {1}",
								analyzerType, ANALYZER_HELP));
					} // end switch
				} // end if operatorName.equalsIgnoreCase(KEY_AUTOCOMPLETE)
				
				if (null == analyzer) {
					throw new Exception("Unable to construct the custom analyzer from the index definition");
				}
				
				tester.displayTokens(analyzer, text,operatorName, tokenizerName, minGrams, maxGrams);
			}
		} catch (Exception ex) {
			System.err.println(ex.getLocalizedMessage());
			System.exit(1);
		}
	}
	
	public Analyzer buildAutocompleteAnalyzer(String tokenizer, String minGrams, String maxGrams) throws IOException {
		Builder builder = CustomAnalyzer.builder().withTokenizer(
			tokenizer, "minGramSize", minGrams, "maxGramSize", maxGrams);
		return builder.build();
	}
	
    @SuppressWarnings("unchecked")
	public Analyzer buildCustomAnalyzer(JsonObject indexDef, String analyzerName) throws Exception {
    	if (null == indexDef || null == analyzerName || analyzerName.trim().length() == 0) {
    		throw new Exception("Unable to construct custom analyzer -- null definition or name");
    	}
    	
    	Analyzer analyzer = null;    	
		ArrayList<String> tokenizerParams = new ArrayList<>();
		
		JsonArray analyzers = indexDef.getArray(KEY_ANALYZERS);
		if (null != analyzers && analyzers.length() > 0) {
			JsonObject analyzerObj = null;
			String analyzerObjName = null;
			for (int i = 0; i < analyzers.length(); i++) {
				analyzerObj = analyzers.getObject(i);
				analyzerObjName = analyzerObj.getString(KEY_NAME);
				logger.info(analyzerObjName);
				if (analyzerObjName.equals(analyzerName)) {
					break;
				}
			}
			
			if (!analyzerObjName.equals(analyzerName)) {
				throw new Exception("Analyzer " + analyzerName + " not found in index definition");
			}
			
			JsonObject tokenizerObj = analyzerObj.getObject(KEY_TOKENIZER);
			//logger.info(JsonUtil.stringify(tokenizerObj, 2));
			String tokenizerType = tokenizerObj.getString(KEY_TYPE);
			//logger.info("Tokenizer type: " + tokenizerType);
			if (tokenizerType.equals("uaxUrlEmail")) {
				tokenizerType = "uax29UrlEmail";	// Atlas Search omits the "29"
			} else if (tokenizerType.equals("edgeGram")) {
				tokenizerType = "edgeNGram";	// Atlas Search omits the "N"
				if (tokenizerObj.hasKey(KEY_MIN_GRAM)) {
					int minGram = Double.valueOf(tokenizerObj.getNumber(KEY_MIN_GRAM)).intValue();
					tokenizerParams.add(KEY_MIN_GRAM_SIZE);
					tokenizerParams.add(Integer.toString(minGram));
				}
				if (tokenizerObj.hasKey(KEY_MAX_GRAM)) {
					int maxGram = Double.valueOf(tokenizerObj.getNumber(KEY_MAX_GRAM)).intValue();
					tokenizerParams.add(KEY_MAX_GRAM_SIZE);
					tokenizerParams.add(Integer.toString(maxGram));
				}
			} else if (tokenizerType.equals("regexCaptureGroup")) {
				tokenizerType = "pattern";
				String pattern = tokenizerObj.getString(KEY_PATTERN);
				//logger.info("Pattern: " + pattern);
				tokenizerParams.add(KEY_PATTERN);
				tokenizerParams.add(pattern);
				int group = Double.valueOf(tokenizerObj.getNumber(KEY_GROUP)).intValue();
				//logger.info("Group: " + group);
				tokenizerParams.add(KEY_GROUP);
				tokenizerParams.add(Integer.toString(group));
			} else if (tokenizerType.equals("regexSplit")) {
				tokenizerType = "pattern";
				if (tokenizerObj.hasKey(KEY_PATTERN)) {
					String pattern = tokenizerObj.getString(KEY_PATTERN);
					tokenizerParams.add(KEY_PATTERN);
					tokenizerParams.add(pattern);
				}
			}
			
			String[] tokenizerStrs = new String[tokenizerParams.size()];
			tokenizerStrs = tokenizerParams.toArray(tokenizerStrs);
			logger.info("Tokenizer strings: " + Arrays.toString(tokenizerStrs));
			Builder builder = CustomAnalyzer.builder()
				.withTokenizer(tokenizerType, tokenizerStrs);

			JsonArray tokenFilters = analyzerObj.getArray(KEY_TOKEN_FILTERS);
			if (null != tokenFilters && tokenFilters.length() > 0) {
				Object[] objs = tokenFiltersToParams(tokenFilters);
				if (objs.length > 0) {
					int j = 0;
					for (int i = 0; i < objs.length; i++) {
						if (objs[i].getClass().getSimpleName().equals("Class")) {
							j = i;
							ArrayList<String> params = new ArrayList<>();
							if (j < objs.length - 1) {
								String clsName = objs[++j].getClass().getSimpleName();
								//System.out.println("Next class: " + clsName);
								while (!clsName.equals("Class") && j < objs.length) {
									//System.out.println("Param: " + objs[j]);
									params.add(objs[j].toString());
									j++;
									if (j < objs.length) {
										clsName = objs[j].getClass().getSimpleName();
									} else {
										break;
									}
								}
							}
							String[] strs = new String[params.size()];
							strs = params.toArray(strs);
							builder = builder.addTokenFilter((Class<? extends TokenFilterFactory>)objs[i], strs);
						}
					}
				} // end if (objs.length > 0)
			}
			
			@SuppressWarnings("rawtypes")
			Class charFilterClass = null;
			String charFilterType = null;
			JsonArray charFilters = analyzerObj.getArray(KEY_CHAR_FILTERS);
			
			if (null != charFilters && charFilters.length() > 0) {
				for (int i = 0; i < charFilters.length(); i++) {
					JsonObject charFilter = charFilters.getObject(i);
					charFilterType = charFilter.getString(KEY_TYPE);
					logger.info("Char filter type: " + charFilterType);
					if (charFilterType.equals("htmlStrip")) {
						charFilterClass = HTMLStripCharFilterFactory.class;
					} else if (charFilterType.equals("mapping")) {
						charFilterClass = AtlasSearchMappingCharFilterFactory.class;
					} else if (charFilterType.equals("icuNormalize")) {
						charFilterClass = ICUNormalizer2CharFilterFactory.class;
					} else if (charFilterType.equals("persian")) {
						charFilterClass = PersianCharFilterFactory.class;
					}

					
					String[] strs = charFiltersToParams(charFilter, charFilterType);
					logger.info("Char filters: " + Arrays.toString(strs));
					if (charFilterType.equals("mapping")) {
						for (int j = 0; j < strs.length; j++) {
							builder = builder.addCharFilter(charFilterClass, strs[j], strs[++j]);
						}
						analyzer = builder.build();
					} else {
						analyzer = builder.addCharFilter(charFilterClass, strs).build();
					}
				}				
			} else {
				analyzer = builder.build();
			}
		}
		
    	return analyzer;
    }
		
	private Object[] tokenFiltersToParams(JsonArray tokenFilters) {
		ArrayList<Object> params = new ArrayList<Object>();
		if (null != tokenFilters && tokenFilters.length() > 0) {
			for (int i = 0; i < tokenFilters.length(); i++) {
				JsonObject filter = tokenFilters.getObject(i);
				String filterType = filter.getString(KEY_TYPE);
				if (null != filterType) {
					JsonObject filterObj = filter;
					switch (filterType) {
						case "daitchMokotoffSoundex":
							// additional parameters: originalTokens
							params.add(DaitchMokotoffSoundexFilterFactory.class);
							if (filterObj.hasKey("originalTokens")) {
								params.add("inject");
								params.add(filterObj.getString("originalTokens").equals("include") ? "true" : "false");
							}
							break;
							
						case "lowercase":
							params.add(LowerCaseFilterFactory.class);
							break;

						case "asciiFolding":
							params.add(ASCIIFoldingFilterFactory.class);
							break;
							
						case "stopword":
							params.add(AtlasSearchStopFilterFactory.class);
							// additional parameters: tokens, ignoreCase
							if (filterObj.hasKey("tokens")) {
								params.add("words");
								JsonArray tokens = filterObj.getArray("tokens");
								params.add(tokens.toJson().replace("\"", "").replace("[", "").replace("]", ""));
							}
							if (filterObj.hasKey("ignoreCase")) {
								params.add("ignoreCase");
								params.add(filterObj.getBoolean("ignoreCase"));
							}
							break;
							
						case "icuFolding":
							params.add(ICUFoldingFilterFactory.class);
							break;
							
						case "icuNormalizer":
							params.add(ICUNormalizer2FilterFactory.class);
							// additional parameters: normalizationForm
							if (filterObj.hasKey("normalizationForm")) {
								params.add("name");
								params.add(filterObj.getString("normalizationForm"));
							}
							break;
							
						case "length":
							params.add(LengthFilterFactory.class);
							// additional parameters: min, max
							if (filterObj.hasKey("min")) {
								params.add("min");
								params.add(Double.valueOf(filterObj.getNumber("min")).intValue());
								if (!filterObj.hasKey("max")) {
									params.add("max");
									params.add(255);									
								}
							}
							if (filterObj.hasKey("max")) {
								params.add("max");
								params.add(Double.valueOf(filterObj.getNumber("max")).intValue());
								if (!filterObj.hasKey("min")) {
									params.add("min");
									params.add(0);									
								}
							}
							
							break;
							
						case "nGram":
							params.add(NGramFilterFactory.class);
							// additional parameters: minGram, maxGram, termNotInBounds
							if (filterObj.hasKey(KEY_MIN_GRAM)) {
								params.add(KEY_MIN_GRAM_SIZE);
								params.add(Double.valueOf(filterObj.getNumber(KEY_MIN_GRAM)).intValue());
							}
							if (filterObj.hasKey(KEY_MAX_GRAM)) {
								params.add(KEY_MAX_GRAM_SIZE);
								params.add(Double.valueOf(filterObj.getNumber(KEY_MAX_GRAM)).intValue());
							}
							if (filterObj.hasKey("termNotInBounds")) {
								params.add("preserveOriginal");
								params.add(filterObj.getString("termNotInBounds").equals("include") ? "true" : "false");
							}
							break;
							
						case "edgeGram":
							params.add(EdgeNGramFilterFactory.class);
							// additional parameters: minGram, maxGram, termNotInBounds
							if (filterObj.hasKey(KEY_MIN_GRAM)) {
								params.add(KEY_MIN_GRAM_SIZE);
								params.add(Double.valueOf(filterObj.getNumber(KEY_MIN_GRAM)).intValue());
							}
							if (filterObj.hasKey(KEY_MAX_GRAM)) {
								params.add(KEY_MAX_GRAM_SIZE);
								params.add(Double.valueOf(filterObj.getNumber(KEY_MAX_GRAM)).intValue());
							}
							if (filterObj.hasKey("termNotInBounds")) {
								params.add("preserveOriginal");
								params.add(filterObj.getString("termNotInBounds").equals("include") ? "true" : "false");
							}
							break;
							
						case "shingle":
							params.add(ShingleFilterFactory.class);
							// additional parameters: minShingleSize, maxShingleSize
							if (filterObj.hasKey("minShingleSize")) {
								params.add("minShingleSize");
								params.add(Double.valueOf(filterObj.getNumber("minShingleSize")).intValue());
							}
							if (filterObj.hasKey("maxShingleSize")) {
								params.add("maxShingleSize");
								params.add(Double.valueOf(filterObj.getNumber("maxShingleSize")).intValue());
							}
							break;
							
						case "regex":
							params.add(PatternReplaceFilterFactory.class);
							// additional parameters: pattern, replacement, matches
							if (filterObj.hasKey(KEY_PATTERN)) {
								params.add(KEY_PATTERN);
								params.add(filterObj.getString(KEY_PATTERN));
							}
							if (filterObj.hasKey("replacement")) {
								params.add("replacement");
								params.add(filterObj.getString("replacement"));
							}
							if (filterObj.hasKey("matches")) {
								params.add("replace");
								params.add(filterObj.getString("matches"));
							}
							break;
							
						case "snowballStemming":
							params.add(SnowballPorterFilterFactory.class);
							// additional parameters: stemmerName
							if (filterObj.hasKey("stemmerName")) {
								String language = filterObj.getString("stemmerName");
								// capitalize
								language = language.substring(0, 1).toUpperCase() + language.substring(1);
								params.add("language");
								params.add(language);
							}

							break;
														
						case "trim":
							params.add(TrimFilterFactory.class);
							break;
							
						case "reverse":
							params.add(ReverseStringFilterFactory.class);
							// TODO: more???
							break;
							
						default:
							System.err.println(MessageFormat.format(
								"Unknown filter type ''{0}'' -- {1}",
								filterType, FILTER_TYPE_HELP));
					} // end switch
				} // end if (null != filterType)
			} // end for tokenFilters
		}
		
		Object[] objs = new Object[params.size()];
		objs = params.toArray(objs);
		return objs;
	}
	
	private String[] charFiltersToParams(JsonObject charFilter, String charFilterType) {
		ArrayList<String> params = new ArrayList<String>();

		charFilterType = charFilter.getString(KEY_TYPE);
		if (charFilterType.equals("htmlStrip")) {
			JsonArray ignoredTags = charFilter.getArray(KEY_IGNORED_TAGS);
			if (null != ignoredTags && ignoredTags.length() > 0) {
				params.add(KEY_ESCAPED_TAGS);
				params.add(ignoredTags.toJson().replace("\"", "").replace("[", "").replace("]", ""));
			}
		} else if (charFilterType.equals("mapping")) {
			JsonObject mappings = charFilter.getObject(KEY_MAPPINGS);
			String[] keys = mappings.keys();
			for (int j = 0; j < keys.length; j++) {
				String key = keys[j];
				String mapping = "\"" + key + "\" => \"" + mappings.getString(key) + "\"";
				params.add(KEY_MAPPING);
				params.add(mapping);
			}
			/*
			mappings.toMap().entrySet().forEach(entry -> {
				String mapping = "\"" + entry.getKey() + "\" => \"" + entry.getValue() + "\"";
				params.add(KEY_MAPPING);
				params.add(mapping);
			});*/
		}
		
		String[] strs = new String[params.size()];
		strs = params.toArray(strs);
		return strs;
	}
	
	public String printTokens(
		Analyzer analyzer,
		String text,
		String operatorName,
		String tokenizer,
		String minGrams,
		String maxGrams) throws IOException {
		StringBuffer buffer = new StringBuffer();
		
		TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		//System.out.println(cattr.getClass().getName());
		stream.reset();
		buffer.append(MessageFormat.format("Using {0}<br>", analyzer.getClass().getName()));
		if (operatorName.equalsIgnoreCase(KEY_AUTOCOMPLETE)) {
			buffer.append(MessageFormat.format(
				"Autocomplete - {0}, minGram({1}), maxGram({2})<br>", tokenizer, minGrams, maxGrams));
		}
		
		while (stream.incrementToken()) {
			buffer.append("[" + cattr.toString() + "] ");
		}
		if (buffer.length() == 0) {
			buffer.append("[]");
		}
		return buffer.toString();
	}
	
	public void displayTokens(
		Analyzer analyzer,
		String text,
		String operatorName,
		String tokenizer,
		String minGrams,
		String maxGrams) throws IOException {
		// TODO: add minGramSize and maxGramSize tokenizer params
		//       to existing analyzer instead of building new
		/*
		if (null != operatorName && operatorName.equalsIgnoreCase(KEY_AUTOCOMPLETE)) {
			Builder builder = CustomAnalyzer.builder().withTokenizer(
				tokenizer, "minGramSize", minGrams, "maxGramSize", maxGrams);
			analyzer = builder.build();
		}*/
		TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		//System.out.println(cattr.getClass().getName());
		stream.reset();
		System.out.println(MessageFormat.format("Using {0}", analyzer.getClass().getName()));
		if (operatorName.equalsIgnoreCase(KEY_AUTOCOMPLETE)) {
			System.out.println(MessageFormat.format(
				"Autocomplete - {0}, minGram({1}), maxGram({2})", tokenizer, minGrams, maxGrams));
		}
		StringBuffer output = new StringBuffer();
		
		while (stream.incrementToken()) {
			output.append("[" + cattr.toString() + "] ");
		}
		if (output.length() == 0) {
			output.append("[]");
		}
		System.out.println(output.toString());
		stream.end();
		stream.close();
	}
	
	private String readFile(String filename) {
		Path fileName = Path.of(filename);
		String content = null;
		try {
			content = Files.readString(fileName);
		} catch (IOException iox) {
			System.err.println(iox.getLocalizedMessage());
		}
		return content;
	}
	
	private JsonObject readJsonFile(String filename) throws Exception {
		 JsonObject jsonObj = null;
		try {
			InputStream jsonStream = Files.newInputStream(Path.of(filename));
    	    String jsonString = new String(jsonStream.readAllBytes(), StandardCharsets.UTF_8);
    	    jsonObj = Json.parse(jsonString);
		} catch (NoSuchFileException nsfx) {
			System.err.println("File not found");
			throw nsfx;
		}
		
		return jsonObj;
	}
}