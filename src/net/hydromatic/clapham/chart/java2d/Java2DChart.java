/*
// $Id: Chart.java 3 2009-05-11 08:11:57Z jhyde $
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
// Copyright (c) 2005 Stefan Schoergenhumer, Markus Dopler
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;

import net.hydromatic.clapham.chart.AbstractChart;
import net.hydromatic.clapham.chart.AbstractChartOptions;
import net.hydromatic.clapham.chart.ChartOptions;
import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Size;

/**
 * TODO:
 * 
 * @author jhyde
 * @version $Id: Java2DChart.java 3 2009-05-11 08:11:57Z jhyde $
 * @since Aug 26, 2008
 */
public class Java2DChart extends AbstractChart {
    // Constants
    public static final Color ITER_COLOR = Color.PINK;
    public static final Color EPS_COLOR = Color.DARK_GRAY; // was DarkKhaki
    public static final Color OPT_COLOR = Color.DARK_GRAY; // was DarkKhaki
    public static final Color RERUN_COLOR = Color.GREEN;
    public static final Color RERUN1_COLOR = Color.magenta; // was Fuschia
    public static final Color N_NT_COLOR = Color.CYAN; // was PaleGreen

    // Default Settings
    private static final Font defaultCharFont = Font.decode("Serif")
            .deriveFont(Font.PLAIN, 12f);

    public static final BasicStroke STROKE1 = new BasicStroke(1f);
    private static final Stroke defaultLineStroke = STROKE1;
    private static final Color defaultCharColor = Color.BLACK;

    // thickness of the line
    public Stroke lineStroke = defaultLineStroke;

    /** color of the line */
    public Color lineColor = Color.BLACK;

    /** fontColor of the T and NT symbols */
    public Color charColor = defaultCharColor;

    /** the total size of the current Rule */
    private Size symbolSize = new Size(1, 1);

    /**
     * the graphics object from the EBNFForm on which the drawing takes place
     */
    protected final Graphics2D g;

    public Java2DChart(Grammar grammar, Graphics2D graphics) {
        super(grammar);
        this.g = graphics;
    }

    private ChartOptions createOptions(Font font) {
        g.setFont(font);
        return new AbstractChartOptions() {

            public int stringWidth(String text) {
                return g.getFontMetrics().stringWidth(text) + symbolGapWidth()
                        * 6;
            }

            public int fontHeight() {
                return g.getFontMetrics().getHeight();
            }
        };
    }

    public ChartOptions createOptions(String font) {
        return createOptions(Font.decode(font));
    }

    public ChartOptions createOptions() {
        return createOptions(defaultCharFont);
    }

    protected void internalDrawString(NodeType nodeType, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    public Size getSymbolSize() {
        return symbolSize;
    }

    public void drawAndExport(String symbolName, File output)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void internalDrawArrow(int x1, int y1, int x2, int y2,
            int[] xpoints, int[] ypoints) {
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.drawLine(x1, y1, x2, y2);

        g.fillPolygon(xpoints, ypoints, 3);
    }
    
    @Override
    protected void internalDrawArc(int x, int y, int width, int height,
            int startAngle, int arcAngle) {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    protected void internalDrawLine(int x1, int y1, int x2, int y2) {
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.drawLine(x1, y1, x2, y2);
    }

    protected void internalDrawRectangle(int x, int y, int width, int height) {
        g.drawRect(x, y, width, height);
    }
    
    @Override
    protected void internalDrawRoundRectangle(int x, int y, int width,
            int height, int arcWidth, int arcHeight) {
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);   
    }

    public int fontHeightCorrectness() {
        return symbolGapHeight() * 3;
    }
}

// End Java2DChart.java
