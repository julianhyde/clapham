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
import java.io.File;
import java.io.IOException;

import net.hydromatic.clapham.graph.Node;

/**
 * <p>The main class for drawing. This class contains all the basic drawing
 * functions to generate railroad diagrams.</p>
 * 
 * <p>Usage</p>
 * <pre>
 * Chart chart = ......//a concrete implementation
 * 
 * ChartOptions options = chart.createOptions();//Create the default options for this chart
 * options.withOptimize(false); //change the optimize feature
 * 
 * chart.draw("rule");
 * </pre>
 * @author Edgar Espina
 * @version $Id: $
 * @see Java2DChart
 */
public interface Chart {

	public enum ArrowDirection {
		LEFT, RIGHT, UP, DOWN
	}

	/**
	 * 
	 * This interface allow to traverse all the nodes
	 * 
	 */
	interface NodeVisitor {
		void visit(Node node);
	}

	/**
	 * Create the chart options with default values
	 * 
	 * @return
	 */
	ChartOptions createOptions();

	/**
	 * Create the chart options for the specific font
	 * 
	 * @param fontName
	 * @return
	 */
	ChartOptions createOptions(String fontName);

	/**
	 * The options for this chart
	 * 
	 * @return
	 */
	ChartOptions getOptions();

	/**
	 * Set the options for this chart
	 * 
	 * @param options
	 */
	void setOptions(ChartOptions options);

	void drawString(String text, int x, int y);

	void drawRectangle(int x, int y, int width, int height);

	void drawArcCorner(int x, int y, int arcSize, int angle);

	void drawLine(int x1, int y1, int x2, int y2);

	void drawArrow(int x1, int y1, int x2, int y2, ArrowDirection right);

	void drawArcCorner(int x, int y, int arcSize);

	void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle);

	/**
	 * Initialize and check all the resources before drawing
	 */
	void prepare();

	/**
	 * Returns the size of the graph that corresponds to the given symbolName
	 * 
	 * @param symbolName
	 * @return The size of the graph that corresponds to the given symbolName
	 */
	Dimension size(String symbolName);

	/**
	 * Draw the diagram in the underlying graphic object
	 * 
	 * @param symbolName
	 */
	void draw(String symbolName);

	/**
	 * Draw the diagram in the underlying graphic object and save as an image (a
	 * PNG image)
	 * 
	 * @param symbolName
	 * @param outputDirectory
	 *            The output directory
	 * @throws IOException
	 */
	void drawAndExport(String symbolName, File outputDirectory)
			throws IOException;

	int fontHeightCorrectness();
}

// End Chart.java
