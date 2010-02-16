package net.hydromatic.clapham.chart;

import java.awt.Font;

import net.hydromatic.clapham.graph.Node;
import net.hydromatic.clapham.graph.Symbol;
import net.hydromatic.clapham.graph.Grammar.Direction;

/**
 * 
 * @author Edgar Espina
 *
 */
public interface Chart {
	
	interface NodeVisitor {
        void visit(Node node);
    }

	/**
	 * The font height
	 * 
	 * @return The font height
	 */
	int getFontHeight();

	/**
	 * The component gap height
	 * @return
	 */
	int componentGapHeight();

	int getStringWidth(Font font, String text);

	void drawString(String text, float x, float y);

	int symbolGapHeight();

	int symbolGapWidth();

	float componentArcSize();

	int componentGapWidth();

	boolean showBorders();

	void drawRectangle(float x, float f, float width, float height);

	void drawArcCorner(float x, float y, float foo, int i);

	void drawLine(float f, float y, float g, float y2);

	void drawArrow(float x, float y, float x2, float y2, Direction right);

	void drawArcCorner(float f, float y, int i);

	float beginningXCoordinate();

	void drawArc(float x, float y, float foo, float foo2, int i, int j);

	int arrowSize();

	void calcDrawing();

	void drawComponent(Symbol symbol);

}
