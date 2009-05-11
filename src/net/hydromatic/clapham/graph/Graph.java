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

/**
 * TODO:
 *
 * @author jhyde
 * @version $Id: $
 * @since Jul 30, 2008
 */
public class Graph {

    public Node l;    // left end of graph = head
    public Node r;    // right end of graph = list of nodes to be linked to successor graph
    public Size graphSize;

    public Graph() {
        l = null;
        r = null;
    }

    public Graph(Node left, Node right) {
        l = left;
        r = right;
    }

    public Graph(Node p) {
        l = p;
        r = p;
    }

    public void finish(Graph g) {
        Node p = g.r;
        while (p != null) {
            Node q = p.next;
            p.next = null;
            p = q;
        }
    }
}

// End Graph.java
