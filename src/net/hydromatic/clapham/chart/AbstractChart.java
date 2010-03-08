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
package net.hydromatic.clapham.chart;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Symbol;

public abstract class AbstractChart implements Chart, ChartOptions {

    private static class SizeChart extends AbstractChart {

        private AbstractChart owner;

        private int xMax;

        private int yMax;

        public SizeChart(AbstractChart owner) {
            super(owner.grammar);
            this.owner = owner;
        }

        @Override
        protected void expandBounds(int x, int y) {
            xMax = Math.max(xMax, x);
            yMax = Math.max(yMax, y);
        }

        @Override
        protected void internalDrawArc(int x, int y, int width, int height,
                int startAngle, int arcAngle) {
        }

        @Override
        protected void internalDrawArrow(int x1, int y1, int x2, int y2,
                int[] xpoints, int[] ypoints) {
        }

        @Override
        protected void internalDrawLine(int x1, int y1, int x2, int y2) {
        }

        @Override
        protected void internalDrawRectangle(int x, int y, int width, int height) {
        }

        @Override
        protected void internalDrawString(NodeType nodeType, String text, int x, int y) {
        }

        public ChartOptions createOptions() {
            return null;
        }

        @Override
        public ChartOptions getOptions() {
            return owner.getOptions();
        }

        public ChartOptions createOptions(String fontName) {
            return null;
        }

        public void drawAndExport(String symbolName, File outputDirectory)
                throws IOException {
        }

        public int fontHeightCorrectness() {
            return owner.fontHeightCorrectness();
        }

        public int width() {
            return xMax + 5;
        }

        public int height() {
            return yMax + 10;
        }

    }

    private ChartOptions options;

    protected final Grammar grammar;

    public AbstractChart(Grammar grammar) {
        this.grammar = grammar;
    }

    public final void prepare() {
        for (Symbol s : grammar.nonterminals) {
            s.graph.graphSize = s.graph.l.calcSize(this);
            s.graph.l.setWrapSize(this);
            s.graph.l.calcPos(this, initialY(), false);
            if (Grammar.TRACE) {
                System.out.println("\n\n" + s.graph.graphSize.toString());
            }
        }
        if (Grammar.TRACE) {
            grammar.printNodes(System.out);
        }
    }

    public final void draw(String symbolName) {
        Symbol symbol = symbol(symbolName);

        Point p = new Point(initialX(), initialY() - 30);

        internalDrawLine(initialX() - componentGapWidth() / 4 - arcSize() / 2,
                symbol.graph.l.posLine.y, initialX(), symbol.graph.l.posLine.y);

        symbol.graph.l.drawComponents(this, p, symbol.graph.graphSize);

    }

    public final void drawArc(int x, int y, int width, int height,
            int startAngle,
            int arcAngle) {
        expandBounds(x - width, y - height);
        expandBounds(x + width, y - height);
        expandBounds(x - width, y + height);
        expandBounds(x + width, y + height);

        internalDrawArc(x, y, width, height,
                startAngle == 180 ? 90 : startAngle == 90 ? 180
                        : startAngle == 270 ? 0 : startAngle == 0 ? 270
                                : startAngle, (int) arcAngle);
    }

    public final void drawString(NodeType nodeType, String text, int x, int y) {
        internalDrawString(nodeType, text, x, y);
    }

    protected abstract void internalDrawString(NodeType nodeType, String text, int x, int y);

