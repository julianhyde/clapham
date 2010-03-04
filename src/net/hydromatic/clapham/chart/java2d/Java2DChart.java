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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;

import net.hydromatic.clapham.chart.AbstractChart;
import net.hydromatic.clapham.chart.AbstractChartOptions;
import net.hydromatic.clapham.chart.Chart;
import net.hydromatic.clapham.chart.MutableChartOptions;
import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.Size;
import net.hydromatic.clapham.graph.Symbol;

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

	private float xMin = Integer.MAX_VALUE;
	private float yMin = Integer.MAX_VALUE;
	private float xMax = Integer.MIN_VALUE;
	private float yMax = Integer.MIN_VALUE;

	/**
	 * the graphics object from the EBNFForm on which the drawing takes place
	 */
	protected final Graphics2D g;

	public Java2DChart(Grammar grammar, Graphics2D graphics) {
		super(grammar);
		this.g = graphics;
	}

	public Dimension getDimension() {
		assert xMin >= 0;
		assert yMin >= 0;
		return new Dimension((int) xMax + 10, (int) yMax + 10);
	}

	private MutableChartOptions createOptions(Font font) {
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

	public MutableChartOptions createOptions(String font) {
		return createOptions(Font.decode(font));
	}
	
	public MutableChartOptions createOptions() {
		return createOptions(defaultCharFont);
	}
	public void drawString(String text, int x, int y) {
		g.drawString(text, x, y);
	}
	
	void setComponentGapHeight(int value) {
		int componentGapHeight = value;
		final int fontHeight = fontHeight();
		int componentArcSize = getOptions().arcSize();
		if (componentGapHeight / 2 + fontHeight / 2 < componentArcSize) {
			componentArcSize = (componentGapHeight + fontHeight) / 2;
		}

		if (componentArcSize % 2 != 0) {
			componentArcSize -= 1;
		}
		getOptions()
				.withArcSize(componentArcSize)
				.withComponentGapHeight(componentGapHeight);
	}

	public Size getSymbolSize() {
		return symbolSize;
	}

	public void draw(String symbolName) {
		Symbol s = grammar.symbolMap.get(symbolName);

		if (s == null) {
			return;
		}
		symbolSize = new Size(s.graph.graphSize.getWidth() + initialX()
				+ componentGapWidth() * 2, s.graph.graphSize.getHeight()
				+ initialY() + componentGapHeight() * 2 + 5);

		// EbnfForm.Drawarea = new Bitmap(
		// Node.getSymbolSize().getWidth(),
		// Node.getSymbolSize().getHeight(),
		// System.Drawing.Imaging.PixelFormat.Format24bppRgb);

		// decide either draw on visualized bitmap or record a metafile
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) symbolSize.getWidth(), (int) symbolSize
				.getHeight());
		g.setColor(Color.BLACK);
		drawString(s.name, initialX() - 20, initialY() - 30);
		// g.DrawRectangle(
		// new Pen(Color.Orange, 2),
		// p.X,
		// p.Y + 30,
		// s.graph.graphSize.getWidth(),
		// s.graph.graphSize.getHeight());
		g.setStroke(lineStroke);
		g.setColor(lineColor);
		g.drawLine(initialX() - componentGapWidth() / 4 - arcSize() / 2,
				(int) s.graph.l.posLine.y, initialX(), (int) s.graph.l.posLine.y);
		Point p = new Point(initialX(), initialY() - 30);
		s.graph.l.drawComponents(this, p, s.graph.graphSize);
		// final SizeMapper sizeMapper = new SizeMapper();
		// s.graph.l.accept(sizeMapper);
		// s.graph.r.accept(sizeMapper);
	}

	public void drawAndExport(String symbolName, File output)
			throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public int getHeight() {
		return getDimension().height;
	}
	
	public int getWidth() {
		return getDimension().width;
	}
	
	// draws arrows for different directions
	public void drawArrow(int x1, int y1, int x2, int y2,
			Chart.Direction direction) {
		expandBounds(x1, y1);
		expandBounds(x2, y2);
		g.setColor(lineColor);
		g.setStroke(lineStroke);
		g.drawLine(x1, y1, x2, y2);
		switch (direction) {
		case RIGHT:
			g.fillPolygon(new int[] { x2, x2 - arrowSize() * 2,
					x2 - arrowSize() * 2 }, new int[] { y2, y2 - arrowSize(),
					y2 + arrowSize() }, 3);
			break;
		case UP:
			g
					.fillPolygon(
							new int[] { x2, x2 - arrowSize(), x2 + arrowSize() },
							new int[] { y2, y2 + arrowSize() * 2,
									y2 + arrowSize() * 2 }, 3);
			break;
		case LEFT:
			g.fillPolygon(new int[] { x2, x2 + arrowSize() * 2,
					x2 + arrowSize() * 2 }, new int[] { y2, y2 + arrowSize(),
					y2 - arrowSize() }, 3);
			break;
		case DOWN:
			g
					.fillPolygon(
							new int[] { x2, x2 - arrowSize(), x2 + arrowSize() },
							new int[] { y2, y2 - arrowSize() * 2,
									y2 - arrowSize() * 2 }, 3);
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

	public void drawArc(int x, int y, int width, int height, int startAngleF,
			int arcAngle) {
		expandBounds(x - width, y - height);
		expandBounds(x + width, y - height);
		expandBounds(x - width, y + height);
		expandBounds(x + width, y + height);
		int startAngle = (int) startAngleF;
		// g.setStroke(stroke);
		// g.setColor(color);
		g.drawArc((int) x, (int) y, (int) width, (int) height,
				startAngle == 180 ? 90 : startAngle == 90 ? 180
						: startAngle == 270 ? 0 : startAngle == 0 ? 270
								: startAngle, (int) arcAngle);
	}

	public void drawArcCorner(int x, int y, int arcSize, int startAngle) {
		drawArc(x, y, arcSize, arcSize, startAngle, 90);
	}

	public void drawArcCorner(int x, int y, int startAngle) {
		drawArc(x, y, arcSize(), arcSize(), startAngle, 90);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		expandBounds(x1, y1);
		expandBounds(x2, y2);
		g.setColor(lineColor);
		g.setStroke(lineStroke);
		g.drawLine(x1, y1, x2, y2);
	}

	public void drawRectangle(int x, int y, int width, int height) {
		expandBounds(x, y);
		expandBounds(x + width, y + height);
		g.drawRect((int) x, (int) y, (int) width, (int) height);
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

		private void foo(Point pos) {
			x1 = Math.min(x1, pos.x);
			y1 = Math.min(y1, pos.y);
			x2 = Math.max(x2, pos.x);
			y2 = Math.max(y2, pos.y);
		}

		Dimension getDimension() {
			assert x1 >= 0;
			assert y1 >= 0;
			return new Dimension(x2, y2);
		}
	}

	public int fontHeightCorrectness() {
		return symbolGapHeight() * 3;
	}
}

// End Java2DChart.java
