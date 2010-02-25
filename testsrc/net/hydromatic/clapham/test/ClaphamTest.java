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
package net.hydromatic.clapham.test;

import junit.framework.TestCase;
import net.hydromatic.clapham.parser.wirth.WirthParser;
import net.hydromatic.clapham.parser.wirth.ParseException;
import net.hydromatic.clapham.parser.*;
import net.hydromatic.clapham.parser.bnf.BnfParser;
import net.hydromatic.clapham.graph.*;
import net.hydromatic.clapham.Clapham;

import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

/**
 * Unit test for {@link net.hydromatic.clapham.Clapham}.
 *
 * @author jhyde
 * @since 2008/10/31
 */
public class ClaphamTest extends TestCase {
    /**
     * Grammar for Wirth Syntax Notation (WSN) expressed in its own grammar.
     *
     * <p>See <a href="http://en.wikipedia.org/wiki/Wirth_syntax_notation">Wirth
     * Syntax Notation</a>.
     */
    private static final String WIRTH_GRAMMAR =
        "SYNTAX     = { PRODUCTION } .\n"
        + "PRODUCTION = IDENTIFIER \"=\" EXPRESSION \".\" .\n"
        + "EXPRESSION = TERM { \"|\" TERM } .\n"
        + "TERM       = FACTOR { FACTOR } .\n"
        + "FACTOR     = IDENTIFIER\n"
        + "           | LITERAL\n"
        + "           | \"[\" EXPRESSION \"]\"\n"
        + "           | \"(\" EXPRESSION \")\"\n"
        + "           | \"{\" EXPRESSION \"}\" .\n"
        + "IDENTIFIER = letter { letter } .\n"
        + "LITERAL    = \"\"\"\" character { character } \"\"\"\" .";

    /**
     * Grammar for Wirth Syntax Notation (WSN) expressed extended BNF.
     */
    private static final String WIRTH_GRAMMAR_BNF =
        "SYNTAX     ::= ( PRODUCTION )*\n"
        + "PRODUCTION ::= IDENTIFIER <EQUALS> EXPRESSION \".\"\n"
        + "EXPRESSION ::= TERM ( <BAR> TERM )*\n"
        + "TERM       ::= FACTOR+\n"
        + "FACTOR     ::= IDENTIFIER\n"
        + "           | LITERAL\n"
        + "           | <LBRACKET> EXPRESSION <RBRACKET>\n"
        + "           | <LPAREN> EXPRESSION <RPAREN>\n"
        + "           | <LBRACE> EXPRESSION <RBRACE>\n"
        + "IDENTIFIER ::= <LETTER>+\n"
        + "LITERAL    ::= <QUOT> <CHARACTER>+ <QUOT>";

    //    BNF (not supported yet)
    // See http://en.wikipedia.org/wiki/Backus–Naur_Form
    //
    // <syntax> ::= <rule> | <rule> <syntax>
    // <rule>   ::= <opt-whitespace> "<" <rule-name> ">" <opt-whitespace> "::="
    //                 <opt-whitespace> <expression> <line-end>
    // <opt-whitespace> ::= " " <opt-whitespace> | ""  <!-- "" is empty string, i.e. no whitespace -->
    // <expression>     ::= <list> | <list> "|" <expression>
    // <line-end>       ::= <opt-whitespace> <EOL> | <line-end> <line-end>
    // <list>    ::= <term> | <term> <opt-whitespace> <list>
    // <term>    ::= <literal> | "<" <rule-name> ">"
    // <literal> ::= '"' <text> '"' | "'" <text> "'" <!-- actually, the original BNF did not use quotes -->

    // Augmented BNF (not supported yet)
    // See http://en.wikipedia.org/wiki/Augmented_Backus–Naur_Form
    //
    // As BNF, but:
    // rule = definition ; comment CR LF
    
    public void testParse() throws ParseException {
        final WirthParser parser =
            new WirthParser(new StringReader(WIRTH_GRAMMAR));
        final List<ProductionNode> productionNodes = parser.Syntax();
        StringBuilder buf = new StringBuilder();
        WirthParser.toString(buf,"{",productionNodes, "}");
        String s = buf.toString();
        assertEquals(
            "{SYNTAX = RepeatNode(PRODUCTION), "
            + "PRODUCTION = SequenceNode(IDENTIFIER, \"\"=\"\", EXPRESSION, \"\".\"\"), "
            + "EXPRESSION = SequenceNode(TERM, RepeatNode(SequenceNode(\"\"|\"\", TERM))), "
            + "TERM = SequenceNode(FACTOR, RepeatNode(FACTOR)), "
            + "FACTOR = AlternateNode(IDENTIFIER,"
            + " LITERAL,"
            + " SequenceNode(\"\"[\"\", EXPRESSION, \"\"]\"\"),"
            + " SequenceNode(\"\"(\"\", EXPRESSION, \"\")\"\"),"
            + " SequenceNode(\"\"{\"\", EXPRESSION, \"\"}\"\")), "
            + "IDENTIFIER = SequenceNode(letter, RepeatNode(letter)), "
            + "LITERAL = SequenceNode(\"\"\"\"\"\", character, RepeatNode(character), \"\"\"\"\"\")}",
            s);
    }

