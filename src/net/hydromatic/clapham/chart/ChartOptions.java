/*
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

package net.hydromatic.clapham.chart;

/**
 * {@link Chart} options. These options affect how the {@link Chart} is
 * displayed.
 * 
 * @author Edgar Espina
 * @version $Id: $
 */
public interface ChartOptions {
    /**
     * The default arc size
     */
    int ARC_SIZE = 16;

    /**
     * The default component gap height
     */
    int COMPONENT_GAP_HEIGHT = 10;

    /**
     * The default component gap width
     */
    int COMPONENT_GAP_WIDTH = 32;

    /**
     * The default arrow size
     */
    int ARROW_SIZE = 3;

    /**
     * The default initial x
     */
    int INITIAL_X = 35;

    /**
     * The default initial y
     */
    int INITIAL_Y = 20;

    int SYMBOL_GAP_HEIGHT = 4;

    int SYMBOL_GAP_WIDTH = 2;

    /**
     * The font height
     * 
     * @return The font height
     */
    int fontHeight();

    /**
     * How much vertical space is between two symbols
     * 
     * @return How much vertical space is between two symbols
     */
    int componentGapHeight();

    /**
     * How much horizontal space is between two symbols
     * 
     * @return How much horizontal space is between two symbols
     */
    int componentGapWidth();

    /**
     * The string width of the text. That's the string width calculated using
     * the underlying font
     * 
     * @param text
     * @return
     */
    int stringWidth(String text);

    /**
     * The vertical gap between the text of a symbol and the box that contains
     * the symbol
     * 
     * @return The vertical gap between the text of a symbol and the box that
     *         contains the symbol
     */
    int symbolGapHeight();

    /**
     * The horizontal gap between the text of a symbol and the box that contains
     * the symbol
     * 
     * @return The horizontal gap between the text of a symbol and the box that
     *         contains the symbol
     */
    int symbolGapWidth();

    /**
     * The arc size used in optional and iterations node
     * 
     * @return The arc size used in optional and iterations node
     */
    int arcSize();

    /**
     * Draw the borders of each node. Useful for debugging purpose only
     * 
     * @return
     */
    boolean showBorders();

    /**
     * The x coordinate where the graph should start drawing it
     * 
     * @return
     */
    int initialX();

    /**
     * The y coordinate were the graph should start drawing it
     * 
     * @return
     */
    int initialY();

    /**
     * How big is the arrow of each connector
     * 
     * @return
     */
    int arrowSize();

    /**
     * where the drawing starts
     * 
     * @param x
     * @param y
     */
    void withInitialLocation(int x, int y);

    boolean optimize();
}
