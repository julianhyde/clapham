/*
// $Id$
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
package net.hydromatic.clapham.parser;

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Graph;
import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Symbol;

/**
 * TODO:
*
* @author jhyde
* @version $Id$
* @since Jul 30, 2008
*/
public class LiteralNode extends BaseEbnfNode {
    public final String s;

    public LiteralNode(String s) {
        this.s = s;
    }
    
    public Graph toGraph(Grammar grammar) {
		Symbol symbol = new Symbol(NodeType.TERM, s);
		grammar.terminals.add(symbol);
		// grammar.symbolMap.put(symbol.name, symbol);
		Graph graph = new Graph(new Node(grammar, symbol));
		return graph;
	}

    public void toString(StringBuilder buf) {
        buf.append('"').append(s).append('"');
    }
}

// End LiteralNode.java
