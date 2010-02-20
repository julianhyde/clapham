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
 * 
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

		if (language == null)
			throw new IllegalArgumentException("language can't be null");
		if (input == null)
			throw new IllegalArgumentException("input can't be null");

		try {
			List<ProductionNode> productionList = language
					.parse(toString(input));

			Grammar grammar = new Grammar();
			for (ProductionNode productionNode : productionList) {
				Symbol symbol = new Symbol(NodeType.NONTERM,
						productionNode.id.s);
				grammar.nonterminals.add(symbol);
				grammar.symbolMap.put(symbol.name, symbol);
				Graph g = productionNode.toGraph(grammar);
				symbol.graph = g;
				grammar.ruleMap.put(symbol, g);
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
