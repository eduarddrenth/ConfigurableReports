package com.vectorprint.report.itext.style.css;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2013 VectorPrint
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

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Selector;

/**
 * How to get from css to {@link BaseStyler}s.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface CssToBaseStylers extends DocumentHandler{
   
   /**
    * Each selector in {@link DocumentHandler#startSelector(org.w3c.css.sac.SelectorList) the list of css selectors} should map
    * to a {@link StylerFactory#getStylers(java.lang.String...) set of stylers} that are responsible for the implementation of
    * {@link DocumentHandler#property(java.lang.String, org.w3c.css.sac.LexicalUnit, boolean) css properties}.
    * @see #fillParameters(com.vectorprint.report.itext.style.BaseStyler, java.util.Collection, java.lang.String, org.w3c.css.sac.LexicalUnit) 
    * @param cssClass
    * @param key
    * @param value
    * @return 
    */
   Collection<BaseStyler> toBaseStylerParam(Selector cssClass, String key, LexicalUnit value);
   
   /**
    * Should yield a collection of BaseStylers found during parsing css for a given Selector.
    * @param selector
    * @return 
    */
   Collection<BaseStyler> getStylersFound(Selector selector);
   
   /**
    * Print the BaseStylers to a stylesheet for use by a {@link StylerFactory} after conversion of a css stylesheet to a collection of BaseStylers.
    * @see EnhancedMapBindingFactory
    * @see ParameterizableBindingFactory
    * @param os
    * @throws IOException 
    */
   void printStylers(OutputStream os) throws IOException;
   
   /**
    * turn a parsed css stylesheet into settings suitable for report styling.
    * @return
    * @throws IOException 
    */
   EnhancedMap getStyling() throws IOException;
   
   /**
    * Turn a css Selector into a className, return null when the Selector is not supported
    * @param selector
    * @return 
    */
   public String getClassFromSelector(Selector selector);
   
   /**
    * Use multiple parameters for the implementation of a css property, see {@link BaseStyler#findForCssProperty(java.lang.String) }.
    *
    * @param bs
    * @param params
    * @param key
    * @param value
    */
   public void fillParameters(BaseStyler bs, Collection<Parameter> params, String key, LexicalUnit value);

   /**
    * Convert a lexical unit into a float (conversion to points (72 points in one inch)), applicable for {@link LexicalUnit#SAC_MILLIMETER}, {@link LexicalUnit#SAC_PIXEL} etc.
    * @param lu
    * @return 
    */
   public Float getPoints(LexicalUnit lu);

   /**
    * Construct a String to use in {@link ParamBindingHelper#convert(java.lang.String, java.lang.Class)  }. Probably the most messy part of the SAC
    * api, becasue of composed css properties, because of functions for example rgb(r,g,b). It is often necessary to
    * debug in order to find the values you need to convert to String that can be used by {@link ParamBindingHelper#convert(java.lang.String, java.lang.Class) }.
    *
    * @param lu
    * @return
    */
   public String fromLexicalUnit(LexicalUnit lu);
}
