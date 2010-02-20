package net.hydromatic.clapham.parser;

/**
 * 
 * An syntax parser exception. It could be throw during the parse phase
 * 
 * @author Edgar Espina
 * @see Language
 */
public class LanguageParserException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LanguageParserException(String message) {
		super(message);
	}

	public LanguageParserException(Throwable cause, String message) {
		super(message, cause);
	}
}
