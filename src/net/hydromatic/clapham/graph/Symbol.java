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
public class Symbol {

    public final NodeType typ;         // t, nt
	public final String   name;        // symbol name
	public Graph    graph;       // nt: to first node of syntax graph

	public Symbol(NodeType typ, String name) {
		if (name.length() == 0) {
			System.out.println("empty token not allowed");
            name = "???";
		}
		this.typ = typ;
        this.name = name;
	}
}

// End Symbol.java
