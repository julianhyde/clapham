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
package net.hydromatic.clapham.chart.java2d;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.hydromatic.clapham.graph.Grammar;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;

/**
 * TODO:
 * 
 * @author Edgar Espina
 * @version $Id: Batik2DChart.java 3 2009-05-11 08:11:57Z jhyde $
 * @since Aug 26, 2008
 */
public class Batik2DChart extends Java2DChart {

    public Batik2DChart(Grammar grammar) {
        super(grammar, createGraphics());
    }

    @Override
    public void drawAndExport(String symbolName, File output)
            throws IOException {
        Dimension size = size(symbolName);
        draw(symbolName);

        File svgFile = new File(output.getParentFile(), symbolName + ".svg");

        SVGGraphics2D graphics = (SVGGraphics2D) g;
        graphics.setSVGCanvasSize(new Dimension(size.width, size.height));
        graphics.stream(svgFile.getPath(), true);

        try {
            toPng(svgFile, output);
        } catch (TranscoderException e) {
            throw new IOException("Can not create: " + output, e);
        }
    }

    private static void toPng(File inFile, File file) throws IOException,
            TranscoderException {
        // Create a PNG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Create the transcoder input.
        TranscoderInput input = new TranscoderInput("file:" + inFile.getPath());

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream(file);
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    }

    private static Graphics2D createGraphics() {
        try {
            final DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            return new SVGGraphics2D(document);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error while creating the chart", e);
        }
    }
}
