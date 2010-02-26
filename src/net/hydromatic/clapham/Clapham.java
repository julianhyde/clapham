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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.hydromatic.clapham.chart.Chart;
import net.hydromatic.clapham.chart.java2d.Java2DChart;
import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.GrammarFactory;
import net.hydromatic.clapham.parser.Language;
import net.hydromatic.clapham.parser.LanguageParserException;
import net.hydromatic.clapham.parser.ProductionNode;
import net.hydromatic.clapham.parser.bnf.BnfParser;
import net.hydromatic.clapham.parser.bnf.ParseException;
import net.hydromatic.clapham.parser.wirth.WirthParser;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;

/**
 * Command line utility Clapham, the railroad diagram generator.
 *
 * @author jhyde
 * @version $Id$
 * @since Sep 11, 2008
 */
public class Clapham {
    private File outputDir;
    private boolean outputEscapeFilename;
    private final HashSet<String> fileNameSet = new HashSet<String>();
    private final List<String> nameList = new ArrayList<String>();
    private Grammar grammar;
    private EnumSet<ImageFormat> imageFormatSet;
    private Map<Pair<String, ImageFormat>, File> imageFileNames =
        new HashMap<Pair<String, ImageFormat>, File>();
    private boolean outputDirCreated;

    public Clapham() {
        this.outputEscapeFilename = true;
        this.imageFormatSet = EnumSet.of(ImageFormat.PNG, ImageFormat.SVG);
    }

    public void setOutputDir(File file) {
        this.outputDir = file;
    }

    public void setOutputEscapeFilename(boolean b) {
        this.outputEscapeFilename = b;
    }

