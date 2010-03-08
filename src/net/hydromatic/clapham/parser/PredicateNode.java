package net.hydromatic.clapham.parser;

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Graph;
import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Symbol;

/**
 * TODO:
 * 
 * @author Edgar Espina
 * @version $Id$
 * @since Mar 5, 2010
 */
public class PredicateNode extends BaseEbnfNode {
    public final String s;

    public final EbnfNode node;

    public PredicateNode(String s, EbnfNode node) {
        this.s = s;
        this.node = node;
    }

    public Graph toGraph(Grammar grammar) {
        final Graph g2 = node.toGraph(grammar);
        Symbol symbol = new Symbol(NodeType.PREDICATE, s);
        // grammar.symbolMap.put(symbol.name, symbol);
        Graph g1 = new Graph(new Node(grammar, symbol));
        grammar.makePredicate(g1, g2);
        return g1;
    }

    public void toString(StringBuilder buf) {
        StringBuilder buf1 = new StringBuilder();;
        node.toString(buf1 );
        buf.append("PredicateNode(").append(s).append(", ").append(buf1).append(")");
    }

    public String toEbnf(EbnfDecorator decorator) {
        return s;
    }
}