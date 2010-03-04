/*
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2010-2010 Edgar Espina
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
 */
package net.hydromatic.clapham.chart.exporter;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.hydromatic.clapham.chart.Chart;
import net.hydromatic.clapham.chart.ChartFactory;
import net.hydromatic.clapham.chart.ChartOptions;
import net.hydromatic.clapham.chart.Chart.NodeVisitor;
import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.GrammarFactory;
import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Symbol;
import net.hydromatic.clapham.parser.EbnfDecorator;
import net.hydromatic.clapham.parser.Language;
import net.hydromatic.clapham.parser.ProductionNode;

/**
 * 
 * An HTML railroad exporter
 * 
 * @author Edgar Espina
 * 
 */
public class ChartHtmlExporter {

	private ChartFactory chartFactory;

	private File outputDirectory;

	private boolean optimize;

	private boolean ebnfNotation;

	private ChartDocumentationProvider documentationProvider;

	private Map<String, String> templateCache;

	public ChartHtmlExporter(ChartFactory chartFactory) {
		if (chartFactory == null) {
			throw new IllegalArgumentException("chartFactory can't be null");
		}
		this.chartFactory = chartFactory;
		// the cache template
		templateCache = new HashMap<String, String>();
		// set the output directory to the clapham home location
		withOutputDirectory(new File(""));
		// optimize Graph by default
		withOptimize(true);
		// add the ebnf notation by default
		withEbnfNotation(true);
	}