    public void generateIndex() {
        final File htmlIndexFile = makeFile("index", ".html");
        FileWriter w = null;
        try {
            // Open index.html for writing
            w = new FileWriter(htmlIndexFile);
            final PrintWriter pw = new PrintWriter(w);
            pw.println("<html>");
            pw.println("<body>");
            pw.println("<table border='0'>");

            for (String name : nameList) {
                // add link to index
                File svgFile =
                    imageFileNames.get(
                        new Pair<String, ImageFormat>(
                            name,
                            ImageFormat.SVG));
                File pngFile =
                    imageFileNames.get(
                        new Pair<String, ImageFormat>(
                            name,
                            ImageFormat.PNG));
                if (svgFile == null) {
                    svgFile = pngFile;
                }
                if (pngFile == null) {
                    pngFile = svgFile;
                }
                pw.println(
                    "<tr><td>" + name + "</td><td>" + "<a href='"
                    + svgFile.getName() + "'><img src='"
                    + pngFile.getName() + "'/>" + "</td></tr>");
            }
            // close index.html
            pw.println("</table>");
            pw.println("</body>");
            pw.println("</html>");
            pw.close();
            System.out.println("Generated index: " + htmlIndexFile);
        } catch (IOException e) {
            throw new RuntimeException(
                "Error while generating index file "
                + htmlIndexFile);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void drawAll() {
        for (String name : nameList) {
            draw(name);
        }
    }

    /**
     * Creates the output directory, if necessary, and prints a message. Only
     * does this once.
     */
    private void checkOutputDir() {
        if (outputDir != null) {
            if (!outputDirCreated) {
                if (outputDir.mkdirs()) {
                    System.out.println("Created output directory " + outputDir);
                } else {
                    System.out.println("Output directory " + outputDir);
                }
                outputDirCreated = true;
            }
        }
    }

    public void draw(String symbolName) {
        checkGrammarLoaded();
        checkOutputDir();
        try {
            final DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final SVGGraphics2D graphics = new SVGGraphics2D(document);
            final Chart chart = new Java2DChart(grammar, graphics);
            chart.calcDrawing();
            chart.drawComponent(symbolName);

            // Write .svg file. If we want to generate .png we generate the
            // .svg file and delete later.
            final File svgFile;
            final boolean generateSvg = imageFormatSet
                    .contains(ImageFormat.SVG);
            final boolean generatePng = imageFormatSet
                    .contains(ImageFormat.PNG);
            String gen = "";
            if (generateSvg || generatePng) {
                svgFile = makeFile(symbolName, ".svg");
                if (generateSvg) {
                    imageFileNames.put(new Pair<String, ImageFormat>(
                            symbolName, ImageFormat.SVG), svgFile);
                }
                gen = svgFile.getPath();
                final String path = svgFile.getPath();
                graphics.stream(path, true);
            } else {
                svgFile = null;
            }

            // convert to .png file
            if (generatePng) {
                final File pngFile = makeFile(symbolName, ".png");
                imageFileNames.put(
                    new Pair<String, ImageFormat>(
                        symbolName,
                        ImageFormat.PNG),
                    pngFile);
                if (!gen.equals("")) {
                    gen += ", ";
                }
                gen += pngFile.getPath();
                toPng(svgFile, pngFile);
                if (!generateSvg) {
                    svgFile.delete();
                }
            }
            System.out.println("Symbol " + symbolName + " (" + gen + ")");
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(
                    "Error while generating chart for symbol " + symbolName);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while generating chart for symbol " + symbolName);
        } catch (TranscoderException e) {
            throw new RuntimeException(
                    "Error while generating chart for symbol " + symbolName);
        }
    }

    /**
     * Checks that the grammar is loaded.
     *
     * @throws RuntimeException
     *             if grammar is not loaded
     */
    private void checkGrammarLoaded() {
        if (grammar == null) {
            throw new RuntimeException("No grammar loaded");
        }
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
            // Build grammar.
            grammar =
                GrammarFactory.build(
                    dialect,
                    new FileReader(inputFile),
                    true);
            nameList.clear();
            nameList.addAll(grammar.symbolMap.keySet());
            Collections.sort(nameList);
        } catch (LanguageParserException ex) {
            throw new RuntimeException(
                "Error while loading file '" + inputFile.getPath() + "'.", ex);
        } catch (IOException e) {
            throw new RuntimeException(
                "Error while loading file '" + inputFile + "'.", e);
        }
    }

    /**
     * Generates a name for an output file.
     *
     * <p>The file is in the output directory (if
     * {@link #setOutputDir(java.io.File)} specified); punctuation is replaced
     * with underscores (if {@link #setOutputEscapeFilename(boolean) enabled});
     * and is unique among output files generated this run.
     *
     * <p>If you want to know what file a symbol was generated to, record the
     * generated file name in {@link #imageFileNames}.
     *
     * @param name
     *            Base name of file
     * @param suffix
     *            Suffix of file
     * @return Name of output file
     */
    private File makeFile(String name, String suffix) {
        String s = name + suffix;
        if (outputEscapeFilename) {
            // Replace spaces etc. with underscores, then make sure that the
            // name is distinct from other file name we have generated this
            // run.
            s = s.replaceAll("[^A-Za-z0-9_.]", "_");
            s = uniquify(s, 128, fileNameSet);
        }
        return new File(outputDir, s);
    }

    /**
     * Makes a name distinct from other names which have already been used and
     * shorter than a length limit, adds it to the list, and returns it.
     *
     * @param name
     *            Suggested name, may not be unique
     * @param maxLength
     *            Maximum length of generated name
     * @param nameList
     *            Collection of names already used
     *
     * @return Unique name
     */
    private static String uniquify(
        String name,
        int maxLength,
        Collection<String> nameList)
    {
        assert name != null;
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength);
        }
        if (nameList.contains(name)) {
            String aliasBase = name;
            int j = 0;
            while (true) {
                name = aliasBase + j;
                if (name.length() > maxLength) {
                    aliasBase = aliasBase.substring(0, aliasBase.length() - 1);
                    continue;
                }
                if (!nameList.contains(name)) {
                    break;
                }
                j++;
            }
        }
        nameList.add(name);
        return name;
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
            load(new File(fileName));
            final File outputDir = outputDirName == null ? new File("")
                    : new File(outputDirName);
            setOutputDir(outputDir);
            setOutputFormats(EnumSet.of(ImageFormat.SVG, ImageFormat.PNG));
            drawAll();
            generateIndex();
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
        out.println("  filename     Name of file containing grammar");
    }

    /**
     * Sets the format(s) in which to generate images. The list must not be
     * empty.
     *
     * @param imageFormatSet
     *            Set of output formats
     */
    public void setOutputFormats(EnumSet<ImageFormat> imageFormatSet) {
        assert imageFormatSet != null;
        assert imageFormatSet.size() > 0;
        this.imageFormatSet = imageFormatSet;
    }

    public static void toPng(File inFile, File file)
        throws IOException,
               TranscoderException
    {
        // Create a PNG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Create the transcoder input.
        TranscoderInput input =
            new TranscoderInput("file:" + inFile.getPath());

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream(file);
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    }

    private enum Dialect implements Language {
        WIRTH {
            @Override
            public List<ProductionNode> parse(String input)
                throws LanguageParserException
            {
                try {
                    WirthParser parser =
                        new WirthParser(
                            new StringReader(input));
                    return parser.Syntax();
                } catch (Throwable t) {
                    throw new LanguageParserException(t, t.getMessage());
                }
            }
        },
        BNF {
            @Override
            public List<ProductionNode> parse(String input)
                throws LanguageParserException
                {
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

    private static class Pair<L, R> {
        L left;
        R right;

        Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public int hashCode() {
            return (left == null ? 0 : left.hashCode()) << 4
                    ^ (right == null ? 1 : right.hashCode());
        }

        public boolean equals(Object obj) {
            return obj instanceof Pair && eq(left, ((Pair) obj).left)
                    && eq(right, ((Pair) obj).right);
        }

        private static boolean eq(Object o, Object o2) {
            return o == null ? o2 == null : o.equals(o2);
        }
    }
}

// End Clapham.java
