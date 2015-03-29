
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.stylers;

/*
 * #%L
 * VectorPrintReport4.0
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

//~--- non-JDK imports --------------------------------------------------------

import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.report.itext.ImageLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Settings for lists, numbered, alpha, bullet,...
 * @author Eduard Drenth at VectorPrint.nl
 */
public class List extends ListItem  {

   public List(ImageLoader imageLoader, EnhancedMap settings) throws VectorPrintException {
      super(imageLoader, settings);
      initParams();
   }
   
   public enum LISTTYPE {SYMBOL, ALPHA, NUMERIC}

   public static final String POSTSYMBOL_PARAM = "postfix";
   public static final String PRESYMBOL_PARAM = "prefix";
   public static final String LISTTYPE_PARAM = "listtype";
   
   private void initParams() {
      addParameter(new StringParameter(POSTSYMBOL_PARAM, "String to use as postfix for list items").setDefault(""),List.class);
      addParameter(new StringParameter(PRESYMBOL_PARAM, "String to use as prefix for list items").setDefault(""),List.class);
      addParameter(new StringParameter(LISTTYPE_PARAM, "type of list: " + Arrays.asList(LISTTYPE.values())).setDefault(LISTTYPE.NUMERIC.name()),List.class);
   }

   public List() {
      initParams();
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      text = super.style(text, data);
      com.itextpdf.text.List l = (com.itextpdf.text.List)text;
      l.setPostSymbol(getValue(POSTSYMBOL_PARAM, String.class));
      l.setPreSymbol(getValue(PRESYMBOL_PARAM, String.class));
      l.setNumbered(LISTTYPE.NUMERIC.name().equalsIgnoreCase(getValue(LISTTYPE_PARAM, String.class)));
      l.setLettered(LISTTYPE.ALPHA.name().equalsIgnoreCase(getValue(LISTTYPE_PARAM, String.class)));
      return text;
   }
   private static final Class<Object>[] classes = new Class[]{com.itextpdf.text.List.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   @Override
   public String getHelp() {
      return "Specify the looks of a list. " + super.getHelp(); 
   }
}
