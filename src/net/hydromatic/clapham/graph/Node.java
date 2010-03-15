/*
// $Id$
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
package net.hydromatic.clapham.graph;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.hydromatic.clapham.chart.Chart;
import net.hydromatic.clapham.chart.ChartOptions;
import net.hydromatic.clapham.chart.ChartOptions.ChartLayout;

/**
 * TODO:
 *
 * @author jhyde
 * @version $Id$
 * @since Jul 30, 2008
 */
public class Node {
    
    public final int n; // node number
    public NodeType typ; // t, nt, eps, alt, iter, opt, rerun
    public Node next; // to successor node
    public Node down; // alt: to next alternative
    public Node sub; // alt, iter, opt: to first node of substructure
    public boolean up; // true: "next" leads to successor in enclosing structure
    public Symbol sym; // nt, t: symbol represented by this node
    public Node itergraph; // rerun: points to the b in "a {b a}", null if
    // "a {a}"
    private boolean firstLevel; // true if the Node is in the first Level
    private TextBox textBox;
    private boolean optimized;

    public Node(Grammar grammar, Symbol sym) {
        this.typ = sym.typ;
        this.sym = sym;
        n = grammar.nodes.size();
        grammar.nodes.add(this);
    }

    public Node(Grammar grammar, NodeType typ, Node sub) {
        this.typ = typ;
        n = grammar.nodes.size();
        grammar.nodes.add(this);
        this.sub = sub;
    }

    public Node(Grammar grammar, NodeType typ, Node sub, boolean optimized) {
        this(grammar, typ, sub);
        this.optimized = optimized;
    }

    public boolean isOptimized() {
        return optimized;
    }

    // ----------------- for printing ----------------------

    // ----------------- for drawing ----------------------

    /***************** other variables needed for the drawing ********/
    public Size size = new Size(0, 0); // the required size to draw the node
    private Size altSize = new Size(0, 0); // the required size to draw a
    // construct of alts or the size of
    // the firstcomponent in the special
    // rerun-node (itergraph!=null)
    private Size iterSize = new Size(0, 0); // the size of the second component
    // in the special rerun Node
    // (itergraph!=null)
    public final Point posBegin = new Point(0, 0); // the point in the left
    // above
    // corner of the component
    public final Point posLine = new Point(0, 0); // the point of the line of
    // the component
    public final Point posEnd = new Point(0, 0); // the point in the left down

    // corner

    // of the component

    public void unparse(StringBuffer buf) {
        switch (typ) {
        case EXCEPTION:
        case PREDICATE:
        case TERM:
        case NONTERM:
            buf.append('<').append(sym.name).append('>');
            break;
        case ALT:
            final List<Node> alts = new ArrayList<Node>();
            for (Node n = this; n != null; n = n.down) {
                alts.add(n.sub);
            }
            int count = 0;
            buf.append("(");
            for (Node alt : alts) {
                if (count++ > 0) {
                    buf.append(" | ");
                }
                unparseList(buf, nextChildren(alt), "", " ", "");
            }
            buf.append(")");
            break;
        case ITER:
            unparseList(buf, nextChildren(sub), "iter(", " ", ")");
            break;
        default:
            buf.append("unknown <").append(typ).append(">");
        }
    }

    private static List<Node> nextChildren(Node next) {
        final List<Node> list = new ArrayList<Node>();
        for (Node n = next; n != null; n = n.up ? null : n.next) {
            list.add(n);
        }
        return list;
    }

    private void unparseList(
        StringBuffer buf,
        List<Node> list,
        String before,
        String mid,
        String after)
    {
        int count = 0;
        buf.append(before);
        for (Node n : list) {
            if (count++ > 0) {
                buf.append(mid);
            }
            n.unparse(buf);
        }
        buf.append(after);
    }

    // calculates the size if there are wraps in the rule
    public void setWrapSize(ChartOptions options) {
        Node n = this;
        int maxH = 0;
        while (n != null) {
            n.firstLevel = true;
            switch (n.typ) {
            case WRAP:
                n.size.setHeight(maxH);
                maxH = 0;
                break;
            case ITER:
                if (maxH < n.size.getHeight()
                        + (options.fontHeight() + options.componentGapHeight())
                        / 2)
                {
                    maxH = n.size.getHeight()
                            + (options.fontHeight() + options
                                    .componentGapHeight()) / 2;
                }
                break;
            default:
                if (maxH < n.size.getHeight() || maxH < n.altSize.getHeight()) {
                    if (n.altSize.getHeight() != 0) {
                        maxH = n.altSize.getHeight();
                    } else {
                        maxH = n.size.getHeight();
                    }
                }
                break;
            }
            n = n.next;
        }
    }

