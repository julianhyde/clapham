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

import net.hydromatic.clapham.graph.Grammar;
import net.hydromatic.clapham.graph.Symbol;

public abstract class AbstractChart implements Chart, ChartOptions {
	private MutableChartOptions options;

	protected final Grammar grammar;
	
	public AbstractChart(Grammar grammar) {
		this.grammar = grammar;
	}
	
	public final void calcDrawing() {
		for (Symbol s : grammar.nonterminals) {
			s.graph.graphSize = s.graph.l.calcSize(this);
			s.graph.l.setWrapSize(this);
			s.graph.l.calcPos(this, initialY(), false);
			if (Grammar.TRACE) {
				System.out.println("\n\n" + s.graph.graphSize.toString());
			}
		}
		if (Grammar.TRACE) {
			grammar.printNodes(System.out);
		}
	}

	public MutableChartOptions getOptions() {
		return options;
	}

	public void setOptions(MutableChartOptions options) {
		this.options = options;
	}

	public int fontHeight() {
		return getOptions().fontHeight();
	}

	public int initialX() {
		return getOptions().initialX();
	}

	public int initialY() {
		return getOptions().initialY();
	}

	public int stringWidth(String text) {
		return getOptions().stringWidth(text);
	}

	public int componentGapWidth() {
		return getOptions().componentGapWidth();
	}

	public int componentGapHeight() {
		return getOptions().componentGapHeight();
	}

	public int arrowSize() {
		return getOptions().arrowSize();
	}

	public int arcSize() {
		return getOptions().arcSize();
	}

	public int symbolGapWidth() {
		return getOptions().symbolGapWidth();
	}

	public int symbolGapHeight() {
		return getOptions().symbolGapHeight();
	}

	public void withInitialLocation(int x, int y) {
		getOptions().withInitialLocation(x, y);
	}
	
	public boolean showBorders() {
		return getOptions().showBorders();
	}
	
	public boolean optimize() {
		return getOptions().optimize();
	}
}
