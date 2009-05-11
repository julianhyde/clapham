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

import java.awt.*;

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
    private final Font font;
    private final Color color;
    final int width;
    final int height;

    TextBox(Chart chart, String text, Font font, Color color) {
        this.chart = chart;
        this.text = text;
        this.font = font;
        this.color = color;
        this.width = chart.getStringWidth(font, text);
        this.height = chart.getFontHeight();

    }

    void drawAtCenter(float x1, float y1, float width, float height) {
        float x = x1 + width / 2f;
        x -= this.width / 2f;
        float y = y1 + height / 2f;
        y += this.height / 2f;
        chart.drawString(text, font, color, x, y);
    }
}

// End TextBox.java
