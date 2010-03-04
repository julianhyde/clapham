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

import java.io.File;
import java.io.IOException;

import net.hydromatic.clapham.graph.Node;

/**
 * TODO:
 * 
 * @author Edgar Espina
 * @version $Id: $
 */
public interface Chart {

	public enum Direction {
		LEFT, RIGHT, UP, DOWN
	}

	interface NodeVisitor {
		void visit(Node node);
	}

	MutableChartOptions createOptions();
	
	MutableChartOptions createOptions(String fontName);
	
	MutableChartOptions getOptions();
	
	void setOptions(MutableChartOptions options);
	
	void drawString(String text, int x, int y);

	void drawRectangle(int x, int y, int width, int height);

	void drawArcCorner(int x, int y, int arcSize, int angle);

	void drawLine(int x1, int y1, int x2, int y2);

	void drawArrow(int x1, int y1, int x2, int y2, Direction right);

	void drawArcCorner(int x, int y, int arcSize);	

	void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle);

	void calcDrawing();

	void draw(String symbolName);
	
	void drawAndExport(String symbolName, File output) throws IOException;

	int fontHeightCorrectness();
	
	int getWidth();
	
	int getHeight();
}

// End Chart.java
