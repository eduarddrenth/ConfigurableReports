/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.stylers;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2014 VectorPrint
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.URLParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Styler to configure links and targets in your pdf
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters(
    parameters = {
       @Param(
           key = Image.URLPARAM,
           clazz = URLParameter.class,
           help = "an external destination to open"
       ),
       @Param(
           key = Link.ANCHOR,
           clazz = StringParameter.class,
           help = "an internal anchor to assign"
       ),
       @Param(
           key = Link.GOTO,
           clazz = StringParameter.class,
           help = "an internal destination to goto"
       )
    }
)
public class Link extends AbstractStyler  {

   /**
    * key of the parameter to set an internal goto
    */
   public static final String GOTO = "goto";
   /**
    * key of the parameter to set an internal destination
    */
   public static final String ANCHOR = "anchor";

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      if (isParameterSet(Image.URLPARAM)) {
         ((Chunk) element).setAnchor(getValue(Image.URLPARAM, URL.class));
      } else if (isParameterSet(GOTO)) {
         ((Chunk) element).setLocalGoto(getValue(GOTO, String.class));
      } else if (isParameterSet(ANCHOR)) {
         ((Chunk) element).setLocalDestination(getValue(ANCHOR, String.class));
      }
      return element;
   }

   private static final Class<Object>[] classes = new Class[]{Chunk.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

}
