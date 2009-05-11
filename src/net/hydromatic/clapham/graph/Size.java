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

/**
 * TODO:
 *
 * @author jhyde
 * @version $Id$
 * @since Jul 30, 2008
 */
public class Size {
    private float width;
    private float height;

    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Size() {
        this(0, 0);
    }

    public String toString() {
        return width + "x" + height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void maxHeight(float height) {
        if (height > this.height) {
            this.height = height;
        }
    }

    public void maxWidth(float width) {
        if (width > this.width) {
            this.width = width;
        }
    }

    public void incWidth(float width) {
        this.width += width;
    }

    public void incHeight(float height) {
        this.height += height;
    }
}

// End Size.java
