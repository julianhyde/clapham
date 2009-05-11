/*
// $Id: $
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.hydromatic.clapham.graph;

import org.apache.batik.svggen.SVGGraphics2D;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;

/**
 * TODO:
*
* @author jhyde
* @version $Id: $
* @since Aug 26, 2008
*/
public class Chart {
    // Constants

    public static final Color ITER_COLOR = Color.PINK;
    public static final Color EPS_COLOR = Color.DARK_GRAY;  // was DarkKhaki
    public static final Color OPT_COLOR = Color.DARK_GRAY;  // was DarkKhaki
    public static final Color RERUN_COLOR = Color.GREEN;
    public static final Color RERUN1_COLOR = Color.magenta; // was Fuschia
    public static final Color N_NT_COLOR = Color.CYAN; // was PaleGreen

    private final Font titleFont =
        Font.decode("Serif").deriveFont(0, 14f);
    private final Color titleColor = Color.BLACK;

    // Default Settings

    /** show the rectangles around the components */
    private static final int   defaultComponentArcSize	= 16;

    public final boolean showBorders = false;
    private static final int   defaultComponentGapWidth  	= 32;
    private static final int   defaultComponentGapHeight 	= 10;
    private static final Font  defaultCharFont =
        Font.decode("Serif").deriveFont(Font.PLAIN, 12f);

    private static final int   defaultArrowSize			= 3;
    public static final BasicStroke STROKE1 = new BasicStroke(1f);
    private static final Stroke defaultLineStroke = STROKE1;
    private static final Color defaultLineColor           = Color.BLACK;
    private static final int   defaultSymbolGapHeight 	= 4;
    private static final Color defaultCharColor			= Color.BLACK;

    // Initialize variables with default settings

    // size of the arcs
    public int 	 componentArcSize 	= defaultComponentArcSize;

    // gap between subcomponent size and actual size
    public int 	 componentGapWidth 	= defaultComponentGapWidth;

    // gap between subcomponent size and actual size
    public int 	 componentGapHeight	= defaultComponentGapHeight;

    // font of the t and nt symbols
    public Font  charFont			= defaultCharFont;

    // size of the arrows
    public int 	 arrowSize			= defaultArrowSize;

    // thickness of the line
    public Stroke lineStroke        = defaultLineStroke;

    /** color of the line */
    public Color lineColor          = Color.BLACK;

    /** fontColor of the T and NT symbols */
    public Color charColor			= defaultCharColor;

    /** gap between the line of the symbol and the font */
    public int symbolGapHeight = defaultSymbolGapHeight;

    public final int symbolGapWidth = 2;

    /** the total size of the current Rule */
    private Size 	symbolSize 				= new Size(1,1);

    private float xMin = Integer.MAX_VALUE;
    private float yMin = Integer.MAX_VALUE;
    private float xMax = Integer.MIN_VALUE;
    private float yMax = Integer.MIN_VALUE;

    /** needed to make the gap between the symbol and and the font possible */
    public int 	 getFontHeight() {
        return (int) (charFont.getSize2D() + symbolGapHeight);
    }

    /** where the drawing starts (X) */
    private final int 		beginningYCoordinate 	= 40;

    /** where the drawing starts (Y) */
    public final int 		beginningXCoordinate 	= 50;

    /** the graphics object from the EBNFForm on which the drawing takes place */
    private final Grammar grammar;

    final Graphics2D g;

    public Chart(Grammar grammar, SVGGraphics2D graphics) {
        this.grammar = grammar;
        this.g = graphics;
    }

    public Dimension getDimension() {
        assert xMin >= 0;
        assert yMin >= 0;
        return new Dimension((int) xMax + 10, (int) yMax + 10);
    }

    public int getStringWidth(Font font, String text) {
        g.setFont(font);
        final FontRenderContext context = g.getFontRenderContext();
        final GlyphVector glyphVector =
            this.charFont.layoutGlyphVector(
                context, text.toCharArray(), 0, text.length(), 0);
        double width = glyphVector.getVisualBounds().getWidth();
        //System.out.println("width of " + text + " is " + width);
        width *= 1.35;
        return (int) width;
    }

