/*
// $Id$
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
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
package net.hydromatic.clapham.example;

import net.hydromatic.clapham.Clapham;

import java.io.File;
import java.util.EnumSet;

/**
 * Example driver program that generates railroad diagrams for Wirth's grammar.
 *
 * @author jhyde
 * @since 2009/5/9
 */
public class WirthExample {
    /**
     * Command-line entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException(
                "Must specify file name");
        }
        String grammarFileName = args[0];
        final File grammarFile = new File(grammarFileName);
        final Clapham clapham = new Clapham();
        clapham.load(grammarFile, null);
        clapham.setOutputFormats(
            EnumSet.of(Clapham.ImageFormat.PNG, Clapham.ImageFormat.SVG));
        clapham.drawAll();
        clapham.generateIndex();
    }

    /**
     * Test case for WirthExample. To be completed.
     */
    public static class GrammarImplTest /* extends TestCase */ {
        public void testLoadNonexistentFileFails() {
            // call load(File) with bad file
        }

        public void testDrawBeforeCallingLoadFails() {
            // create a grammar, do not call load, then call draw
        }

        public void testDrawNonexistentSymbolFails() {

        }
    }
}

// End WirthExample.java
