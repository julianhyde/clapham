/*
// $Id$
// Clapham generates railroad diagrams to represent computer language grammars.
// Copyright (C) 2008-2009 Julian Hyde
// Copyright (c) 2005 Stefan Schoergenhumer, Markus Dopler
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
package net.hydromatic.clapham.graph;

import net.hydromatic.clapham.parser.ProductionNode;

/**
 * TODO:
 *
 * @author jhyde
 * @version $Id$
 * @since Jul 30, 2008
 */
public class Symbol {

    public final NodeType typ;         // t, nt
    public final String   name;        // symbol name
    public Graph    graph;       // nt: to first node of syntax graph
    /**The production that generate this symbol*/
    private ProductionNode production;

    public Symbol(NodeType typ, String name) {
        if (name.length() == 0) {
            System.out.println("empty token not allowed");
            name = "???";
        }
        this.typ = typ;
        this.name = name;
    }
        
    public ProductionNode getProduction() {
		return production;
	}
    
    public Symbol withProduction(ProductionNode production) {
		this.production = production;
		return this;
	}
}

// End Symbol.java