    /**
     * Calculates the size of each symbol.
     */
    public Size calcSize(ChartOptions options) {
        Node n = this; // current node in the level
        Size s = new Size(); // alt,iter,opt: size of current construct
        int iterCompensation = 0;
        boolean samelevel = true; // next node in same level?
        int realHeight = n.calcHeight(options);
        Size maxTotalSize = new Size(0, 0);
        while (n != null && samelevel) {
            switch (n.typ) {
            case PREDICATE:
            case EXCEPTION:
            case TERM:
            case NONTERM:
                n.size.setHeight(
                    options.fontHeight() + options.symbolGapHeight() * 2
                    + options.componentGapHeight());
                n.size.setWidth(options.stringWidth(n.sym.name) + options.symbolGapWidth() * 2);
                if (n.typ.matches(NodeType.TERM, 
                        NodeType.EXCEPTION, 
                        NodeType.PREDICATE)) {
                    n.size.maxWidth(options.arcSize());
                }

                if (!n.up && n.next != null && n.next.typ == NodeType.WRAP
                        && n.next.size.getHeight() == 0)
                {
                    if (!n.next.up
                            && n.next.next != null
                            && (n.next.next.typ.matches(NodeType.TERM, 
                                                        NodeType.NONTERM, 
                                                        NodeType.EXCEPTION,
                                                        NodeType.PREDICATE)))
                    {
                        s.incWidth(options.componentGapWidth() / 2);
                    }
                }
                if (!n.up
                        && n.next != null
                        && (n.next.typ.matches(NodeType.TERM,
                                               NodeType.NONTERM,
                                               NodeType.EXCEPTION,
                                               NodeType.PREDICATE)))
                {
                    s.incWidth(options.componentGapWidth() / 2);
                }
                break;
            case EPS:
                n.size.setHeight(
                    options.fontHeight()
                    + options.componentGapHeight());
                n.size.setWidth(options.componentGapWidth());
                break;
            case OPT:
                n.size = n.sub.calcSize(options);
                n.size.incWidth(options.componentGapWidth() * 2);
                n.size.incHeight(
                    options.componentGapHeight() / 2
                    + options.componentGapHeight());
                break;
            case ITER:
                n.size = n.sub.calcSize(options);
                n.size.incWidth(options.componentGapWidth() * 2);
                n.size.incHeight(options.componentGapHeight() / 2);
                break;
            case WRAP:
                maxTotalSize.incHeight(
                    s.getHeight()
                    - options.componentGapHeight() / 2);
                maxTotalSize.maxWidth(s.getWidth());
                s.setHeight(0);
                s.setWidth(0);
                break;
            case RERUN:
                n.size = n.sub.calcSize(options);
                if (n.itergraph != null) {
                    n.altSize = n.size;
                    n.iterSize = n.itergraph.calcSize(options);
                    n.size.maxWidth(n.iterSize.getWidth());
                    n.size.incHeight(
                        options.fontHeight() * 3 / 2
                        + options.componentGapHeight());
                } 
                n.size.incHeight(options.componentGapHeight() / 2);
                
                n.size.incWidth(options.componentGapWidth() * 2);
                break;
            case ALT: {
                Node a = n;
                int maxH = -options.componentGapHeight();
                int maxW = 0;
                while (a != null) {
                    a.size = a.sub.calcSize(options);
                    maxH += a.size.getHeight();
                    if (a.size.getWidth() > maxW) {
                        maxW = a.size.getWidth();
                    }
                    a = a.down;
                }
                if (n.sub.typ == NodeType.ITER && realHeight != 0) {
                    maxH +=
                        (options.fontHeight() + options.componentGapHeight())
                        / 2;
                }
                maxW += 2 * options.componentGapWidth();
                maxH += options.componentGapHeight();

                n.altSize.setHeight(maxH);
                n.altSize.setWidth(maxW);
            }
                break;
            }
            if (n.typ == NodeType.ITER && realHeight != 0) {
                iterCompensation = (options.fontHeight() + options
                        .componentGapHeight()) / 2;
            }
            if (n.typ == NodeType.ALT) {
                s.maxHeight(n.altSize.getHeight());
                s.incWidth(n.altSize.getWidth());
            } else {
                s.maxHeight(n.size.getHeight());
                s.incWidth(n.size.getWidth());
            }
            if (n.typ == NodeType.ITER) {
                s.maxHeight(n.size.getHeight() + iterCompensation);
            }
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
        if (maxTotalSize.getWidth() != 0) {
            maxTotalSize.incHeight(
                s.getHeight() - options.componentGapHeight()
                / 2);
            maxTotalSize.maxWidth(s.getWidth());
            return maxTotalSize;
        } else {
            return s;
        }
    }

    /**
     * Calculates the total height of all symbols wich are in the same
     * horizontal level.
     */
    private int calcHeight(ChartOptions options) {
        Node n = this; // current node in the level
        int realHeight = 0;
        boolean samelevel = true; // next node in same level?
        while (n != null && samelevel) {
            int tmpHeight = 0;
            if (n.typ.matches(NodeType.TERM, NodeType.NONTERM, NodeType.EXCEPTION)) {
                tmpHeight = n.size.getHeight();
            } else if (n.typ == NodeType.ITER) {
                tmpHeight = n.sub.calcHeight(options);
            } else if (n.typ == NodeType.OPT) {
                tmpHeight = n.sub.calcHeight(options);
            } else if (n.typ == NodeType.PREDICATE) {
                tmpHeight = n.size.getHeight();
            } else if (n.typ == NodeType.RERUN) {
                tmpHeight = n.sub.calcHeight(options);
            } else if (n.typ == NodeType.ALT) {
                tmpHeight = n.sub.calcHeight(options);
            } else if (n.typ == NodeType.EPS) {
                tmpHeight = options.fontHeight() * 3 / 2;
                if (realHeight < tmpHeight) {
                    tmpHeight = options.fontHeight()
                            + options.componentGapHeight();
                } else {
                    tmpHeight = 0;
                }
            }
            realHeight = Math.max(realHeight, tmpHeight);
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
        return realHeight;
    }

    /**
     * Calculates the horizontal position of the symbols.
     */
    public void calcPos(ChartOptions options, int posBegin, boolean inverse) {
        Node n = this; // current node in the level
        int realHeight = calcHeight(options);
        boolean samelevel = true; // next node in same level?
        while (n != null && samelevel) {
            if (n.typ.matches(NodeType.NONTERM, NodeType.TERM, NodeType.EXCEPTION)) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posBegin.y = n.posLine.y
                        - (n.size.getHeight() - options.componentGapHeight()) / 2;
                n.posEnd.y = n.posLine.y
                        + (n.size.getHeight() - options.componentGapHeight()) / 2;
            } else if (n.typ == NodeType.EPS) {
                int offset =
                    ((inverse && n.up) || (n.n > 0 && n.isOptimized()))
                    ? options.symbolGapHeight()
                    : 0;
                n.posLine.y = posBegin + n.size.getHeight() / 2 + offset;
                n.posBegin.y = posBegin;
                n.posEnd.y = posBegin + n.size.getHeight();
            } else if (n.typ == NodeType.OPT) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posBegin.y = posBegin;
                n.posEnd.y = posBegin + n.size.getHeight();
                n.sub.calcPos(options, n.posBegin.y, inverse);
            } else if (n.typ == NodeType.PREDICATE) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posBegin.y = n.posLine.y - n.size.getHeight() / 2 
                            - options.componentGapHeight() / 2 +1;
                n.posEnd.y = n.posLine.y;
                
            } else if (n.typ == NodeType.RERUN) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posBegin.y = posBegin;
                n.posEnd.y = posBegin + n.size.getHeight();
                if (n.itergraph != null) {
                    n.itergraph.calcPos(
                        options,
                        posBegin + n.altSize.getHeight() / 2,
                        true);
                }
                n.sub.calcPos(options, n.posBegin.y, inverse);
            } else if (n.typ == NodeType.ITER) {
                if (realHeight == 0) {
                    n.posLine.y = posBegin + realHeight / 2;
                    n.posBegin.y = posBegin;
                    n.posEnd.y = posBegin + n.size.getHeight();
                } else {
                    n.posLine.y = posBegin + realHeight / 2;
                    n.posBegin.y = posBegin
                            + (options.fontHeight() + options
                                    .componentGapHeight()) / 2;
                    n.posEnd.y = n.posBegin.y + n.size.getHeight();
                }
                n.sub.calcPos(options, n.posLine.y, inverse);
            } else if (n.typ == NodeType.WRAP && firstLevel) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posEnd.y = posBegin + n.size.getHeight();
                posBegin = posBegin + n.size.getHeight();
            } else if (n.typ == NodeType.ALT) {
                n.posLine.y = posBegin + realHeight / 2;
                n.posBegin.y = posBegin;
                n.posEnd.y = posBegin + n.altSize.getHeight();

                n.sub.calcPos(options, posBegin, inverse);
                if (n.down != null) {
                    n.down.calcPos(
                        options,
                        posBegin + n.size.getHeight(),
                        inverse);
                }
            }
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
    }

    /**
     * Draws the components from left to right.
     *
     * <p>Each component paints itself and then give its coordinates to its
     * sub-components for a recursive call, or if applicable, a call to the
     * {@link #drawComponentsInverse} method.
     */
    public void drawComponents(Chart chart, Point p, Size s) {
        Node n = this; // current node in the level
        boolean samelevel = true; // next node in same level?

        ChartOptions options = chart.getOptions();
        while (n != null && samelevel) {
            switch (n.typ) {
            case EXCEPTION:
            case TERM:
            case NONTERM:
            case PREDICATE:
                drawDefault(chart, p, n);
                break;
            case EPS:
                drawEpsilon(chart, p, n);
                break;
            case OPT:
                drawOption(chart, p, n);
                break;
            case RERUN:
                drawRerun(chart, p, n);
                break;
            case ITER:
                drawIteration(chart, p, n);
                break;
            case WRAP:
                drawWrap(chart, p, n);
                break;
            case ALT:
                drawAlt(chart, p, n);
                break;
            }

            if (n.up) {
                samelevel = false;
            }
            if (n.next == null && firstLevel) {
                chart.drawLine(
                    p.x,
                    n.posLine.y,
                    p.x
                    + options.componentGapWidth() / 4,
                    n.posLine.y);
                chart.drawArrow(
                    p.x
                    + options.componentGapWidth() / 4
                    + options.arrowSize(),
                    n.posLine.y,
                    p.x
                    + options.componentGapWidth() / 4
                    + options.arrowSize(),
                    n.posLine.y,
                    Chart.ArrowDirection.RIGHT);
            }
            n = n.next;
        }
    }

	private void drawAlt(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.altSize.getWidth(),
		        n.altSize.getHeight());
		}

		// the two short lines at the beginning and the end of the alt
		// component
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + options.arcSize() * 3 / 2,
		    n.posLine.y);
		chart.drawLine(
		    p.x
		    + n.altSize.getWidth(),
		    n.posLine.y,
		    p.x
		    + n.altSize.getWidth()
		    - options.arcSize() * 3 / 2,
		    n.posLine.y);
		Node a = n;
		boolean first = true;
		while (a != null) {
		    // the horizontal lines at the beginning and the end
		    chart.drawLine(
		        p.x
		        + options.arcSize() * 3 / 2,
		        a.sub.posLine.y,
		        p.x
		        + (n.altSize.getWidth() - a.size.getWidth()) / 2,
		        a.sub.posLine.y);
		    chart.drawLine(
		        p.x
		        - options.arcSize() * 3 / 2
		        + n.altSize.getWidth()
		        + 1,
		        a.sub.posLine.y,
		        p.x
		        + (n.altSize.getWidth() - a.size.getWidth()) / 2
		        + a.size.getWidth(),
		        a.sub.posLine.y);
		    // the first alternative draws different arcs
		    if (first) {
		        chart.drawArcCorner(p.x, n.posLine.y, 270);
		        chart.drawArcCorner(
		        p.x
		        + n.altSize.getWidth()
		        - options.arcSize(),
		        n.posLine.y,
		        180);
		        first = false;
		    } else {
		        // else draw other arcs and vertical lines
		        chart.drawArcCorner(
		            p.x
		            + options.arcSize(),
		            a.sub.posLine.y
		            - options.arcSize(),
		            90);
		        chart.drawLine(
		            p.x
		            + options.arcSize(),
		            n.posLine.y
		            + options.arcSize() / 2,
		            p.x
		            + options.arcSize(),
		            a.posLine.y
		            - options.arcSize() / 2
		            + options.symbolGapHeight() - 2);
		        chart.drawArcCorner(
		            p.x
		            - options.arcSize() * 2
		            + n.altSize.getWidth(),
		            a.sub.posLine.y
		            - options.arcSize(),
		            0);
		        chart.drawLine(
		            p.x
		            - options.arcSize()
		            + n.altSize.getWidth(),
		            n.posLine.y
		            + options.arcSize() / 2,
		            p.x
		            - options.arcSize()
		            + n.altSize.getWidth(),
		            a.posLine.y
		            - options.arcSize() / 2
		            + options.symbolGapHeight()
		            - 2);
		    }
		    a.sub.drawComponents(
		        chart,
		        new Point(
		            p.x
		            + (n.altSize.getWidth() - a.size.getWidth()) / 2,
		            a.posEnd.y),
		        a.size);
		    a = a.down;
		}
		p.x += n.altSize.getWidth();
	}

	private void drawWrap(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (n.size.getHeight() != 0 && n.next != null) {
		    // the short horizontal line after the first component
		    chart.drawLine(
		        p.x,
		        n.posLine.y,
		        p.x
		        + options.componentGapWidth() / 4
		        + 1,
		        n.posLine.y);
		    // the short horizontal line at the beginning of the second
		    // component
		    chart.drawLine(
		        options.initialX(),
		        n.next.posLine.y,
		        options.initialX()
		        - options.componentGapWidth() / 4,
		        n.next.posLine.y);
		    // the quarter Arcs
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.posLine.y,
		        270);
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.posEnd.y
		        - options.arcSize(),
		        0);
		    chart.drawArcCorner(
		        options.initialX()
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.posEnd.y,
		        180);
		    chart.drawArcCorner(
		        options.initialX()
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.next.posLine.y
		        - options.arcSize(),
		        90);
		    // the short vertical lines between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posEnd.y
		        - options.arcSize() / 2
		        + 1);
		    chart.drawLine(
		        options.initialX()
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.posEnd.y
		        + options.arcSize() / 2,
		        options.initialX()
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2,
		        n.next.posLine.y
		        - options.arcSize() / 2
		        + 1);
		    // the long horizontal line in the middle oft the two
		    // components
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4 + 1,
		        n.posEnd.y,
		        options.initialX()
		        - options.componentGapWidth() / 4,
		        n.posEnd.y);

		    p.x = options.initialX();
		}
	}

	private void drawIteration(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		}
		ChartLayout layout = options.iterationLayout();
		if(options.iterationLayout() == ChartLayout.BEST) {
			if(n.sub.typ.matches(NodeType.ALT)) {
				layout = ChartLayout.LEFT_TO_RIGHT;
			} else {
				int count = countChildren(n.sub);
				layout = count > 1? ChartLayout.LEFT_TO_RIGHT: 
									ChartLayout.RIGHT_TO_LEFT;
			}
		}
		
		if(layout == ChartLayout.LEFT_TO_RIGHT) {
		 // the quarter Arcs
		    chart.drawArcCorner(p.x, n.posLine.y, 270);

		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.sub.posLine.y
		        - options.arcSize(),
		        90);
		    
		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.sub.posLine.y - options.arcSize(),
		        0);
		    
		    chart.drawArcCorner(
		            p.x
		            - options.componentGapWidth() / 4
		            - options.arcSize() / 2
		            + n.size.getWidth(),
		            n.posLine.y,
		            180);

		    //the two short vertical lines between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.sub.posLine.y
		        - options.arcSize() / 2
		        + 1);
		    chart.drawLine(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.sub.posLine.y
		        - options.arcSize() / 2
		        + 1);
		    // the two short horizontal lines between the quater Arcs and
		    // the components
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize()
		        - 1,
		        n.sub.posLine.y,
		        p.x
		        + options.componentGapWidth(),
		        n.sub.posLine.y);
		    chart.drawLine(
		        p.x
		        - options.componentGapWidth()
		        + n.size.getWidth(),
		        n.sub.posLine.y,
		        p.x
		        + n.size.getWidth()
		        - options.componentGapWidth() / 4
		        - options.arcSize() + 1,
		        n.sub.posLine.y);
		    // the long horizontal line in the middle
		    chart.drawLine(
		        p.x,
		        n.posLine.y,
		        p.x + n.size.getWidth(),
		        n.posLine.y);

		    n.sub.drawComponents(
		        chart,
		        new Point(
		                p.x
		                + options.componentGapWidth() / 2
		                + options.arcSize(),
		            0),
		        n.size);
		    
		    // the quarter Arcs                    
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.sub.posLine.y,
		        180);

		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.sub.posLine.y,
		        270);
		        
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight()
		        - options.arcSize(),
		        90);

		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight()
		        - options.arcSize(),
		        0);
		    // the short vertical lines between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.sub.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight() 
		        - options.arcSize() / 2);
		    chart.drawLine(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.sub.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight() 
		        - options.arcSize() / 2
		        );
		    // the long horizontal line between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize(),
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight(),
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize()
		        + n.size.getWidth()
		        + 1,
		        n.posLine.y + n.size.getHeight() - options.symbolGapHeight());
		} else {
		// the quarter Arcs
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.sub.posLine.y
		    - options.arcSize(),
		    90);
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y,
		    180);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.sub.posLine.y - options.arcSize(),
		    0);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.posLine.y,
		    270);
		// the short vertical lines between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.sub.posLine.y
		    - options.arcSize() / 2
		    + 1);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.sub.posLine.y
		    - options.arcSize() / 2
		    + 1);
		// the two short horizontal lines between the quater Arcs and
		// the components
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize()
		    - 1,
		    n.sub.posLine.y,
		    p.x
		    + options.componentGapWidth(),
		    n.sub.posLine.y);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth()
		    + n.size.getWidth(),
		    n.sub.posLine.y,
		    p.x
		    + n.size.getWidth()
		    - options.componentGapWidth() / 4
		    - options.arcSize() + 1,
		    n.sub.posLine.y);
		// the long horizontal line in the middle
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + n.size.getWidth(),
		    n.posLine.y);

		n.sub.drawComponentsInverse(
		    chart,
		    new Point(
		        p.x - options.componentGapWidth() + n.size.getWidth(),
		        0),
		    n.size);
		}
		p.x += n.size.getWidth();
	}

	private int countChildren(Node node) {
	    int count = 0;
	    while (node != null) {
            if (node.up) {
                node = null;
            } else {
                node = node.next;
                count++;
            }
        }
	    return count;
	}
	
	private void drawRerun(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (n.itergraph == null) {
		    if (options.showBorders()) {
		        chart.drawRectangle(
		            p.x,
		            n.posBegin.y,
		            n.size.getWidth(),
		            n.size.getHeight());
		    }

		    // the two short lines at the beginning and the end
		    chart.drawLine(
		        p.x,
		        n.posLine.y,
		        p.x + options.componentGapWidth(),
		        n.posLine.y);
		    chart.drawLine(
		        p.x
		        + n.size.getWidth(),
		        n.posLine.y,
		        p.x
		        + n.size.getWidth() - options.componentGapWidth(),
		        n.posLine.y);
		    // the quarter Arcs
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posEnd.y
		        - options.componentGapHeight() / 2
		        - options.arcSize(),
		        90);
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y,
		        180);
		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.posEnd.y
		        - options.componentGapHeight() / 2
		        - options.arcSize(),
		        0);
		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.posLine.y,
		        270);
		    // the short vertical lines between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posEnd.y
		        - options.componentGapHeight() / 2
		        - options.arcSize() / 2 + 1);
		    chart.drawLine(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.posEnd.y
		        - options.componentGapHeight() / 2
		        - options.arcSize() / 2 + 1);
		    // the the long horizontal line between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() - 1,
		        n.posEnd.y
		        - options.componentGapHeight() / 2,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize()
		        + n.size.getWidth()
		        + 1,
		        n.posEnd.y - options.componentGapHeight() / 2);

		    n.sub.drawComponents(
		        chart,
		        new Point(p.x + options.componentGapWidth(), 0),
		        n.size);
		    p.x += n.size.getWidth();
		} else {
		    if (options.showBorders()) {
		        chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		    }

		    int nodeWidth = n.size.getWidth();
		    int subNodeWidth = 0;
		    Node sn = n.sub;
		    int nnodes = 0;
		    while (sn != null) {
		        subNodeWidth += sn.size.getWidth();
		        if (sn.up) {
		            sn = null;
		        } else {
		            sn = sn.next;
		            nnodes++;
		        }
		    }
		    if (nnodes > 1) {
		        // nodes in sub > 1
		        subNodeWidth += options.componentGapWidth() * (nnodes);
		    }
		    // center node
		    int center = p.x + (nodeWidth - subNodeWidth) / 2;

		    int minCenter = p.x + options.componentGapWidth() / 4
		            + options.arcSize();
		    while (center < minCenter) {
		        center += options.componentGapWidth() / 2;
		        subNodeWidth -= options.componentGapWidth();
		    }

		    // the two short lines at the beginning and the end of the
		    // first component
		    chart.drawLine(p.x, n.posLine.y, center, n.posLine.y);
	
		    chart.drawLine(
		        center + subNodeWidth,
		        n.posLine.y,
		        p.x + n.size.getWidth(),
		        n.posLine.y);
		    // the quarter Arcs
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.itergraph.posLine.y
		        - options.arcSize(),
		        90);
		    chart.drawArcCorner(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y,
		        180);
		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.itergraph.posLine.y
		        - options.arcSize(),
		        0);
		    chart.drawArcCorner(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() * 3 / 2
		        + n.size.getWidth(),
		        n.posLine.y,
		        270);
		    // the short vertical lines between the quarter Arcs
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize() / 2,
		        n.itergraph.posLine.y
		        - options.arcSize() / 2
		        + 1);
		    chart.drawLine(
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.posLine.y
		        + options.arcSize() / 2,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize() / 2
		        + n.size.getWidth(),
		        n.itergraph.posLine.y
		        - options.arcSize() / 2
		        + 1);
		    // the two short lines at the beginning and the end of the
		    // second component
		    chart.drawLine(
		        p.x
		        + options.componentGapWidth() / 4
		        + options.arcSize(),
		        n.itergraph.posLine.y,
		        p.x
		        + n.size.getWidth() / 2
		        - n.iterSize.getWidth() / 2
		        - 1,
		        n.itergraph.posLine.y);
		    chart.drawLine(
		        p.x
		        + n.size.getWidth() / 2
		        + n.iterSize.getWidth() / 2
		        + 1,
		        n.itergraph.posLine.y,
		        p.x
		        - options.componentGapWidth() / 4
		        - options.arcSize()
		        + n.size.getWidth()
		        + 1,
		        n.itergraph.posLine.y);

		    n.itergraph.drawComponentsInverse(
		        chart,
		        new Point(
		            p.x
		            + n.size.getWidth() / 2
		            + n.iterSize.getWidth() / 2,
		            n.posEnd.y),
		        n.size);

		    n.sub.drawComponents(
		        chart,
		        new Point(center, n.posEnd.y),
		        n.size);

		    p.x += n.size.getWidth();
		}
	}

	private void drawOption(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		}

		// the two short lines at the beginning and the end
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + options.componentGapWidth(),
		    n.posLine.y);
		chart.drawLine(
		    p.x + n.size.getWidth(),
		    n.posLine.y,
		    p.x + n.size.getWidth() - options.componentGapWidth(),
		    n.posLine.y);