    public final void drawArrow(int x1, int y1, int x2, int y2,
            Chart.ArrowDirection direction) {
        expandBounds(x1, y1);
        expandBounds(x2, y2);
        int[] xpoints = null;
        int[] ypoints = null;
        switch (direction) {
        case RIGHT:
            xpoints = new int[] { x2, x2 - arrowSize() * 2,
                    x2 - arrowSize() * 2 };
            ypoints = new int[] { y2, y2 - arrowSize(),
                    y2 + arrowSize() };
            break;
        case UP:
            xpoints = new int[] { x2, x2 - arrowSize(), x2 + arrowSize() };
            ypoints = new int[] { y2, y2 + arrowSize() * 2,
                    y2 + arrowSize() * 2 };
            break;
        case LEFT:
            xpoints = new int[] { x2, x2 + arrowSize() * 2,
                    x2 + arrowSize() * 2 };
            ypoints = new int[] { y2, y2 + arrowSize(),
                    y2 - arrowSize() };
            break;
        case DOWN:
            xpoints = new int[] { x2, x2 - arrowSize(), x2 + arrowSize() };
            ypoints = new int[] { y2, y2 - arrowSize() * 2,
                                    y2 - arrowSize() * 2 };
            break;
        }
        internalDrawArrow(x1, y1, x2, y2, xpoints, ypoints);
    }

    protected abstract void internalDrawArrow(int x1, int y1, int x2, int y2,
            int[] xpoints, int[] ypoints);

    public final void drawRectangle(int x, int y, int width, int height) {
        expandBounds(x, y);
        expandBounds(x + width, y + height);
        internalDrawRectangle(x, y, width, height);
    }

    protected abstract void internalDrawRectangle(int x, int y, int width,
            int height);

    public final void drawLine(int x1, int y1, int x2, int y2) {
        expandBounds(x1, y1);
        expandBounds(x2, y2);
        internalDrawLine(x1, y1, x2, y2);
    }

    protected abstract void internalDrawLine(int x1, int y1, int x2, int y2);

    public final void drawArcCorner(int x, int y, int startAngle) {
        drawArc(x, y, arcSize(), arcSize(), startAngle, 90);
    }

    public final void drawArcCorner(int x, int y, int arcSize, int startAngle) {
        drawArc(x, y, arcSize, arcSize, startAngle, 90);
    }

    protected abstract void internalDrawArc(int x, int y, int width,
            int height,
            int startAngle,
            int arcAngle);

    protected void expandBounds(int x, int y) {
    }

    public Dimension size(String symbolName) {
        // could not found a better way of doing this
        SizeChart sizeChart = new SizeChart(this);
        sizeChart.prepare();
        // just fake the drawing
        sizeChart.draw(symbolName);
        return new Dimension(sizeChart.width(), sizeChart.height());
    }

    protected Symbol symbol(String name) {
        Symbol symbol = grammar.symbolMap.get(name);
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol not found: " + name);
        }
        return symbol;
    }

    public ChartOptions getOptions() {
        return options;
    }

    public void setOptions(ChartOptions options) {
        this.options = options;
    }

    public int fontHeight() {
        return getOptions().fontHeight();
    }

    public int initialX() {
        return getOptions().initialX();
    }

    public int initialY() {
        return getOptions().initialY();
    }

    public int stringWidth(String text) {
        return getOptions().stringWidth(text);
    }

    public int componentGapWidth() {
        return getOptions().componentGapWidth();
    }

    public int componentGapHeight() {
        return getOptions().componentGapHeight();
    }

    public int arrowSize() {
        return getOptions().arrowSize();
    }

    public int arcSize() {
        return getOptions().arcSize();
    }

    public int symbolGapWidth() {
        return getOptions().symbolGapWidth();
    }

    public int symbolGapHeight() {
        return getOptions().symbolGapHeight();
    }

    public void withInitialLocation(int x, int y) {
        getOptions().withInitialLocation(x, y);
    }

    public boolean showBorders() {
        return getOptions().showBorders();
    }

    public boolean optimize() {
        return getOptions().optimize();
    }

    public ChartOptions withArcSize(int size) {
        getOptions().withArcSize(size);
        return getOptions();
    }

    public ChartOptions withComponentGapHeight(int height) {
        getOptions().withComponentGapHeight(height);
        return getOptions();
    };

    public ChartOptions withOptimize(boolean optimize) {
        getOptions().withOptimize(optimize);
        return getOptions();
    }

    public ChartOptions withIterationOrder(ChartOrder rightToLeft) {
        getOptions().withIterationOrder(rightToLeft);
        return getOptions();
    }
    
    public ChartOrder iterationOrder() {
        return getOptions().iterationOrder();
    }
}
