package com.mongodb.atlas.search.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.charfilter.AtlasSearchMappingCharFilterFactory;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.core.AtlasSearchStopFilterFactory;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.et.EstonianAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.icu.ICUFoldingFilterFactory;
import org.apache.lucene.analysis.icu.ICUNormalizer2FilterFactory;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.LengthFilterFactory;
import org.apache.lucene.analysis.miscellaneous.TrimFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.phonetic.DaitchMokotoffSoundexFilterFactory;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer.Builder;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class LuceneAnalyzer {
	private static final String ANALYZER_HELP     = 
		"Analyzer must be one of [Standard, Simple, Whitespace, Language, Keyword, Custom]";
	private static final String LANG_CODE_HELP    = 
		"Language code must be one of [ar, bg, bn, br, ca, cjk, ckb, cz, da, de, el, en, es, et, fa, fi, fr, ga, gl, hi, hu, hy, id, it, lt, lv, nl, no, ro, ru, sv, th, tr]";
	private static final String FILTER_TYPE_HELP  =
		"Filter type must be one of [lowercase, length, icuFolding, icuNormalizer, nGram, edgeGram, shingle, regex, snowballStemming, stopword, trim]";
	private static final String LIST_OPT          = "list";
	private static final String KEY_ANALYZERS     = "analyzers";
	private static final String KEY_TOKENIZER     = "tokenizer";
	private static final String KEY_CHAR_FILTERS  = "charFilters";
	private static final String KEY_TOKEN_FILTERS = "tokenFilters";
	private static final String KEY_MAPPING       = "mapping";
	private static final String KEY_MAPPINGS      = "mappings";
	private static final String KEY_IGNORED_TAGS  = "ignoredTags";
	private static final String KEY_ESCAPED_TAGS  = "escapedTags";
	private static final String KEY_NAME          = "name";
	private static final String KEY_TYPE          = "type";
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		LuceneAnalyzer tester = new LuceneAnalyzer();

		// options
		Option analyzerOpt = Option.builder("a").longOpt("analyzer").hasArg().desc("Lucene analyzer to use (defaults to 'Standard'). Use 'list' for supported analyzer names.").build();
		Option langOpt = Option.builder("l").longOpt("language").hasArg().desc("Language code (used with '--analyzer=Language' only.  Use 'list' for supported language codes.").build();
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
			.addOption(nameOpt);
		
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
				try {	
					switch (analyzerType) {
						case "simple":
							analyzer = new SimpleAnalyzer();
							tester.displayTokens(analyzer, text);
							break;
			
						case "standard":
							analyzer = new StandardAnalyzer();
							tester.displayTokens(analyzer, text);
							break;
			
						case "whitespace":
							analyzer = new WhitespaceAnalyzer();
							tester.displayTokens(analyzer, text);
							break;
			
						case "keyword":
							analyzer = new KeywordAnalyzer();
							tester.displayTokens(analyzer, text);
							break;
			
						case "language":
							if (cmd.hasOption("l")) {
								switch (langCode) {
									case "ar":
										analyzer = new ArabicAnalyzer();
										break;
										
									case "bg":
										analyzer = new BulgarianAnalyzer();
										break;
										
									case "bn":
										analyzer = new BengaliAnalyzer();
										break;
										
									case "br":
										analyzer = new BrazilianAnalyzer();
										break;
										
									case "ca":
										analyzer = new CatalanAnalyzer();
										break;
										
									case "cjk":
										analyzer = new CJKAnalyzer();
										break;
										
									case "ckb":
										analyzer = new SoraniAnalyzer();
										break;
										
									case "cz":
										analyzer = new CzechAnalyzer();
										break;
										
									case "da":
										analyzer = new DanishAnalyzer();
										break;
										
									case "de":
										analyzer = new GermanAnalyzer();
										break;
										
									case "el":
										analyzer = new GreekAnalyzer();
										break;
										
									case "en":
										analyzer = new EnglishAnalyzer();
										break;
										
									case "es":
										analyzer = new SpanishAnalyzer();
										break;
										
									case "et":
										analyzer = new EstonianAnalyzer();
										break;
										
									case "fa":
										analyzer = new PersianAnalyzer();
										break;
										
									case "fi":
										analyzer = new FinnishAnalyzer();
										break;
										
									case "fr":
										analyzer = new FrenchAnalyzer();
										break;
										
									case "ga":
										analyzer = new IrishAnalyzer();
										break;
										
									case "gl":
										analyzer = new GalicianAnalyzer();
										break;
										
									case "hi":
										analyzer = new HindiAnalyzer();
										break;
										
									case "hu":
										analyzer = new HungarianAnalyzer();
										break;
										
									case "hy":
										analyzer = new ArmenianAnalyzer();
										break;
										
									case "id":
										analyzer = new IndonesianAnalyzer();
										break;
										
									case "it":
										analyzer = new ItalianAnalyzer();
										break;
										
									case "lt":
										analyzer = new LithuanianAnalyzer();
										break;
										
									case "lv":
										analyzer = new LatvianAnalyzer();
										break;
										
									case "nl":
										analyzer = new DutchAnalyzer();
										break;
										
									case "no":
										analyzer = new NorwegianAnalyzer();
										break;
										
									case "ro":
										analyzer = new RomanianAnalyzer();
										break;
										
									case "ru":
										analyzer = new RussianAnalyzer();
										break;
										
									case "sv":
										analyzer = new SwedishAnalyzer();
										break;
										
									case "th":
										analyzer = new ThaiAnalyzer();
										break;
										
									case "tr":
										analyzer = new TurkishAnalyzer();
										break;
										
									default:
										System.err.println(MessageFormat.format(
											"Unknown language code ''{0}'' -- {1}",
											langCode, LANG_CODE_HELP));
										System.exit(1);
								} // end switch langCode

								tester.displayTokens(analyzer, text);						
							} else {
								System.err.println(MessageFormat.format(
									"Analyzer language is required -- {0}", LANG_CODE_HELP));
								System.exit(1);
							}
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
		
							if (customGroup.getSelected().equals(defOpt.getOpt())) {
								JSONObject def = null != cmd && cmd.hasOption(defOpt.getOpt()) && cmd.hasOption(nameOpt.getOpt()) ? tester.readJsonFile(cmd.getOptionValue(defOpt.getOpt()), cmd.getOptionValue(nameOpt.getOpt())) : null;
								if (null == def) {
									throw new ParseException(MessageFormat.format("File ''{0}'' cannot be null or empty", cmd.getOptionValue(defOpt.getOpt())));
								}
								
								//System.out.println(def.toString(2));
								
								String tokenizerType = def.optJSONObject(KEY_TOKENIZER).getString(KEY_TYPE);
								if (tokenizerType.equals("uaxUrlEmail")) {
									tokenizerType = "uax29UrlEmail";	// Atlas Search omits the "29"
								}
								Builder builder = CustomAnalyzer.builder()
									.withTokenizer(tokenizerType);
								JSONArray tokenFilters = def.optJSONArray(KEY_TOKEN_FILTERS);
								if (null != tokenFilters && !tokenFilters.isEmpty()) {
									Object[] objs = tester.tokenFiltersToParams(tokenFilters);
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
									}
								}
								
								@SuppressWarnings("rawtypes")
								Class charFilterClass = null;
								String charFilterType = null;
								JSONArray charFilters = def.optJSONArray(KEY_CHAR_FILTERS);
								
								if (null != charFilters && !charFilters.isEmpty()) {
									charFilterType = ((JSONObject)charFilters.get(0)).getString(KEY_TYPE);
									if (charFilterType.equals("htmlStrip")) {
										charFilterClass = HTMLStripCharFilterFactory.class;
									} else if (charFilterType.equals("mapping")) {
										charFilterClass = AtlasSearchMappingCharFilterFactory.class;
									}
									
									String[] strs = tester.charFiltersToParams(charFilters, charFilterType);
									if (charFilterType.equals("mapping")) {
										for (int i = 0; i < strs.length; i++) {
											builder = builder.addCharFilter(charFilterClass, strs[i], strs[++i]);
										}
										analyzer = builder.build();
									} else {
										analyzer = builder
											.addCharFilter(charFilterClass, strs)
											.build();
									}
								} else {
									analyzer = builder.build();									
								}
								
								if (null != analyzer) {
									tester.displayTokens(analyzer, text);
								} else {
									System.out.println("Analyzer not built yet");
								}
							} else {
								throw new ParseException(MessageFormat.format("Missing required option: ''{0}''", defOpt.getOpt()));								
							}
							break;
							
						case "help":
							System.out.println(ANALYZER_HELP);
							break;
							
						default:
							System.err.println(MessageFormat.format(
								"Unknown analyzer ''{0}'' -- {1}",
								analyzerType, ANALYZER_HELP));
					} // end switch analyzerType

				} catch (Exception e) {
					System.err.println(e.getLocalizedMessage());
					System.exit(1);
				}
			}
		} catch (ParseException pex) {
			System.err.println(pex.getLocalizedMessage());
			System.exit(1);
		}
	}
	
	private Object[] tokenFiltersToParams(JSONArray tokenFilters) {
		ArrayList<Object> params = new ArrayList<Object>();
		if (null != tokenFilters && !tokenFilters.isEmpty()) {
			tokenFilters.forEach(filter -> {
				String filterType = ((JSONObject) filter).optString(KEY_TYPE);
				if (null != filterType) {
					JSONObject filterObj = (JSONObject) filter;
					switch (filterType) {
						case "daitchMokotoffSoundex":
							// additional parameters: originalTokens
							params.add(DaitchMokotoffSoundexFilterFactory.class);
							if (filterObj.has("originalTokens")) {
								params.add("inject");
								params.add(filterObj.getString("originalTokens").equals("include") ? "true" : "false");
							}
							break;
							
						case "lowercase":
							params.add(LowerCaseFilterFactory.class);
							break;
							
						case "stopword":
							params.add(AtlasSearchStopFilterFactory.class);
							// additional parameters: tokens, ignoreCase
							if (filterObj.has("tokens")) {
								params.add("words");
								JSONArray tokens = filterObj.optJSONArray("tokens");
								params.add(tokens.join(",").replace("\"", ""));
							}
							if (filterObj.has("ignoreCase")) {
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
							if (filterObj.has("normalizationForm")) {
								params.add("name");
								params.add(filterObj.getString("normalizationForm"));
							}
							break;
							
						case "length":
							params.add(LengthFilterFactory.class);
							// additional parameters: min, max
							if (filterObj.has("min")) {
								params.add("min");
								params.add(filterObj.getInt("min"));
								if (!filterObj.has("max")) {
									params.add("max");
									params.add(255);									
								}
							}
							if (filterObj.has("max")) {
								params.add("max");
								params.add(filterObj.getInt("max"));
								if (!filterObj.has("min")) {
									params.add("min");
									params.add(0);									
								}
							}
							
							break;
							
						case "nGram":
							params.add(NGramFilterFactory.class);
							// additional parameters: minGram, maxGram, termNotInBounds
							if (filterObj.has("minGram")) {
								params.add("minGramSize");
								params.add(filterObj.getInt("minGram"));
							}
							if (filterObj.has("maxGram")) {
								params.add("maxGramSize");
								params.add(filterObj.getInt("maxGram"));
							}
							if (filterObj.has("termNotInBounds")) {
								params.add("preserveOriginal");
								params.add(filterObj.getString("termNotInBounds").equals("include") ? "true" : "false");
							}
							break;
							
						case "edgeGram":
							params.add(EdgeNGramFilterFactory.class);
							// additional parameters: minGram, maxGram, termNotInBounds
							if (filterObj.has("minGram")) {
								params.add("minGramSize");
								params.add(filterObj.getInt("minGram"));
							}
							if (filterObj.has("maxGram")) {
								params.add("maxGramSize");
								params.add(filterObj.getInt("maxGram"));
							}
							if (filterObj.has("termNotInBounds")) {
								params.add("preserveOriginal");
								params.add(filterObj.getString("termNotInBounds").equals("include") ? "true" : "false");
							}
							break;
							
						case "shingle":
							params.add(ShingleFilterFactory.class);
							// additional parameters: minShingleSize, maxShingleSize
							if (filterObj.has("minShingleSize")) {
								params.add("minShingleSize");
								params.add(filterObj.getInt("minShingleSize"));
							}
							if (filterObj.has("maxShingleSize")) {
								params.add("maxShingleSize");
								params.add(filterObj.getInt("maxShingleSize"));
							}
							break;
							
						case "regex":
							params.add(PatternReplaceFilterFactory.class);
							// additional parameters: pattern, replacement, matches
							if (filterObj.has("pattern")) {
								params.add("pattern");
								params.add(filterObj.getString("pattern"));
							}
							if (filterObj.has("replacement")) {
								params.add("replacement");
								params.add(filterObj.getString("replacement"));
							}
							if (filterObj.has("matches")) {
								params.add("replace");
								params.add(filterObj.getString("matches"));
							}
							break;
							
						case "snowballStemming":
							params.add(SnowballPorterFilterFactory.class);
							// additional parameters: stemmerName
							if (filterObj.has("stemmerName")) {
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
							
						default:
							System.err.println(MessageFormat.format(
								"Unknown filter type ''{0}'' -- {1}",
								filterType, FILTER_TYPE_HELP));
					}
				}
			});
		}
		
		Object[] objs = new Object[params.size()];
		objs = params.toArray(objs);
		return objs;
	}
	
	private String[] charFiltersToParams(JSONArray charFilters, String charFilterType) {
		ArrayList<String> params = new ArrayList<String>();

		if (null != charFilters && !charFilters.isEmpty()) {
			charFilterType = ((JSONObject)charFilters.get(0)).getString(KEY_TYPE);
			if (charFilterType.equals("htmlStrip")) {
				JSONArray ignoredTags = ((JSONObject)charFilters.get(0)).optJSONArray(KEY_IGNORED_TAGS);
				if (!ignoredTags.isEmpty()) {
					params.add(KEY_ESCAPED_TAGS);
					params.add(ignoredTags.join(",").replace("\"", ""));
				}
			} else if (charFilterType.equals("mapping")) {
				charFilters.forEach(filter -> {
					JSONObject mappings = ((JSONObject)filter).optJSONObject(KEY_MAPPINGS);
					mappings.toMap().entrySet().forEach(entry -> {
						String mapping = "\"" + entry.getKey() + "\" => \"" + entry.getValue() + "\"";
						params.add(KEY_MAPPING);
						params.add(mapping);
					});
				});
			}
		}
		
		String[] strs = new String[params.size()];
		strs = params.toArray(strs);
		return strs;
	}
	
	private JSONObject readJsonFile(String filename, String analyzerName) throws Exception{
		JSONTokener tokener = null;
		
		try {
			tokener = new JSONTokener(Files.newInputStream(Path.of(filename)));
		} catch (NoSuchFileException nsfx) {
			System.err.println("File not found");
			throw nsfx;
		}
		JSONObject jsonObject = new JSONObject(tokener);

		if (jsonObject.has(KEY_ANALYZERS)) {
			JSONArray analyzers = (JSONArray) jsonObject.get(KEY_ANALYZERS);
			if (null != analyzers) {
				List<JSONObject> names = (List<JSONObject>) StreamSupport.stream(analyzers.spliterator(), false)
					.map(val -> (JSONObject) val)
					.filter(val -> ((JSONObject) val).get(KEY_NAME).equals(analyzerName))
					.collect(Collectors.toList());
				if (names.size() > 0) {
					return (JSONObject)names.get(0);
				} else {
					throw new ParseException(MessageFormat.format("Index definition file ''{0}'' does not contain a custom analyzer named ''{1}''", filename, analyzerName));
				}
			}
		} else {
			throw new ParseException(MessageFormat.format("Index definition file ''{0}'' is missing an ''{1}'' field", filename, KEY_ANALYZERS));
		}
		
		return null;
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

	private void displayTokens(Analyzer analyzer, String text) throws IOException {
		TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		System.out.println(MessageFormat.format("Using {0}", analyzer.getClass().getName()));
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
}
