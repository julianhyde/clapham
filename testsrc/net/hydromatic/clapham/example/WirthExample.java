/*
// $Id: $
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
