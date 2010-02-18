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

import java.util.*;
import java.util.List;
import java.io.PrintStream;

/**
 * TODO:
*
* @author jhyde
* @version $Id$
* @since Aug 26, 2008
*/
public class Grammar {
    public final Map<String,Symbol> symbolMap = new HashMap<String, Symbol>();
    public final Map<Symbol, Graph> ruleMap = new HashMap<Symbol, Graph>();
    final List<Node> nodes = new ArrayList<Node>();

    public static boolean TRACE = false;

    // enable optimizations?
    private boolean optimizeGraph = true;

    public final List<Symbol> terminals = new ArrayList<Symbol>();
    public final List<Symbol> nonterminals = new ArrayList<Symbol>();

    private static int ptr(Node p, boolean up) {
        if (p == null) {
            return 0;
        } else if (up) {
            return -p.n;
        } else {
            return p.n;
        }
    }

    public void setOptimizeGraph(boolean value) {
        this.optimizeGraph = value;
    }

    public boolean setOptimizeGraph() {
        return optimizeGraph;
    }

    boolean compare(Node n1, Node n2) {
        if (n1.typ == n2.typ) {
            if (n1.typ == NodeType.NONTERM || n1.typ == NodeType.TERM) {
                if (!n1.sym.name.equals(n2.sym.name)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    boolean deepCompare(Node n1, Node n2, boolean untilIter) {
        boolean samelevel = true;
        Node identifier = n2; // helps to identify the relevant iter node
        while (n1 != null && samelevel) {
            //just compare nodes until the iter node
            if (untilIter) {
                if (n1.typ == NodeType.ITER && n1.sub == identifier) {
                    if (n1 == n2) { //last iter node's next points to the iter
                        if (TRACE) {
                            System.out.println(
                                    "true: iter node reached, graphs match");
                        }
                        return true;
                    } else {
                        if (TRACE) {
                            System.out.println(
                                "false: iter node reached, graphs do not match");
                        }
                        return false;
                    }
                }
            }
            if (n2 == null) {
                if (TRACE) {
                    System.out.println(
                        "false: second enclosing substructure ended before first");
                }
                return false;
            }
            if (!compare(n1, n2)) {
                if (TRACE) {
                    System.out.println("false: node not same type/content");
                }
                return false;
            }
            //--> t,nt,eps is ok, go to next

            if (n1.typ == NodeType.OPT
                || n1.typ == NodeType.ITER
                || n1.typ == NodeType.RERUN) {
                if (!deepCompare(n1.sub, n2.sub, false)) {
                    if (TRACE) {
                        System.out
                            .println(
                                "false: false in subelem of iter,opt or rerun");
                    }
                    return false;
                }
                if (n1.typ == NodeType.RERUN
                    && !deepCompare(n1.itergraph, n2.itergraph, false)) {
                    if (TRACE) {
                        System.out.println(
                            "false: itergraph of rerun doesn't match");
                    }
                    return false;
                }
            } else if (n1.typ == NodeType.ALT) {
                Node a1 = n1;
                Node a2 = n2;
                while (a1 != null) {
                    if (a2 == null) {
                        if (TRACE) {
                            System.out
                                .println(
                                    "false: false in subalt, second node null");
                        }
                        return false;
                    }

                    if (!deepCompare(a1.sub, a2.sub, false)) {
                        if (TRACE) {
                            System.out
                                .println("false: false in subelem of subalt");
                        }
                        return false;
                    }
                    a1 = a1.down;
                    a2 = a2.down;
                }
                if (a2 != null) {
                    if (TRACE) {
                        System.out
                            .println("false: second alt has more alternatives");
                    }
                    return false;
                }
            }
            if (n1.up) {
                if (!n2.up) {
                    if (TRACE) {
                        System.out.println(
                            "false: second has not finished enclosing structure");
                    }
                    return false;
                }
                samelevel = false;
            }
            n1 = n1.next;
            n2 = n2.next;
        }
        if (n1 == null && n2 != null) {
            if (TRACE) {
                System.out.println(
                    "false: first enclosing substructure ended before second");
            }
            return false;
        }
        return true;
    }

    /** calls all methods which optimize the graphs */
    public void optimize() {
        for (Symbol s : nonterminals) {
            removeWrongLinebreaks(s.graph.l, null, s);
            if (optimizeGraph) {
                //remove redundant iter/opts
                removeRedundancy(s.graph.l, null, s);
            }
            if (optimizeGraph) {
                //remove eps nodes and redundant eps nodes in alternatives
                removeEps(s.graph.l, null, s);
            }
            if (optimizeGraph) {
                optimizeIter(s.graph.l, null, s);
            }
        }
    }

    /**
     * Removes all unnecessary and wrong linebreaks (wrap-nodes) from the
     * graph.
     */
    void removeWrongLinebreaks(Node n, Node parent, Symbol s) {
        boolean samelevel = true;
        Node i = n;
        while (i != null && samelevel) {
            if (i.typ == NodeType.WRAP) {
                //if in outer structure, just remove multiple wraps
                if (parent == null) {
                    while (i.next != null && i.next.typ == NodeType.WRAP) {
                        i.next = i.next.next;
                    }
                } //if in inner structure remove it
                else {
                    //if \n is first element of substructure
                    if (n == i) {
                        //parent==null doesn't occur

                        //if \n is the only subelement
                        if (i.up || i.next == null) {
                            Node eps = new Node(this, NodeType.EPS, null);
                            parent.sub = eps;
                            eps.up = i.up;
                            eps.next = i.next;
                            n = eps;
                        } else {
                            parent.sub = i.next;
                            n = parent.sub;
                        }
                    } else { //if within substructure
                        Node j = n;
                        while (j.next != i) {
                            j = j.next;
                        }
                        j.next = i.next;
                        j.up = i.up;
                    }
                }
            } else if (i.typ == NodeType.OPT
                || i.typ == NodeType.ITER
                || i.typ == NodeType
                .RERUN) {
                removeWrongLinebreaks(i.sub, i, s);
            } else if (i.typ == NodeType.ALT) {
                Node a = i;
                while (a != null) {
                    removeWrongLinebreaks(a.sub, a, s);
                    a = a.down;
                }
            }

            if (i.up) {
                samelevel = false;
            }
            i = i.next;
        }
    }

    void removeRedundancy(Node n, Node parent, Symbol s) {
        boolean samelevel = true;        //next node in same level?
        Node begin = n;
        while (n != null && samelevel) {
            if (n.typ == NodeType.ALT) {
                Node a = n;
                while (a != null) {
                    removeRedundancy(a.sub, a, s);
                    a = a.down;
                }
            } else if (n.typ == NodeType.ITER) {
                while ((n.sub.typ == NodeType.ITER
                    || n.sub.typ == NodeType.OPT) && n.sub.up) {
                    //EbnfForm.WriteLine("Rendundant "+Node.nTyp[n.sub.typ]+" Node removed (iter).");
                    n.sub = n.sub.sub;
                    Node i = n.sub;
                    while (!i.up) {
                        i = i.next;
                    }
                    i.next = n;
                }
                removeRedundancy(n.sub, n, s);
            } else if (n.typ == NodeType.OPT) {
                boolean containsIter = false;
                while ((n.sub.typ == NodeType.OPT
                    && (n.sub.up || n.sub.next == null))
                    || (n.sub.typ == NodeType.ITER
                    && (n.sub.up || n.sub.next == null))) {
                    //if(n.sub.typ==Node.opt || containsIter) EbnfForm.WriteLine("Rendundant "+Node.nTyp[n.sub.typ]+" Node removed (opt).");
                    if (n.sub.typ == NodeType.ITER) {
                        containsIter = true;
                    }
                    n.sub = n.sub.sub;
                }
                if (containsIter) {
                    Node iter = new Node(this, NodeType.ITER, n.sub);
                    iter.next = n.next;
                    if (n == begin) {
                        if (parent == null) {
                            s.graph.l = iter;
                        } else {
                            parent.sub = iter;
                        }
                    } else {
                        Node j = begin;
                        while (j.next != n) {
                            j = j.next;
                        }
                        j.next = iter;
                    }
                    n = iter;

                    //set correct next pointer of last subelement of new iter
                    Node i = iter.sub;
                    while (i.next != null && !i.up) {
                        i = i.next;
                    }
                    i.next = iter;
                }
                removeRedundancy(n.sub, n, s);
            }
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
    }

    /**
     * Removes all empty ('epsilon') iter/opt nodes in alternatives, as well as
     * multiple epsilon nodes at the beginning.
     */
    void removeEps(Node n, Node parent, Symbol s) {
        boolean samelevel = true;        //next node in same level?
        Node begin = n;
        while (n != null && samelevel) {

            if (n.typ == NodeType.EPS) {
                if (n == begin) {
                    if (parent == null) {
                        //if the graph only consists of an eps, let it live
                        if (n.next != null) {
                            s.graph.l = n.next;
                            begin = n.next;
                        }
                    } //else: at beginning of substructure not required (iter/opt/alt subnodes were already handled)
                } else {
                    Node i = begin;
                    while (i.next != n) {
                        i = i.next;
                    }
                    i.next = n.next;
                    i.up = n.up;
                }
            } else if (n.typ == NodeType.ITER || n.typ == NodeType.OPT) {
                if (n.sub.typ == NodeType.EPS
                    && (n.sub.next == null
                    || n.sub.up)) {
                    if (n == begin) {
                        if (parent == null) { //beginning of graph
                            //if graph only consists of this iter/opt, then replace it with an eps node
                            if (n.next == null) {
                                Node eps = new Node(this, NodeType.EPS, null);
                                s.graph.l = eps;
                                s.graph.r = eps;
                            } else { //remove that node
                                s.graph.l = n.next;
                                begin = n.next;
                            }
                        } //else: at beginning of substructure not required (iter/opt/alt subnodes were already handled)
                    } else { //within substructure
                        Node i = begin;
                        while (i.next != n) {
                            i = i.next;
                        }
                        if (n.up) {
                            i.up = true;
                        }
                        i.next = n.next;
                    }
                } else {
                    removeEps(n.sub, n, s);
                }
            } else if (n.typ == NodeType.ALT) {
                Node a = n;
                //count number of eps
                int numOfEps = 0;
                while (a != null) {
                    //checkSubAlts(a);
                    if (a.sub.typ == NodeType.EPS
                        && (a.sub.next == null
                        || a.sub.up)) {
                        numOfEps++;
                    }
                    a = a.down;
                }
                a = n;
                while (numOfEps > 1) {
                    if (n != a && a.sub.typ == NodeType.EPS && (a.sub
                        .next
                        == null || a.sub.up)) {
                        Node i = n;
                        while (i.down != a) {
                            i = i.down;
                        }
                        i.down = a.down;
                        numOfEps--;
                    }
                    a = a.down;
                }
                removeSameAlts(n);
                putEpsAtBeginningOfAlt(begin, n, parent, s);
                //optimize subcomponents
                a = n;
                while (a != null) {
                    //if not the left eps node
                    if (!(a.sub.typ == NodeType.EPS
                        && (a.sub.next == null
                        || a.sub.up))) {
                        removeEps(a.sub, a, s);
                    }
                    a = a.down;
                }
            }
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
    }

    //they would bug a condition in removeEps
    public void checkSubAlts(Node alt)	{

        //remove all empty iter/opts
        //make sure, that at least one eps Node will exist
        Node eps = new Node(this, NodeType.EPS, null);
        eps.next = alt.sub;
        alt.sub = eps;
        Node i = alt.sub;
        boolean samelevel = true;
        while (i != null && samelevel) {
            //if empty iter/opt
            if ((i.typ == NodeType.ITER
                || i.typ == NodeType.OPT)
                && i.sub.typ == NodeType.EPS
                && (i.sub.next == null
                || i.sub.up)) {
                //case i==alt.sub not possible
                Node a = alt.sub;
                while (a.next != i) {
                    a = a.next;
                }
                a.next = i.next;
            }
            if (i.up) {
                samelevel = false;
            }
            i = i.next;
        }

        i = alt.sub;
        //remove multiple eps nodes at the beginning
        if (i.typ == NodeType.EPS) {
            while (i.next != null
                && !i.next.up
                && i.next.typ == NodeType.EPS) {
                i.next = i.next.next;
            }
        }
    }

    void removeSameAlts(Node alt)	{
        Node a = alt;
        while (a != null) {
            Node i = a.down;
            while (i != null) {
                if (deepCompare(a.sub, i.sub, false)) {
                    Node n = a;
                    while (n.down != i) {
                        n = n.down;
                    }
                    n.down = i.down;
                }
                i = i.down;
            }
            a = a.down;
        }
    }

    void putEpsAtBeginningOfAlt(Node n, Node alt, Node parent, Symbol s) {
        Node a = alt;
        boolean containsEps = false;

        //determine if eps is contained
        while (a != null) {
            //if eps node
            if (a.sub.typ == NodeType.EPS &&
                (a.sub.next == null
                || a.sub.up)) {
                containsEps = true;
            }
            a = a.down;
        }
        if (containsEps) {
            //remove eps node
            a = alt;
            while (a != null) {
                //if eps node
                if (a.sub.typ == NodeType.EPS && (a.sub.next == null
                    || a.sub
                    .up)) {
                    //remove eps only if within alternatives
                    if (a != alt) {
                        Node i = alt;
                        while (i.down != a) {
                            i = i.down;
                        }
                        i.down = a.down;
                    }
                    break; //there can be only one eps in the alts because same nodes have already been removed
                }
                a = a.down;
            }
            //insert eps, if first alt isn't eps

            if (!(alt.sub.typ == NodeType.EPS
                && (alt.sub.next == null || alt.sub.up))) {
                Node eps = new Node(this, NodeType.EPS, null);
                eps.next = alt.next;
                eps.up = true;
                AltNode a1 = new AltNode(this, eps);
                a1.down = alt;
                if (alt == n) {
                    if (parent == null) {
                        s.graph.l = a1;
                    } else {
                        parent.sub = a1;
                    }
                } else {
                    Node i = n;
                    while (i.next != alt) {
                        i = i.next;
                    }
                    i.next = a1;
                }
                a1.next = alt.next;
                a1.up = alt.up;
                alt.next = null;
            }
        }
    }

    //optimizes enclosing structures and recursively its substructures
    void optimizeIter(Node n, Node parent, Symbol s) {
        boolean samelevel = true;        //next node in same level?
        Node i = n;

        while (i != null && samelevel) {
            if (i.typ == NodeType.OPT) {
                optimizeIter(i.sub, i, s);
            } else if (i.typ == NodeType.ALT) {
                Node a = i;
                while (a != null) {
                    optimizeIter(a.sub, a, s);
                    a = a.down;
                }
            } else if (i.typ == NodeType.ITER) {
                //first optimize the iter substructure
                optimizeIter(i.sub, i, s);

                //while loop to deepCompare from every node until the iter node
                Node j = n;
                boolean matchFound = false;
                while (j != i && !matchFound) {
                    Node k = i.sub;
                    boolean samelevel2 = true;
                    while (k != null && samelevel2 && !matchFound) {
                        if (deepCompare(j, k, true)) {
                            //EbnfForm.WriteLine("Iter node optimized.");
                            matchFound = true;
                            //replace the iter node and the nodes before by the rerun node
                            Node re = new Node(this, NodeType.RERUN, k);
                            if (j == n) {

                                if (parent == null) {
                                    s.graph.l = re;
                                    n = re;
                                } else {
                                    parent.sub = re;
                                    n = re;
                                }
                            } else {
                                Node l = n;
                                while (l.next != j) {
                                    l = l.next;
                                }
                                l.next = re;
                            }

                            //if a {b a} isolate b
                            if (k != i.sub) {
                                re.itergraph = i.sub;
                                Node temp = re.itergraph;
                                while (temp.next != k) {
                                    temp = temp.next;
                                }
                                temp.next = null;
                            }

                            re.next = i.next;
                            re.up = i.up;
                            i = re;
                        }
                        if (k.up) {
                            samelevel2 = false;
                        }
                        k = k.next;
                    }

                    j = j.next;
                }
            }
            if (i.up) {
                samelevel = false;
            }
            i = i.next;
        }
    }

    public void makeEpsilon(Graph g) {
        g.l = new Node(this, NodeType.EPS, null);
        g.r = g.l;
    }

    public void makeFirstAlt(Graph g) {
        g.l = new AltNode(this, g.l);
//        g.l.next = g.r;
        g.r = g.l;
    }

    public void makeAlternative(Graph g1, Graph g2) {
        g2.l = new AltNode(this, g2.l);
        Node p = g1.l;
        while (p.down != null) {
            p = p.down;
        }
        p.down = g2.l;
        p = g1.r;
        while (p.next != null) {
            p = p.next;
        }
//        p.next = g2.r;
    }

    public void makeSequence(Graph g1, Graph g2) {
        if (g1.l == null && g1.r == null) {/*case: g1 is empty */
            g1.l = g2.l;
            g1.r = g2.r;
        } else {
            Node p = g1.r.next;
            g1.r.next = g2.l; // link head node
            while (p != null) {  // link substructure
                Node q = p.next;
                p.next = g2.l;
                p.up = true;
                p = q;
            }
            g1.r = g2.r;
        }
    }

    public void makeIteration(Graph g) {
        g.l = new Node(this, NodeType.ITER, g.l);
        Node p = g.r;
        g.r = g.l;
        while (p != null) {
            Node q = p.next;
            p.next = g.l;
            p.up = true;
            p = q;
        }
    }

    public void makeOption(Graph g) {
        g.l = new Node(this, NodeType.OPT, g.l);
//        g.l.next = g.r;
        g.r = g.l;
    }

    /**
     * Finds a terminal or non-terminal with a given name.
     *
     * @param name Name of symbol
     * @return terminal or non-terminal
     */
    public Node find(String name) {
        for (Node n : nodes) {
            if (n.sym != null && n.sym.name.equals(name)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Converts the terminal with a given name to a non-terminal.
     *
     * @param name Name of non-terminal.
     */
    public void terminalToNt(String name) {
        for (Node n : nodes) {
            if (n.sym != null && n.sym.name.equals(name)) {
                n.typ = NodeType.NONTERM;
            }
        }
        for (Symbol s : terminals) {
            if (s.name.equals(name)) {
                nonterminals.add(s);
                terminals.remove(s);
                break;
            }
        }
    }

    public void printNodes(PrintStream out) {
        out.println("Graph nodes:");
        out.println("(S...Starting nodes)");
        out.println("--------------------------------------------");
        out.println("S   n type name          next  down   sub   ");
        out.println("--------------------------------------------");

        for (Node p : nodes) {
            boolean nt = false;
            for (Symbol s : nonterminals) {
                if (s.graph.l.n == p.n) {
                    out.print("*");
                    nt = true;
                }
            }
            if (!nt) {
                out.print(" ");
            }

            out.format("%1$-4s %2$-4s ", p.n, p.typ.name());

            if (p.sym != null) {
                out.format("%1$-12s ", p.sym.name);
            } else {
                out.print("             ");
            }

            out.format("%1$5s ", Grammar.ptr(p.next, p.up));

            switch (p.typ) {
            case ALT:
            case ITER:
            case OPT:
            case RERUN:
                out.format(
                    "%1$5d %2$5d",
                    Grammar.ptr(p.down, false),
                    Grammar.ptr(p.sub, false));
                break;
            case EPS:
                out.print("           ");
                break;
            }
            out.println();
        }
        out.println();

        for (Symbol symbol : symbolMap.values()) {
            final StringBuffer buf = new StringBuffer();
            out.println(symbol.name + " ::=");
            symbol.graph.l.unparse(buf);
            out.println("  " + buf);
        }
        out.println();
    }

    // starts to draw the rule at the given symbol s

    /*
	 * compare two graphs on the basis of structure and value
     * if untilIter is set, n1 and n2 are treated in a different way:
     * the graph n1 to the iter node is compared to the iter subnode
     * params: n1 must be the node before iter if untilIter==true
     * params: n2 must be the first subnode of iter if untilIter==true
     */
}

// End Grammar.java
