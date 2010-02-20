package net.hydromatic.clapham.parser;

import java.util.List;

/**
 * 
 * A bridge between {@link ProductionNode nodes} (what clapham understand) and
 * any other language representation (usually bnf, ebnf, wirth or variants from
 * those)
 * 
 * @author Edgar Espina
 * 
 */
public interface Language {

	/**
	 * Parse the given input an returns {@link ProductionNode nodes}
	 * 
	 * @param input
	 *            The input to parse
	 * @return A list of {@link ProductionNode nodes}
	 */
	List<ProductionNode> parse(String input) throws LanguageParserException;
}
