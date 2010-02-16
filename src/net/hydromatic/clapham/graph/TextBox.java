/*
// $Id$
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
// Copyright (c) 2005 Stefan Schoergenhumer, Markus Dopler
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

import java.awt.Color;
import java.awt.Font;

import net.hydromatic.clapham.chart.Chart;

/**
 * TODO:
 * 
 * @author jhyde
 * @version $Id$
 * @since Sep 1, 2008
 */
public class TextBox {
	private final Chart chart;
	private final String text;
	final int width;
	final int height;

	TextBox(Chart chart, String text, Font font, Color color) {
		this.chart = chart;
		this.text = text;
		this.width = chart.getStringWidth(text);
		this.height = chart.getFontHeight();

	}

	void drawAtCenter(int x1, int y1, int width, int height) {
		int x = x1 + width / 2;
		x -= this.width / 2;
		int y = y1 + height / 2;
		y += this.height / 2;
		chart.drawString(text, x, y);
	}
}

// End TextBox.java