    public void drawString(
        String text, Font font, Color color, float x, float y)
    {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    public void setCharFont(Font value) {
        charFont = value;
    }

    public Font getCharFont() {
        return charFont;
    }

    public void setCharColor(Color value) {
        this.charColor = value;
    }

    public Color getCharColor() {
        return charColor;
    }

    public void setArrowSize(int value) {
        this.arrowSize = value;
    }

    public int getArrowSize() {
        return arrowSize;
    }

    public void setSymbolGapHeight(int value) {
        this.symbolGapHeight = value;
    }

    public int getSymbolGapHeight() {
        return symbolGapHeight;
    }

    void setComponentGapHeight(int value) {
        componentGapHeight = value;
        final int fontHeight = getFontHeight();
        if (componentGapHeight / 2 + fontHeight / 2
            < Chart.defaultComponentArcSize) {
            componentArcSize = (componentGapHeight + fontHeight) / 2;
        } else {
            componentArcSize = Chart.defaultComponentArcSize;
        }
        if (componentArcSize % 2 != 0) {
            componentArcSize -= 1;
        }
    }

    public int getComponentGapHeight() {
        return componentGapHeight;
    }

    public void setComponentGapWidth(int value) {
        componentGapWidth = value;
    }

    public int getComponentGapWidth() {
        return componentGapWidth;
    }

    public Size getSymbolSize() {
        return symbolSize;
    }

    public void restoreDefaultSettings() {
        componentArcSize = defaultComponentArcSize;
        componentGapWidth = defaultComponentGapWidth;
        setComponentGapHeight(Chart.defaultComponentGapHeight);
        charFont = defaultCharFont;
        arrowSize = Chart.defaultArrowSize;
        lineStroke = defaultLineStroke;
        lineColor = defaultLineColor;
        symbolGapHeight = defaultSymbolGapHeight;
        charColor = defaultCharColor;
    }

    public void drawComponent(Symbol s) {
        if (s == null) {
            return;
        }
        symbolSize = new Size(
            s.graph.graphSize.getWidth()
                + beginningXCoordinate
                + componentGapWidth * 2,
            s.graph.graphSize.getHeight()
                + beginningYCoordinate
                + componentGapHeight * 2
                + 5);
//			EbnfForm.Drawarea=new Bitmap(Node.getSymbolSize().getWidth(),Node.getSymbolSize().getHeight(),	System.Drawing.Imaging.PixelFormat.Format24bppRgb);

        //decide either draw on visualized bitmap or record a metafile
        g.setColor(Color.WHITE);
        g.fillRect(
            0,
            0,
            (int) symbolSize.getWidth(),
            (int) symbolSize.getHeight());
        g.setColor(Color.BLACK);
        drawString(
            s.name,
            titleFont,
            titleColor,
            beginningXCoordinate - 20,
            beginningYCoordinate - 30);
        //g.DrawRectangle(new Pen(Color.Orange,2),p.X,p.Y+30,s.graph.graphSize.getWidth(),s.graph.graphSize.getHeight());
        g.setStroke(lineStroke);
        g.setColor(lineColor);
        g.drawLine(
            beginningXCoordinate
                - componentGapWidth / 4
                - componentArcSize / 2,
            (int) s.graph.l.posLine.y,
            beginningXCoordinate,
            (int) s.graph.l.posLine.y);
        Point2D.Float p =
            new Point2D.Float(
                beginningXCoordinate,
                beginningYCoordinate - 30);
        s.graph.l.drawComponents(this, p, s.graph.graphSize);
//        final SizeMapper sizeMapper = new SizeMapper();
//        s.graph.l.accept(sizeMapper);
//        s.graph.r.accept(sizeMapper);
        final Dimension dimension = getDimension();
        ((SVGGraphics2D) g).setSVGCanvasSize(dimension);
    }

    public void calcDrawing() {
        for (Symbol s : grammar.nonterminals) {
            s.graph.graphSize = s.graph.l.calcSize(this);
            s.graph.l.setWrapSize(this);
            s.graph.l.calcPos(this, beginningYCoordinate);
            if (Grammar.TRACE) {
                System.out.println("\n\n" + s.graph.graphSize.toString());
            }
        }
        if (Grammar.TRACE) {
            grammar.printNodes(System.out);
        }
    }

    // draws arrows for different directions
    void drawArrow(
        float x1,
        float y1,
        float x2,
        float y2,
        Grammar.Direction direction)
    {
        drawArrow(
            (int) x1, (int) y1, (int) x2, (int) y2, direction);
    }

    private void drawArrow(
        int x1,
        int y1,
        int x2,
        int y2,
        Grammar.Direction direction)
    {
        expandBounds(x1, y1);
        expandBounds(x2, y2);
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.drawLine(x1, y1, x2, y2);
        switch (direction) {
        case RIGHT:
            g.fillPolygon(
                new int[] {x2, x2 - arrowSize * 2, x2 - arrowSize * 2},
                new int[] {y2, y2 - arrowSize, y2 + arrowSize},
                3);
            break;
        case UP:
            g.fillPolygon(
                new int[] {x2, x2 - arrowSize, x2 + arrowSize},
                new int[] {y2, y2 + arrowSize * 2, y2 + arrowSize * 2},
                3);
            break;
        case LEFT:
            g.fillPolygon(
                new int[] {x2, x2 + arrowSize * 2, x2 + arrowSize * 2},
                new int[] {y2, y2 + arrowSize, y2 - arrowSize},
                3);
            break;
        case DOWN:
            g.fillPolygon(
                new int[] {x2, x2 - arrowSize, x2 + arrowSize},
                new int[] {y2, y2 - arrowSize * 2, y2 - arrowSize * 2},
                3);
            break;
        }
	}

    private void expandBounds(float x, float y) {
        if (x < xMin) {
            xMin = x;
        }
        if (y < yMin) {
            yMin = y;
        }
        if (x > xMax) {
            xMax = x;
        }
        if (y > yMax) {
            yMax = y;
        }
    }

    void drawArc(
        Stroke stroke,
        Color color,
        float x,
        float y,
        float width,
        float height,
        float startAngleF,
        float arcAngle)
    {
        expandBounds(x - width, y - height);
        expandBounds(x + width, y - height);
        expandBounds(x - width, y + height);
        expandBounds(x + width, y + height);
        int startAngle = (int) startAngleF;
        g.setStroke(stroke);
        g.setColor(color);
        g.drawArc(
            (int) x,
            (int) y,
            (int) width,
            (int) height,
            startAngle == 180 ? 90
                : startAngle == 90 ? 180
                    : startAngle == 270 ? 0
                        : startAngle == 0 ? 270
                            : startAngle,
            (int) arcAngle);
    }

    void drawArcCorner(
        float x,
        float y,
        float arcSize,
        float startAngle)
    {
        drawArc(
            lineStroke,
            lineColor,
            x,
            y,
            arcSize,
            arcSize,
            startAngle,
            90);
    }

    void drawArcCorner(
        float x,
        float y,
        float startAngle)
    {
        drawArc(
            lineStroke,
            lineColor,
            x,
            y,
            componentArcSize,
            componentArcSize,
            startAngle,
            90);
    }

    void drawLine(
        float x, float y, float x1, float y1)
    {
        expandBounds(x, y);
        expandBounds(x1, y1);
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.drawLine((int) x, (int) y, (int) x1, (int) y1);
    }

    void drawRectangle(
        Color color,
        Stroke stroke,
        float x,
        float y,
        float width,
        float height)
    {
        expandBounds(x, y);
        expandBounds(x + width, y + height);
        g.setColor(color);
        g.setStroke(stroke);
        g.drawRect((int) x, (int) y, (int) width, (int) height);
    }

    interface NodeVisitor {
        void visit(Node node);
    }

    static class SizeMapper implements NodeVisitor {
        private int x1 = Integer.MAX_VALUE;
        private int y1 = Integer.MAX_VALUE;
        private int x2 = Integer.MIN_VALUE;
        private int y2 = Integer.MIN_VALUE;

        public void visit(Node node) {
            foo(node.posBegin);
            foo(node.posEnd);
            foo(node.posLine);
            node.visitChildren(this);
        }

        private void foo(Point2D.Float pos) {
            x1 = Math.min(x1, (int) pos.x);
            y1 = Math.min(y1, (int) pos.y);
            x2 = Math.max(x2, (int) pos.x);
            y2 = Math.max(y2, (int) pos.y);
        }

        Dimension getDimension() {
            assert x1 >= 0;
            assert y1 >= 0;
            return new Dimension(x2, y2);
        }
    }
}

// End Chart.java
