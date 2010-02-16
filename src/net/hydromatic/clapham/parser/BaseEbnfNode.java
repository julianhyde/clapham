package net.hydromatic.clapham.parser;

import java.util.List;

/**
 * 
 * A base class for {@link EbnfNode}
 * 
 * @author Edgar Espina
 * 
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
}