//        chart.drawArrow(
//            p.x
//            - options.componentGapWidth() / 4
//            + n.size.getWidth() + options.arrowSize(), 
//            n.posLine.y, 
//            p.x
//            - options.componentGapWidth() / 4
//            + n.size.getWidth() + options.arrowSize(),
//            n.posLine.y, 
//            ArrowDirection.RIGHT);
		// the quarter Arcs
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    - options.arcSize() / 2,
		    n.posLine.y,
		    270);
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posEnd.y
		    - options.arcSize()
		    - options.componentGapHeight() / 2,
		    90);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posLine.y,
		    180);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.posEnd.y
		    - options.arcSize()
		    - options.componentGapHeight() / 2,
		    0);
		// the short vertical lines between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posEnd.y
		    - options.arcSize() / 2
		    - options.componentGapHeight() / 2 + 1);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2 + n.size.getWidth(),
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posEnd.y
		    - options.arcSize() / 2
		    - options.componentGapHeight() / 2 + 1);
		// the the long horizontal line between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize(),
		    n.posEnd.y
		    - options.componentGapHeight() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize()
		    + n.size.getWidth()
		    + 1,
		    n.posEnd.y - options.componentGapHeight() / 2);

		n.sub.drawComponents(
		    chart,
		    new Point(p.x + options.componentGapWidth(), 0),
		    n.size);
		p.x += n.size.getWidth();
	}

	private void drawEpsilon(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		}

		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + n.size.getWidth(),
		    n.posLine.y);
	}

	private Point drawDefault(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y
		        - options.componentGapHeight() / 2,
		        n.size.getWidth(),
		        n.size.getHeight());
		}
		n.textBox = new TextBox(chart, n.sym.name, n.typ);
		if (n.typ.matches(NodeType.TERM, NodeType.EXCEPTION)) {
		    // the quarter Arcs
		    final int arcSize = (n.size.getHeight() - options
		            .componentGapHeight()) / 2;
		    chart.drawArcCorner(p.x, n.posBegin.y, arcSize, 180);
		    chart.drawArcCorner(p.x, n.posLine.y, arcSize, 90);
		    chart.drawArcCorner(
		        p.x + n.size.getWidth() - arcSize,
		        n.posBegin.y, arcSize, 270);
		    chart.drawArcCorner(
		        p.x + n.size.getWidth() - arcSize,
		        n.posLine.y, arcSize, 0);

		    n.textBox.drawAtCenter(
		        p.x, n.posBegin.y,
		        n.size.getWidth() - arcSize,
		        n.size.getHeight() - options.componentGapHeight());

		    // the short vertical and horizontal lines between the
		    // quarter Arcs
		    final int quarterHeight =
		        (n.size.getHeight() - options.componentGapHeight()) / 4;
		    chart.drawLine(
		        p.x + quarterHeight - 1,
		        n.posBegin.y,
		        p.x + n.size.getWidth() - quarterHeight + 1,
		        n.posBegin.y);
		    chart.drawLine(
		        p.x + quarterHeight - 1,
		        n.posEnd.y,
		        p.x + n.size.getWidth() - quarterHeight + 1,
		        n.posEnd.y);
		    chart.drawLine(
		        p.x,
		        n.posLine.y + quarterHeight + 1,
		        p.x,
		        n.posLine.y - quarterHeight - 1);
		    chart.drawLine(
		        p.x + n.size.getWidth(),
		        n.posLine.y + quarterHeight + 1,
		        p.x + n.size.getWidth(),
		        n.posLine.y - quarterHeight - 1);
		} else if(n.typ == NodeType.PREDICATE) {
		 // the quarter Arcs
		    final int arcSize = (n.size.getHeight() - options
		            .componentGapHeight()) / 2;

		    n.textBox.drawAtCenter(
		        p.x, n.posBegin.y,
		        n.size.getWidth() - arcSize,
		        n.size.getHeight() - options.componentGapHeight());

		    // the long horizontal lines
		    chart.drawLine(
		        p.x,
		        n.posEnd.y,
		        p.x + n.size.getWidth(),
		        n.posEnd.y);
		} else {
		    n.posBegin.x = p.x;
		    n.posEnd.x = p.x + n.size.getWidth();
		    chart.drawRectangle(
		        n.posBegin.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight() - options.componentGapHeight());

		    n.textBox.drawAtCenter(
		        n.posBegin.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight() - options.componentGapHeight());
		}
		if (Grammar.TRACE) {
		    System.out.println("text=" + n.sym.name);
		    System.out.println("n.posBegin.y=" + n.posBegin.y);
		    System.out.println(
		        "chart.getFontHeight()=" + options.fontHeight());
		    System.out.println("n.size=" + n.size.getHeight());
		    System.out.println(
		        "2="
		        + (n.size.getHeight() - options.componentGapHeight()));
		    System.out.println(
		        "3="
		        + (n.size.getHeight()
		           - options.componentGapHeight()
		           - options.fontHeight()) / 2);
		}
		if(n.typ != NodeType.PREDICATE) {
		chart.drawArrow(
		    p.x,
		    n.posLine.y,
		    p.x,
		    n.posLine.y,
		    Chart.ArrowDirection.RIGHT);
		}
		p.x += n.size.getWidth();
		// draw lines between t and nt nodes
		if (!n.up
		    && n.next != null
		        && (n.next.typ.matches(NodeType.TERM,
		                               NodeType.NONTERM,
		                               NodeType.EXCEPTION,
		                               NodeType.PREDICATE)))
		{
		    chart.drawArrow(
		        p.x,
		        n.posLine.y,
		        p.x + options.componentGapWidth() / 2,
		        n.posLine.y,
		        Chart.ArrowDirection.RIGHT);
		    p.x += options.componentGapWidth() / 2;
		}
		if (!n.up
		    && n.next != null
		    && n.next.typ == NodeType.WRAP
		    && n.next.size.getHeight() == 0)
		{
		    chart.drawArrow(
		        p.x,
		        n.posLine.y,
		        p.x + options.componentGapWidth() / 2,
		        n.posLine.y,
		        Chart.ArrowDirection.RIGHT);
		    p.x += options.componentGapWidth() / 2;
		}
		return p;
	}

    /*
     * Draw the components from right to left. Needed if for example in an
     * iter-node.
     */
    private void drawComponentsInverse(Chart chart, Point p, Size s) {
        Node n = this; // current node in the level
        boolean samelevel = true; // next node in same level?
        Point p1 = new Point(0, 0);
        while (n != null && samelevel) {
            p.x -= n.size.getWidth();
            if (n.typ.matches(NodeType.TERM, NodeType.NONTERM, NodeType.EXCEPTION, NodeType.PREDICATE)) {
                drawDefaultInverse(chart, p, n);
            } else if (n.typ == NodeType.EPS) {
                drawEpsilon(chart, p, n);
            } else if(n.typ == NodeType.PREDICATE) {
            } else if (n.typ == NodeType.OPT) {
                drawOptionInverse(chart, p, n, p1);
            } else if (n.typ == NodeType.RERUN) {
                drawRerunInverse(chart, p, n, p1);
            } else if (n.typ == NodeType.ITER) {
                drawIterationReverse(chart, p, n, p1);
            } else if (n.typ == NodeType.ALT) {
                drawAltInverse(chart, p, n, p1);
            }
            if (n.up) {
                samelevel = false;
            }
            n = n.next;
        }
    }

	private void drawAltInverse(Chart chart, Point p, Node n, Point p1) {
		ChartOptions options = chart.getOptions();
		p.x -= n.altSize.getWidth() - n.size.getWidth();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.altSize.getWidth(),
		        n.altSize.getHeight());
		}

		// the two short lines at the beginning and the end of the
		// alt component
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x
		    + options.arcSize() * 3 / 2,
		    n.posLine.y);
		chart.drawLine(
		    p.x
		    + n.altSize.getWidth(),
		    n.posLine.y,
		    p.x
		    + n.altSize.getWidth()
		    - options.arcSize() * 3 / 2,
		    n.posLine.y);
		p1.x = p.x + 2 * options.componentGapWidth();
		p1.y = p1.y + options.componentGapHeight();
		Node a = n;
		boolean first = true;
		while (a != null) {
		    // the horizontal lines at the beginning and the end
		    chart.drawLine(
		        p.x
		        + options.arcSize() * 3 / 2,
		        a.sub.posLine.y,
		        p.x
		        + (n.altSize.getWidth() - a.size.getWidth()) / 2,
		        a.sub.posLine.y);
		    chart.drawLine(
		        p.x
		        - options.arcSize() * 3 / 2
		        + n.altSize.getWidth()
		        + 1,
		        a.sub.posLine.y,
		        p.x
		        + (n.altSize.getWidth() - a.size.getWidth()) / 2
		        + a.size.getWidth(),
		        a.sub.posLine.y);
		    // if the first Alternative draw differnt Arcs
		    if (first) {
		        chart.drawArcCorner(p.x, n.posLine.y, 270);
		        chart.drawArcCorner(
		            p.x
		            + n.altSize.getWidth()
		            - options.arcSize(),
		            n.posLine.y,
		            180);
		        first = false;
		    } else {
		        // else draw other Arcs and vertical lines
		        chart.drawArcCorner(
		            p.x
		            + options.arcSize(),
		            a.sub.posLine.y
		            - options.arcSize(),
		            90);
		        chart.drawLine(
		            p.x
		            + options.arcSize(),
		            n.posLine.y
		            + options.arcSize() / 2,
		            p.x
		            + options.arcSize(),
		            a.posLine.y
		            - options.arcSize() / 2
		            + 1);
		        chart.drawArcCorner(
		            p.x
		            - options.arcSize() * 2
		            + n.altSize.getWidth(),
		            a.sub.posLine.y
		            - options.arcSize(),
		            0);
		        chart.drawLine(
		            p.x
		            - options.arcSize()
		            + n.altSize.getWidth(),
		            n.posLine.y
		            + options.arcSize() / 2,
		            p.x
		            - options.arcSize()
		            + n.altSize.getWidth(),
		            a.posLine.y
		            - options.arcSize() / 2
		            + 1);
		    }
		    Point pf =
		        new Point(
		            p.x
		            + (n.altSize.getWidth() + a.size.getWidth()) / 2,
		            p1.y);
		    a.sub.drawComponentsInverse(chart, pf, a.size);
		    a = a.down;
		}
	}

	private void drawIterationReverse(Chart chart, Point p, Node n, Point p1) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		}

		// the quarter Arcs
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.sub.posLine.y
		    - options.arcSize(),
		    90);
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y,
		    180);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.sub.posLine.y
		    - options.arcSize(),
		    0);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.posLine.y,
		    270);
		// the short vertical lines between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.sub.posLine.y
		    - options.arcSize() / 2
		    + 1);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.sub.posLine.y
		    - options.arcSize() / 2
		    + 1);
		// the two short horizontal lines between the quater Arcs and
		// the components
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize()
		    - 1,
		    n.sub.posLine.y,
		    p.x
		    + options.componentGapWidth(),
		    n.sub.posLine.y);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth()
		    + n.size.getWidth(),
		    n.sub.posLine.y,
		    p.x
		    + n.size.getWidth()
		    - options.componentGapWidth() / 4
		    - options.arcSize()
		    + 1,
		    n.sub.posLine.y);
		// the long horizontal line in the middle
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + n.size.getWidth(),
		    n.posLine.y);

		p1.x = p.x + options.componentGapWidth();
		n.sub.drawComponents(chart, p1, n.size);
	}

	private void drawRerunInverse(Chart chart, Point p, Node n, Point p1) {
		ChartOptions options = chart.getOptions();
		if(n.itergraph == null) {
			if (options.showBorders()) {
			    chart.drawRectangle(
			        p.x,
			        n.posBegin.y,
			        n.size.getWidth(),
			        n.size.getHeight());
			}
	
			// the two short lines at the beginning and the end
			chart.drawLine(
			    p.x,
			    n.posLine.y,
			    p.x + options.componentGapWidth(),
			    n.posLine.y);
			chart.drawLine(
			    p.x + n.size.getWidth(),
			    n.posLine.y,
			    p.x + n.size.getWidth() - options.componentGapWidth(),
			    n.posLine.y);
			// the quarter Arcs
			chart.drawArcCorner(
			    p.x
			    + options.componentGapWidth() / 4
			    + options.arcSize() / 2,
			    n.posEnd.y
			    - options.componentGapHeight() / 2
			    - options.arcSize(),
			    90);
			chart.drawArcCorner(
			    p.x
			    + options.componentGapWidth() / 4
			    + options.arcSize() / 2,
			    n.posLine.y,
			    180);
			chart.drawArcCorner(
			    p.x
			    - options.componentGapWidth() / 4
			    - options.arcSize() * 3 / 2
			    + n.size.getWidth(),
			    n.posEnd.y
			    - options.componentGapHeight() / 2
			    - options.arcSize(),
			    0);
			chart.drawArcCorner(
			    p.x
			    - options.componentGapWidth() / 4
			    - options.arcSize() * 3 / 2
			    + n.size.getWidth(),
			    n.posLine.y,
			    270);
			// the short vertical lines between the quarter Arcs
			chart.drawLine(
			    p.x
			    + options.componentGapWidth() / 4
			    + options.arcSize() / 2,
			    n.posLine.y
			    + options.arcSize() / 2,
			    p.x
			    + options.componentGapWidth() / 4
			    + options.arcSize() / 2,
			    n.posEnd.y
			    - options.componentGapHeight() / 2
			    - options.arcSize() / 2
			    + 1);
			chart.drawLine(
			    p.x
			    - options.componentGapWidth() / 4
			    - options.arcSize() / 2
			    + n.size.getWidth(),
			    n.posLine.y
			    + options.arcSize() / 2,
			    p.x
			    - options.componentGapWidth() / 4
			    - options.arcSize() / 2
			    + n.size.getWidth(),
			    n.posEnd.y
			    - options.componentGapHeight() / 2
			    - options.arcSize() / 2
			    + 1);
			// the the long horizontal line between the quarter Arcs
			chart.drawLine(
			    p.x
			    + options.componentGapWidth() / 4
			    + options.arcSize()
			    - 1,
			    n.posEnd.y
			    - options.componentGapHeight() / 2,
			    p.x
			    - options.componentGapWidth() / 4
			    - options.arcSize()
			    + n.size.getWidth()
			    + 1,
			    n.posEnd.y
			    - options.componentGapHeight() / 2);
	
			p1.x = p.x + n.size.getWidth() - options.componentGapWidth();
			n.sub.drawComponentsInverse(chart, p1, n.size);
		} else {
			if (options.showBorders()) {
                chart.drawRectangle(
                    p.x,
                    n.posBegin.y,
                    n.size.getWidth(),
                    n.size.getHeight());
            }

            // the two short lines at the beginning and the end of
            // the first component
            chart.drawLine(
                p.x,
                n.posLine.y,
                p.x
                + n.size.getWidth() / 2
                - n.altSize.getWidth() / 2
                - 1, n.posLine.y);
            chart.drawLine(
                p.x
                + n.size.getWidth() / 2
                + n.altSize.getWidth() / 2
                + 1,
                n.posLine.y,
                p.x
                + n.size.getWidth(),
                n.posLine.y);
            // the quarter Arcs
            chart.drawArcCorner(
                p.x
                + options.componentGapWidth() / 4
                + options.arcSize() / 2,
                n.itergraph.posLine.y
                - options.arcSize(),
                90);
            chart.drawArcCorner(
                p.x
                + options.componentGapWidth() / 4
                + options.arcSize() / 2,
                n.posLine.y,
                180);
            chart.drawArcCorner(
                p.x
                - options.componentGapWidth() / 4
                - options.arcSize() * 3 / 2
                + n.size.getWidth(),
                n.itergraph.posLine.y
                - options.arcSize(),
                0);
            chart.drawArcCorner(
                p.x
                - options.componentGapWidth() / 4
                - options.arcSize() * 3 / 2
                + n.size.getWidth(),
                n.posLine.y,
                270);
            // the short vertical lines between the quarter Arcs
            chart.drawLine(
                p.x
                + options.componentGapWidth() / 4
                + options.arcSize() / 2,
                n.posLine.y
                + options.arcSize() / 2,
                p.x
                + options.componentGapWidth() / 4
                + options.arcSize() / 2,
                n.itergraph.posLine.y
                - options.arcSize() / 2
                + 1);
            chart.drawLine(
                p.x
                - options.componentGapWidth() / 4
                - options.arcSize() / 2
                + n.size.getWidth(),
                n.posLine.y
                + options.arcSize() / 2,
                p.x
                - options.componentGapWidth() / 4
                - options.arcSize() / 2
                + n.size.getWidth(),
                n.itergraph.posLine.y
                - options.arcSize() / 2
                + 1);
            // the two short lines at the beginning and the end of the
            // second component
            chart.drawLine(
                p.x
                + options.componentGapWidth() / 4
                + options.arcSize(),
                n.itergraph.posLine.y,
                p.x
                + n.size.getWidth() / 2
                - n.iterSize.getWidth() / 2
                - 1,
                n.itergraph.posLine.y);
            chart.drawLine(
                p.x
                + n.size.getWidth() / 2
                + n.iterSize.getWidth() / 2
                + 1,
                n.itergraph.posLine.y,
                p.x
                - options.componentGapWidth() / 4
                - options.arcSize()
                + n.size.getWidth()
                + 1,
                n.itergraph.posLine.y);

            n.sub.drawComponentsInverse(
                chart,
                new Point(
                    p.x
                    + n.size.getWidth() / 2
                    + n.altSize.getWidth() / 2,
                    n.posEnd.y),
                n.size);
            n.itergraph.drawComponents(
                chart,
                new Point(
                    p.x
                    + n.size.getWidth() / 2
                    - n.iterSize.getWidth() / 2,
                    n.posEnd.y),
                n.size);
		}
	}

	private void drawOptionInverse(Chart chart, Point p, Node n, Point p1) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight());
		}

		// the two short lines at the beginning and the end
		chart.drawLine(
		    p.x,
		    n.posLine.y,
		    p.x + options.componentGapWidth(),
		    n.posLine.y);
		chart.drawLine(
		    p.x + n.size.getWidth(),
		    n.posLine.y,
		    p.x + n.size.getWidth() - options.componentGapWidth(),
		    n.posLine.y);
		// the quarter Arcs
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    - options.arcSize() / 2,
		    n.posLine.y,
		    270);
		chart.drawArcCorner(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posEnd.y
		    - options.arcSize()
		    - options.componentGapHeight() / 2,
		    90);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posLine.y,
		    180);
		chart.drawArcCorner(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() * 3 / 2
		    + n.size.getWidth(),
		    n.posEnd.y
		    - options.arcSize()
		    - options.componentGapHeight() / 2,
		    0);
		// the short vertical lines between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize() / 2,
		    n.posEnd.y
		    - options.arcSize() / 2
		    - options.componentGapHeight() / 2
		    + 1);
		chart.drawLine(
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posLine.y
		    + options.arcSize() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize() / 2
		    + n.size.getWidth(),
		    n.posEnd.y
		    - options.arcSize() / 2
		    - options.componentGapHeight() / 2
		    + 1);
		// the the long horizontal line between the quarter Arcs
		chart.drawLine(
		    p.x
		    + options.componentGapWidth() / 4
		    + options.arcSize(),
		    n.posEnd.y
		    - options.componentGapHeight() / 2,
		    p.x
		    - options.componentGapWidth() / 4
		    - options.arcSize()
		    + n.size.getWidth()
		    + 1,
		    n.posEnd.y
		    - options.componentGapHeight() / 2);

		p1.x = p.x + n.size.getWidth() - options.componentGapWidth();
		n.sub.drawComponentsInverse(chart, p1, n.size);
	}

	private void drawDefaultInverse(Chart chart, Point p, Node n) {
		ChartOptions options = chart.getOptions();
		if (options.showBorders()) {
		    chart.drawRectangle(
		        p.x,
		        n.posBegin.y
		        - options.componentGapHeight() / 2,
		        n.size.getWidth(),
		        n.size.getHeight());
		}
		if (n.typ.matches(NodeType.TERM, NodeType.EXCEPTION)) {
		    // the quarter Arcs
		    final int arcSize =
		        (n.size.getHeight() - options.componentGapHeight()) / 2;
		    chart.drawArc(
		        p.x,
		        n.posBegin.y,
		        arcSize,
		        arcSize,
		        180,
		        90);
		    chart.drawArc(
		        p.x,
		        n.posLine.y,
		        arcSize,
		        arcSize,
		        90,
		        90);
		    chart.drawArc(
		        p.x
		        + n.size.getWidth()
		        - arcSize,
		        n.posBegin.y,
		        arcSize, arcSize, 270, 90);
		    chart.drawArc(
		        p.x
		        + n.size.getWidth()
		        - arcSize,
		        n.posLine.y,
		        arcSize,
		        arcSize,
		        0,
		        90);
		    // the short vertical and horizontal lines between the
		    // quarter Arcs
		    chart.drawLine(
		        p.x
		        + (n.size.getHeight() - options.componentGapHeight()) / 4
		        - 1,
		        n.posBegin.y,
		        p.x
		        + n.size.getWidth()
		        - (n.size.getHeight() - options.componentGapHeight()) / 4
		        + 1,
		        n.posBegin.y);
		    chart.drawLine(
		        p.x
		        + (n.size.getHeight() - options.componentGapHeight()) / 4
		        - 1,
		        n.posEnd.y,
		        p.x
		        + n.size.getWidth()
		        - (n.size.getHeight() - options.componentGapHeight()) / 4
		        + 1,
		        n.posEnd.y);
		    chart.drawLine(
		        p.x,
		        n.posLine.y
		        + (n.size.getHeight() - options.componentGapHeight()) / 4
		        + 1,
		        p.x,
		        n.posLine.y
		        - (n.size.getHeight() - options.componentGapHeight()) / 4
		        - 1);
		    chart.drawLine(
		        p.x
		        + n.size.getWidth(),
		        n.posLine.y
		        + (n.size.getHeight() - options.componentGapHeight()) / 4
		        + 1,
		        p.x
		        + n.size.getWidth(),
		        n.posLine.y
		        - (n.size.getHeight() - options.componentGapHeight()) / 4
		        - 1);
		} else if(n.typ == NodeType.PREDICATE) {
		    chart.drawLine(
		            p.x,
		            n.posEnd.y,
		            p.x
		            + n.size.getWidth(),
		            n.posEnd.y);
		} else {
		    n.posBegin.x = p.x;
		    n.posEnd.x = p.x + n.size.getWidth();
		    chart.drawRectangle(
		        n.posBegin.x,
		        n.posBegin.y,
		        n.size.getWidth(),
		        n.size.getHeight() - options.componentGapHeight());
		}

		TextBox textBox = new TextBox(chart, n.sym.name, n.typ);
		textBox.drawAtCenter(
		    p.x,
		    n.posBegin.y,
		    n.size.getWidth(),
		    n.size.getHeight() - options.componentGapHeight());

		if(n.typ != NodeType.PREDICATE) {
		    chart.drawArrow(
		        p.x + n.size.getWidth(),
		        n.posLine.y,
		        p.x + n.size.getWidth(),
		        n.posLine.y,
		        Chart.ArrowDirection.LEFT);
		}

		if (!n.up
		    && n.next != null
		    && (n.next.typ.matches(NodeType.TERM,
		                            NodeType.NONTERM,
		                            NodeType.EXCEPTION)))
		{
		    chart.drawArrow(
		        p.x,
		        n.posLine.y,
		        p.x
		        - options.componentGapWidth() / 2,
		        n.posLine.y,
		        Chart.ArrowDirection.LEFT);
		    p.x -= options.componentGapWidth() / 2;
		}
		if (!n.up
		    && n.next != null
		    && n.next.typ == NodeType.WRAP
		    && n.next.size.getHeight() == 0)
		{
		    if (!n.next.up
		        && n.next.next != null
		            && (n.next.next.typ.matches(NodeType.TERM, 
		                                    NodeType.NONTERM,
		                                    NodeType.EXCEPTION)))
		    {
		        chart.drawArrow(
		            p.x,
		            n.posLine.y,
		            p.x - options.componentGapWidth() / 2,
		            n.posLine.y,
		            Chart.ArrowDirection.LEFT);
		        p.x -= options.componentGapWidth() / 2;
		    }
		}
	}

    public void accept(Chart.NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    public void visitChildren(Chart.NodeVisitor visitor) {
        switch (typ) {
        case WRAP:
        case PREDICATE:
        case EXCEPTION:
        case TERM:
        case NONTERM:
        case EPS:
            break;
        case ALT:
            for (Node n = this; n != null; n = n.down) {
                n.sub.accept(visitor);
            }
            break;
        case ITER:
                for(Node n: nextChildren(sub)) {
                	n.accept(visitor);
                }
            break;
        case OPT:
            sub.accept(visitor);
            break;
        case RERUN:
			if (itergraph != null) {
				itergraph.accept(visitor);
			}
            sub.accept(visitor);
        break;
        default:
            throw new RuntimeException("unknown <" + typ + ">");
        }
		if (next != null && !up) {
			next.accept(visitor);
		}
    }

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
        unparse(buffer);
        return buffer.toString();
    }
}

// End Node.java
