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
package net.hydromatic.clapham.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import net.hydromatic.clapham.parser.Language;
import net.hydromatic.clapham.parser.LanguageParserException;
import net.hydromatic.clapham.parser.ProductionNode;

/**
 * A factory for {@link Grammar grammars}
 * 
 * @author Edgar Espina
 */
public class GrammarFactory {

	private static String toString(Reader input) throws IOException {
		try {
			input = (input instanceof StringReader) ? input
					: new BufferedReader(input);
			int b = input.read();
			StringBuilder buff = new StringBuilder();
			while (b != -1) {
				buff.append((char) b);
				b = input.read();
			}
			return buff.toString();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static final Grammar build(Language language, Reader input,
			boolean optimize) throws LanguageParserException {
		if (language == null) {
			throw new IllegalArgumentException("language can't be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input can't be null");
		}
		try {
			List<ProductionNode> productionList = language
					.parse(toString(input));

			Grammar grammar = new Grammar();
			for (ProductionNode productionNode : productionList) {
				Symbol symbol = new Symbol(NodeType.NONTERM, productionNode.id.s)
											.withProduction(productionNode);
				grammar.nonterminals.add(symbol);
				grammar.symbolMap.put(symbol.name, symbol);
				Graph g = productionNode.toGraph(grammar);
				symbol.graph = g;
			}
			grammar.setOptimizeGraph(optimize);
			grammar.optimize();
			return grammar;
		} catch (IOException ex) {
			throw new LanguageParserException(ex, "Error reading file");
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

// End GrammarFactory.java
