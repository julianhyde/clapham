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

import java.io.File;

public abstract class AbstractChartOptions implements ChartOptions {

    private int arcSize;

    private int arrowSize;

    private int componentGapHeight;

    private int componentGapWidth;

    private int initialX;

    private int initialY;

    private boolean showBorders;

    private int symbolGapHeight;

    private int symbolGapWidth;

    private String imageFormat;

    private File outputDirectory;

    private boolean optimize;

    private ChartOrder iterationOrder;

    public AbstractChartOptions() {
        this.arcSize = ARC_SIZE;
        this.arrowSize = ARROW_SIZE;
        this.componentGapHeight = COMPONENT_GAP_HEIGHT;
        this.componentGapWidth = COMPONENT_GAP_WIDTH;
        this.initialX = INITIAL_X;
        this.initialY = INITIAL_Y;
        this.showBorders = false;
        this.optimize = true;
        this.symbolGapHeight = SYMBOL_GAP_HEIGHT;
        this.symbolGapWidth = SYMBOL_GAP_WIDTH;
        this.iterationOrder = ChartOrder.LEFT_TO_RIGHT;
    }

    public boolean optimize() {
        return optimize;
    }

    public int arcSize() {
        return arcSize;
    }

    public int arrowSize() {
        return arrowSize;
    }

    public int componentGapHeight() {
        return componentGapHeight;
    }

    public int componentGapWidth() {
        return componentGapWidth;
    }

    public int initialX() {
        return initialX;
    }

    public int initialY() {
        return initialY;
    }

    public boolean showBorders() {
        return showBorders;
    }

    public int symbolGapHeight() {
        return symbolGapHeight;
    }

    public int symbolGapWidth() {
        return symbolGapWidth;
    }

    public ChartOptions withComponentGapHeight(int height) {
        componentGapHeight = height;
        return this;
    }

    public ChartOptions withArcSize(int size) {
        arcSize = size;
        return this;
    }

    public void withInitialLocation(int x, int y) {
        initialX = x;
        initialY = y;
    }

    public String imageFormat() {
        return imageFormat;
    }

    public File outputDirectory() {
        return outputDirectory;
    }

    public ChartOptions withOuputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public ChartOptions withOptimize(boolean optimize) {
        this.optimize = optimize;
        return this;
    }

    public ChartOptions withIterationOrder(ChartOrder order) {
        if (order == null)
            throw new IllegalArgumentException("order can not be null");
        this.iterationOrder = order;
        return this;
    }

    public ChartOrder iterationOrder() {
        return iterationOrder;
    }
}
