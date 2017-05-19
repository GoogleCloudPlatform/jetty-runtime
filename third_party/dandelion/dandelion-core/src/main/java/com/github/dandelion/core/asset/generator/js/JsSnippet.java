/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors 
 * may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.asset.generator.js;

/**
 * <p>
 * Bean used to hold Javascript code (function or any other Javascript object)
 * that must not be converted into String by a JSON parser.
 * </p>
 * <p>
 * Overriding the toString() method allow the parser to return the Javascript
 * content as an Object, resulting in a non-quoted String in JSON format.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.8.2
 */
public class JsSnippet {

   private String javascript;

   public JsSnippet(String javascript) {
      this.javascript = javascript;
   }

   @Override
   public String toString() {
      return javascript;
   }

   public String getJavascript() {
      return this.javascript;
   }

   public void append(String javascript) {
      if (this.javascript == null) {
         this.javascript = javascript;
      }
      else {
         this.javascript += javascript;
      }
   }

   public void setJavascript(String javascript) {
      this.javascript = javascript;
   }

   @Override
   public boolean equals(Object javascriptSnippet) {
      if (javascriptSnippet != null && javascriptSnippet instanceof JsSnippet) {
         return this.javascript.equals(((JsSnippet) javascriptSnippet).getJavascript());
      }
      else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.javascript.hashCode();
   }
}