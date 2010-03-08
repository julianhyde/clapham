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
public class AnyCharacterNode extends BaseEbnfNode {
    public final String s;

    public AnyCharacterNode(String s) {
        this.s = s;
    }

    public Graph toGraph(Grammar grammar) {
        Symbol symbol = new Symbol(NodeType.TERM, s);
        // TODO: do we really need to add it as a Terminal?
        // grammar.terminals.add(symbol);
        Graph graph = new Graph(new Node(grammar, symbol));
        return graph;
    }

    public void toString(StringBuilder buf) {
        buf.append(s);
    }

    public String toEbnf(EbnfDecorator decorator) {
        return s;
    }
}