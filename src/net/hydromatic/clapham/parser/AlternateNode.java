/*
// $Id$
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
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
package net.hydromatic.clapham.parser;

import java.util.List;

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Graph;

/**
 * TODO:
*
* @author jhyde
* @version $Id$
* @since Jul 30, 2008
*/
public class AlternateNode extends BaseEbnfNode {
    public final List<EbnfNode> list;

    public AlternateNode(List<EbnfNode> list) {
        this.list = list;
    }

    public Graph toGraph(Grammar grammar) {
        Graph g = null;
        for (EbnfNode node : list) {
            if (g == null) {
                g = node.toGraph(grammar);
                grammar.makeFirstAlt(g);
            } else {
                Graph g2 = node.toGraph(grammar);
                grammar.makeAlternative(g, g2);
            }
        }
        return g;
    }

    public void toString(StringBuilder buf) {
        toString(buf, "AlternateNode(", list, ")");
    }
    
    public String toEbnf(EbnfDecorator decorator) {
    	StringBuilder buff = new StringBuilder().append("(");
    	final String SEPARATOR = " | ";
    	for(EbnfNode n: list) {
    		buff.append(n.toEbnf(decorator)).append(SEPARATOR);
    	}
    	
  		buff.setLength(buff.length() - SEPARATOR.length());
  		
    	return buff.append(")").toString();
    }
}

// End AlternateNode.java
