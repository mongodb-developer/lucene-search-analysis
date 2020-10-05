package com.mongodb.atlas.search.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

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
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
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
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;

public class LuceneAnalyzer {
	private static final String ANALYZER_HELP = 
		"Analyzer must be one of [Standard, Simple, Whitespace, Language, Keyword]";
	private static final String LANG_CODE_HELP = 
		"Language code must be one of [ar, bg, bn, br, ca, cjk, ckb, cz, da, de, el, en, es, et, fa, fi, fr, ga, gl, hi, hu, hy, id, it, lt, lv, nl, no, ro, ru, sv, th, tr]";
	private static final String LIST_OPT = "list";
	
	public static void main(String[] args) {
		LuceneAnalyzer tester = new LuceneAnalyzer();

		// options
		Option analyzerOpt = Option.builder("a").longOpt("analyzer").hasArg().desc("Lucene analyzer to use (defaults to 'Standard'). Use 'list' for supported analyzer names.").build();
		Option langOpt = Option.builder("l").longOpt("language").hasArg().desc("Language code (used with '--analyzer=Language' only.  Use 'list' for supported language codes.").build();
		Option textOpt = Option.builder("t").longOpt("text").hasArg().desc("Input text to analyze").build();
		Option fileOpt = Option.builder("f").longOpt("file").hasArg().desc("Input text file to analyze").build();
		Option helpOpt = Option.builder("h").longOpt("help").desc("Prints this message").build();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		HelpFormatter formatter = new HelpFormatter();

		Options helpOptions = new Options();
		helpOptions.addOption(helpOpt)
			.addOption(analyzerOpt)
			.addOption(langOpt);
		
		OptionGroup inputGroup = new OptionGroup();
		inputGroup.addOption(fileOpt).addOption(textOpt).setRequired(true);
		helpOptions.addOptionGroup(inputGroup);

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
							

						case "help":
							System.out.println(ANALYZER_HELP);
							break;
							
						default:
							System.err.println(MessageFormat.format(
								"Unknown analyzer ''{0}'' -- {1}",
								analyzerType, ANALYZER_HELP));
					} // end switch analyzerType

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (ParseException pex) {
			System.err.println(pex.getLocalizedMessage());
			System.exit(1);
		}
		
		
/**		
		
		
*/
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
		while (stream.incrementToken()) {
			System.out.print("[" + cattr.toString() + "] ");
		}
		System.out.println();
		stream.end();
		stream.close();
	}
}
