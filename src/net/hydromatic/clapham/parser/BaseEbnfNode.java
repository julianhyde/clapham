/*
// $Id: Grammar.java 20 2010-02-25 21:12:52Z jhyde $
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2010-2010 Edgar Espina
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

/**
 * A base class for {@link EbnfNode}.
 * 
 * @author Edgar Espina
 * @version $Id: Grammar.java 20 2010-02-25 21:12:52Z jhyde $
 */
public abstract class BaseEbnfNode implements EbnfNode {

	public <E extends EbnfNode> void toString(StringBuilder buf, String start,
			List<E> list, String end) {
		int i = 0;
		buf.append(start);
		for (E node : list) {
			if (i++ > 0) {
				buf.append(", ");
			}
			node.toString(buf);
		}
		buf.append(end);
	}

	protected String toEbnf(EbnfDecorator decorator, EbnfNode node, String operator) {
		StringBuilder buff = new StringBuilder();
		boolean parenthesis = node instanceof SequenceNode;
		buff.append(node.toEbnf(decorator));
		if (parenthesis) {
			buff.insert(0, "(").append(")");
		}
		buff.append(operator).append(" ");
		return buff.toString();
	}
}

// End BaseEbnfNode.java
