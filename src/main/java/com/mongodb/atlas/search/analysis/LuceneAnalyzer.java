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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bn.BengaliAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
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
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class LuceneAnalyzer {

	public static void main(String[] args) {
		LuceneAnalyzer tester = new LuceneAnalyzer();

		// create CLI options
		Options options = new Options();
		options.addOption(new Option("a", "analyzer", true, "Lucene analyzer to use (defaults to 'Standard')"));
		options.addOption(new Option("t", "text", true, "Input text to analyze"));
		options.addOption(new Option("f", "file", true, "Input text file to analyze"));
		options.addOption(new Option("h", "help", false, "Prints this message"));
		options.addOption(new Option("l", "language", true, "Language code (used with '--analyzer Language' only"));

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		HelpFormatter formatter = new HelpFormatter();

		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				// automatically generate the help statement
				formatter.printHelp("mvn exec:java -Dexec.args=<options>", options);
				System.exit(1);
			}
		} catch (ParseException pe) {
			System.err.println(pe.getLocalizedMessage());
			System.exit(1);
		}

		String analyzerType = cmd.hasOption("a") ? cmd.getOptionValue("a") : "standard";
		String text = cmd.hasOption("t") ? cmd.getOptionValue("t")
				: cmd.hasOption("f") ? tester.readFile(cmd.getOptionValue("f")) : null;

		if (null == text) {
			formatter.printHelp("mvn exec:java -Dexec.args=<options>", options);
			System.exit(1);
		}

		Analyzer analyzer = null;
		try {	
			switch (analyzerType.toLowerCase()) {
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
						String langCode = cmd.getOptionValue("l");
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
						} // end switch langCode

						tester.displayTokens(analyzer, text);						
					} else {
						System.err.println(MessageFormat.format(
							"Analyzer language is required -- must be one of [ar, bg, bn, br, ca, cjk, ckb, cz, da, de, el, en, es, et, fa, fi, fr, ga, gl, hi, hu, hy, id, it, lt, lv, nl, no, ro, ru, sv, th, tr]",
							analyzerType));
						System.exit(1);
					}
					break;
					

				default:
					System.err.println(MessageFormat.format(
						"Unknown analyzer ''{0}'' -- must be one of [Standard, Simple, Whitespace, Language, Keyword]",
						analyzerType));
			} // end switch analyzerType

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String readFile(String filename) {
		Path fileName = Path.of(filename);
		String content = null;
		try {
			content = Files.readString(fileName);
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
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
