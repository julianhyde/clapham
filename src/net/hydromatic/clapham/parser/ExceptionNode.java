package net.hydromatic.clapham.parser;

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Graph;
import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.NodeType;
import net.hydromatic.clapham.graph.Symbol;

public class ExceptionNode extends BaseEbnfNode {

    public final EbnfNode node;

    public final String operator;

    public ExceptionNode(EbnfNode node, String operator) {
        this.node = node;
        this.operator = operator;
    }

    public ExceptionNode(EbnfNode node) {
        this(node, "-");
    }

    public String toEbnf(EbnfDecorator decorator) {
        StringBuilder buff = new StringBuilder();
        buff.append(operator);
        String ebnf = node.toEbnf(decorator);
        buff.append(ebnf);
        return buff.toString();
    }

    public Graph toGraph(Grammar grammar) {
        Symbol symbol = new Symbol(NodeType.EXCEPTION, operator);
        // grammar.symbolMap.put(symbol.name, symbol);
        Graph g = new Graph(new Node(grammar, symbol));
        Graph g2 = node.toGraph(grammar);
        grammar.makeException(g, g2);
        return g;
    }

    public void toString(StringBuilder buf) {
        StringBuilder nBuf = new StringBuilder();
        node.toString(nBuf);
        buf.append("ExceptionNode(").append(nBuf).append(")");
    }

}