	public void export(ChartExporterMonitor monitor, File input,
			Language language) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("input can't be null");
		}
		// remove the file extension
		String grammarName = input.getName();
		grammarName = grammarName.substring(0, grammarName.lastIndexOf('.'));

		export(monitor, grammarName, new FileReader(input), language);
	}

	public void export(ChartExporterMonitor monitor, String grammarName,
			Reader input, Language language) throws IOException {

		if (monitor == null) {
			throw new IllegalArgumentException("monitor can't be null");
		}
		if (grammarName == null) {
			throw new IllegalArgumentException("grammarName can't be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input can't be null");
		}
		if (language == null) {
			throw new IllegalArgumentException("language can't be null");
		}

		Grammar grammar = GrammarFactory.build(language, input, optimize);

		int totalOfWork = grammar.symbolMap.size() + 1;
		monitor.beginTask("Generating " + grammarName + " railroad diagrams",
				totalOfWork);

		outputDirectory = new File(outputDirectory, grammarName);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
			monitor.subTask("Creating directory: " + outputDirectory);
		} else {
			monitor.subTask("Cleaning directory: " + outputDirectory);
			clean(outputDirectory);
		}
		Set<String> keySet = grammar.symbolMap.keySet();
		StringBuilder content = new StringBuilder();
		String NL = "\n";
		for (String symbolName : keySet) {
			content.append(
					exportRule(monitor, grammar, symbolName, outputDirectory))
					.append(NL);
			monitor.worked(1);
		}
		File output = new File(outputDirectory, grammarName + ".html");
		monitor.subTask("Creating page: " + output);
		String doc = "";
		if (documentationProvider != null) {
			doc = documentationProvider.grammar(grammarName);
			if (doc == null) {
				doc = "";
			}
		}
		Map<String, String> args = toMap("title", grammarName, "gname",
				grammarName, "rules", content.toString(), "gdoc", doc, "toc",
				toc(grammar));
		write(output, merge("grammar.html", args));
		monitor.worked(1);
		monitor.done();
	}

	private void clean(File root) {
		File[] files = root.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					clean(file);
				}
				file.delete();
			}
		}
	}

	private String exportRule(ChartExporterMonitor monitor, Grammar grammar,
			String symbolName, File outputDirectory) throws IOException {
		// rule.png
		String filename = new StringBuilder().append(symbolName).append(".png")
				.toString();
		// images/rule.png
		String imageFolderName = "images";
		String relativePath = imageFolderName + "/" + filename;
		monitor.subTask("Rule \"" + symbolName + "\" to: " + relativePath);
		File imageFolder = new File(outputDirectory, imageFolderName);
		if (!imageFolder.exists()) {
			imageFolder.mkdirs();
		}

		// absoultePath/images/rule.png
		File outputFile = new File(imageFolder, filename);

		Chart chart = chartFactory.createChart(grammar);
		// create chart options
		ChartOptions options = chart.createOptions();
		options.withOptimize(optimize)
		/**no top margin*/
		.withInitialLocation(20, 0);
		
		chart.setOptions(options);
		chart.prepare();
		
		chart.drawAndExport(symbolName, outputFile);
		
		Dimension size = chart.size(symbolName);

		// add documentation
		String documentation = "";
		if (documentationProvider != null) {
			String doc = documentationProvider.rule(symbolName);
			if (doc != null) {
				documentation = merge("doc.html", toMap("doc", doc));
			}
		}

		String ebnf = "";
		if (ebnfNotation) {
			ProductionNode production = grammar.symbolMap.get(symbolName)
					.getProduction();
			ebnf = merge("ebnf.html", toMap("ebnf", production
					.toEbnf(EbnfDecorator.HTML)));
		}
		Map<String, String> args = toMap("rule", symbolName, "map", map(
				grammar, symbolName), "output", relativePath, "width", Integer
				.toString(size.width), "height", Integer.toString(size
				.height), "doc", documentation, "ebnf", ebnf);
		String out = merge("rule.html", args);
		return out;
	}

	private String toc(Grammar grammar) throws IOException {
		Set<String> keySet = grammar.symbolMap.keySet();
		StringBuilder builder = new StringBuilder();
		for (String ruleName : keySet) {
			builder.append(merge("toc-entry.html", toMap("rule", ruleName)))
					.append("\n");
		}
		return builder.toString();
	}

	private String map(final Grammar grammar, String symbolName)
			throws IOException {
		StringBuilder map = new StringBuilder();

		final Collection<Node> nonterminals = new HashSet<Node>();
		Symbol symbol = grammar.symbolMap.get(symbolName);

		// the nonterminals collector
		NodeVisitor nonterminalVisitor = new NodeVisitor() {

			public void visit(Node node) {
				if (node.typ == NodeType.NONTERM) {
					if (grammar.symbolMap.get(node.sym.name) != null) {
						nonterminals.add(node);
					}
				}
				node.visitChildren(this);
			}
		};
		// traverse the graph
		symbol.graph.l.accept(nonterminalVisitor);

		for (Node node : nonterminals) {
			int x = node.posBegin.x;
			int y = node.posBegin.y;
			int w = x + node.size.getWidth();
			int h = y + node.size.getHeight() - 5;
			Map<String, String> args = toMap("coords", x + "," + y + "," + w
					+ "," + h, "alt", node.sym.name, "href", node.sym.name);
			map.append(merge("map.html", args)).append("\n");
		}
		return map.toString();
	}

	private Map<String, String> toMap(String... args) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < args.length - 1; i += 2) {
			map.put(args[i], args[i + 1]);
		}
		return map;
	}

	/**
	 * Replace all the ${map.key} in the given template
	 * 
	 * @param templateName
	 * @param args
	 * @return
	 * @throws IOException
	 */
	private String merge(String templateName, Map<String, String> args)
			throws IOException {
		String template = template(templateName);
		Set<Entry<String, String>> entrySet = args.entrySet();
		for (Entry<String, String> entry : entrySet) {
			template = template.replace("${" + entry.getKey() + "}", entry
					.getValue());
		}
		return template;
	}

	/**
	 * Get from the classpath the given template
	 * 
	 * @param templateName
	 * @return
	 * @throws IOException
	 */
	private String template(String templateName) throws IOException {
		String template = templateCache.get(templateName);
		if (template == null) {
			InputStream in = getClass().getResourceAsStream(templateName);
			StringBuilder builder = new StringBuilder();
			BufferedInputStream bis = new BufferedInputStream(in);
			int b = bis.read();
			while (b != -1) {
				builder.append((char) b);
				b = bis.read();
			}
			bis.close();
			template = builder.toString();
		}
		return template;
	}

	private void write(File file, String content) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file));
		out.write(content.getBytes());
		out.flush();
		out.close();
	}

	public ChartHtmlExporter withOutputDirectory(File outputDirectory) {
		if (outputDirectory == null) {
			throw new IllegalArgumentException("outputDirectory");
		}
		this.outputDirectory = outputDirectory;
		return this;
	}

	public ChartHtmlExporter withOptimize(boolean optimize) {
		this.optimize = optimize;
		return this;
	}

	public ChartHtmlExporter withEbnfNotation(boolean ebnfNotation) {
		this.ebnfNotation = ebnfNotation;
		return this;
	}

	public ChartHtmlExporter withDocumentationProvider(
			ChartDocumentationProvider documentationProvider) {
		this.documentationProvider = documentationProvider;
		return this;
	}
}