    public void testParseBnf()
        throws ParseException, net.hydromatic.clapham.parser.bnf.ParseException,
        SVGGraphics2DIOException, ParserConfigurationException
    {
        final BnfParser parser =
            new BnfParser(new StringReader(WIRTH_GRAMMAR_BNF));
        final List<ProductionNode> productionNodes = parser.Syntax();
        StringBuilder buf = new StringBuilder();
        WirthParser.toString(buf,"{",productionNodes, "}");
        String s = buf.toString();
        assertEquals(
            "{SYNTAX = RepeatNode(PRODUCTION), "
            + "PRODUCTION = SequenceNode(IDENTIFIER, EQUALS, EXPRESSION, \".\"), "
            + "EXPRESSION = SequenceNode(TERM, RepeatNode(SequenceNode(BAR, TERM))), "
            + "TERM = MandatoryRepeatNode(FACTOR), "
            + "FACTOR = AlternateNode(IDENTIFIER,"
            + " LITERAL,"
            + " SequenceNode(LBRACKET, EXPRESSION, RBRACKET),"
            + " SequenceNode(LPAREN, EXPRESSION, RPAREN),"
            + " SequenceNode(LBRACE, EXPRESSION, RBRACE)), "
            + "IDENTIFIER = MandatoryRepeatNode(LETTER), "
            + "LITERAL = SequenceNode(QUOT, MandatoryRepeatNode(CHARACTER), QUOT)}",
            s);
        doDraw(productionNodes, "FACTOR");
    }

    /**
     * Test that loads Aspen grammar and formats it. Disabled.
     *
     * @throws Exception on error
     */
    public void _testParseAspen() throws Exception
    {
        final BnfParser parser =
            new BnfParser(new FileReader("C:/open/clapham/aspen.bnf"));
        final List<ProductionNode> productionNodes = parser.Syntax();
        StringBuilder buf = new StringBuilder();
        WirthParser.toString(buf,"{",productionNodes, "}");
        String s = buf.toString();
        System.out.println(s);
        doDraw(productionNodes, "FACTOR");
    }

    public void testDraw()
        throws ParseException,
        ParserConfigurationException,
        SVGGraphics2DIOException
    {
        final WirthParser parser =
            new WirthParser(new StringReader(WIRTH_GRAMMAR));
        final List<ProductionNode> productionNodes = parser.Syntax();
        doDraw(productionNodes, "FACTOR");
    }

    public static void main(String[] args)
        throws ParseException, ParserConfigurationException,
        IOException, TranscoderException, SAXException
    {
        final WirthParser parser =
            new WirthParser(new StringReader(WIRTH_GRAMMAR));
        final List<ProductionNode> productionNodes = parser.Syntax();
        final Grammar grammar = Clapham.buildGrammar(productionNodes);
        final List<String> nameList =
            new ArrayList<String>(grammar.symbolMap.keySet());
        Collections.sort(nameList);
        final File dir = new File("out");
        dir.mkdirs();

        // open index.html
        final File index = new File(dir, "index.html");
        final FileWriter w = new FileWriter(index);
        final PrintWriter pw = new PrintWriter(w);
        pw.println("<html>");
        pw.println("<body>");

        for (String name : nameList) {
//            if (!name.equals("EXPRESSION")) continue;
            final Symbol symbol = grammar.symbolMap.get(name);
            assert symbol.graph != null;

            final DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final SVGGraphics2D graphics = new SVGGraphics2D(document);
            final Chart chart = new Chart(grammar, graphics);
            chart.calcDrawing();
            chart.drawComponent(symbol);

            // write .svg file
            final File svgFile = new File(dir, name + ".svg");
            final String path = svgFile.getPath();
            graphics.stream(path, true);

            // convert to .png file
            final File pngFile = new File(dir, name + ".png");
            Clapham.toPng(
                svgFile,
                pngFile);

            // add link to index
            pw.println(
                "<a href='"
                + name
                + ".svg"
                + "'><img src='"
                + symbol.name
                + ".png"
                + "'/>"
                + "<br/>");
        }

        // close index.html
        pw.println("</body>");
        pw.println("</html>");
        pw.close();
    }

    public static void doDraw(
        List<ProductionNode> productionNodes,
        String symbolName)
        throws ParserConfigurationException, SVGGraphics2DIOException
    {
        final Grammar grammar = Clapham.buildGrammar(productionNodes);
        Symbol symbol = grammar.symbolMap.get(symbolName);
        assert symbol.graph != null;
        final DocumentBuilder documentBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        final SVGGraphics2D graphics = new SVGGraphics2D(document);
        final Chart chart = new Chart(grammar, graphics);
        chart.calcDrawing();
        chart.drawComponent(symbol);
        graphics.stream("my.svg");
    }
}

// End ClaphamTest.java
