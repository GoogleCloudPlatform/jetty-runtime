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
package com.github.dandelion.core.web.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.cache.RequestCache;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Pre-handler intended to clear the configured {@link RequestCache} based on
 * the presence of the {@link WebConstants#DANDELION_CLEAR_CACHE} request
 * parameter.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class ClearCachePreHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(ClearCachePreHandler.class);

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public boolean isAfterChaining() {
      return false;
   }

   @Override
   public int getRank() {
      return 0;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {
      return handlerContext.getContext().getConfiguration().isToolDebuggerEnabled()
            && handlerContext.getRequest().getParameter(WebConstants.DANDELION_CLEAR_CACHE) != null;
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {
      handlerContext.getContext().getCache().clear();
      LOG.info("Cleared configured cache");
      return true;
   }
}
