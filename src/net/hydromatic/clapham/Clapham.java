/*
// $Id$
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
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
package net.hydromatic.clapham;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.hydromatic.clapham.chart.Chart;
import net.hydromatic.clapham.chart.ChartFactory;
import net.hydromatic.clapham.chart.exporter.ChartExporterMonitor;
import net.hydromatic.clapham.chart.exporter.ChartHtmlExporter;
import net.hydromatic.clapham.chart.java2d.Batik2DChart;
import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.parser.Language;
import net.hydromatic.clapham.parser.LanguageParserException;
import net.hydromatic.clapham.parser.ProductionNode;
import net.hydromatic.clapham.parser.bnf.BnfParser;
import net.hydromatic.clapham.parser.bnf.ParseException;
import net.hydromatic.clapham.parser.wirth.WirthParser;

/**
 * Command line utility Clapham, the railroad diagram generator.
 * 
 * @author jhyde
 * @version $Id$
 * @since Sep 11, 2008
 */
public class Clapham {
	private File outputDir;

	public Clapham() {
	}

	public void setOutputDir(File file) {
		this.outputDir = file;
	}

	/**
	 * Deduces the dialect of the grammar from the suffix of the file name.
	 * 
	 * @param file
	 *            Grammar file
	 * @return Dialect of grammar file
	 */
	private Dialect deduceDialect(File file) {
		if (file.getName().toLowerCase().endsWith(".bnf")) {
			return Dialect.BNF;
		} else {
			return Dialect.WIRTH;
		}
	}

	/**
	 * Populates the grammar from the grammar file.
	 * 
	 * @param inputFile
	 *            Grammar file
	 * @param inputDialect
	 *            Dialect of grammar
	 */
	public void load(File inputFile) {
		try {
			Dialect dialect = deduceDialect(inputFile);

			final ChartFactory chartFactory = new ChartFactory() {
				public Chart createChart(Grammar grammar) {
					return new Batik2DChart(grammar);
				}
			};

			ChartHtmlExporter exporter = new ChartHtmlExporter(chartFactory);

			exporter.withOptimize(true)
					.withEbnfNotation(true)
					.withOutputDirectory(outputDir);

			exporter.export(ChartExporterMonitor.SYS_OUT, inputFile, dialect);

		} catch (LanguageParserException ex) {
			throw new RuntimeException("Error while loading file '"
					+ inputFile.getPath() + "'.", ex);
		} catch (IOException e) {
			throw new RuntimeException("Error while loading file '" + inputFile
					+ "'.", e);
		}
	}

	/**
	 * Main command-line entry point.
	 * 
	 * @param args
	 *            Command-line arguments
	 */
	public static void main(String[] args) {
		new Clapham().run(args);
	}

	/**
	 * Parses command-line arguments an executes.
	 * 
	 * @param args
	 *            Command-line arguments
	 */
	private void run(String[] args) {
		final Iterator<String> argIter = Arrays.asList(args).iterator();
		try {
			String fileName = null;
			String outputDirName = null;
			while (argIter.hasNext()) {
				final String arg = argIter.next();
				if (arg.startsWith("-")) {
					if (arg.equals("-d")) {
						if (!argIter.hasNext()) {
							throw new RuntimeException(
									"-d option requires argument");
						}
						outputDirName = argIter.next();
					} else if (arg.equals("--help")) {
						usage(System.out);
						return;
					} else {
						throw new RuntimeException("Bad arg: " + arg);
					}
				} else {
					fileName = arg;
				}
			}
			if (fileName == null) {
				throw new RuntimeException("File name must be specified");
			}
			final File outputDir = outputDirName == null ? new File("")
					: new File(outputDirName);
			setOutputDir(outputDir);
			load(new File(fileName));
			// generateIndex();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints command-line usage.
	 * 
	 * @param out
	 *            Output stream
	 */
	private void usage(PrintStream out) {
		out.println("Clapham - Railroad diagram generator");
		out.println();
		out.println("Usage:");
		out.println("  clapham [ options ] filename");
		out.println();
		out.println("Options:");
		out.println("  --help       Print this help");
		out.println("  -d directory Specify output directory");
		out.println("  -o optimize graph");
		out.println("  filename     Name of file containing grammar");
	}

	private enum Dialect implements Language {
		WIRTH {

			public List<ProductionNode> parse(String input)
					throws LanguageParserException {
				try {
					WirthParser parser = new WirthParser(
							new StringReader(input));
					return parser.Syntax();
				} catch (Throwable t) {
					throw new LanguageParserException(t, t.getMessage());
				}
			}
		},
		BNF {

			public List<ProductionNode> parse(String input)
					throws LanguageParserException {
				try {
					BnfParser parser = new BnfParser(new StringReader(input));
					return parser.Syntax();
				} catch (ParseException e) {
					throw new LanguageParserException(e, e.getMessage());
				}
			}
		}
	}

	/**
	 * Output format for graphics.
	 */
	public static enum ImageFormat {
		SVG, PNG,
	}
}

// End Clapham.java
