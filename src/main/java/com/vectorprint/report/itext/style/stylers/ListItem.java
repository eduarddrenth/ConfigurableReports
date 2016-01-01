
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

import com.itextpdf.text.Chunk;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.URLParameter;
import com.vectorprint.report.itext.ImageLoader;
import com.vectorprint.report.itext.ImageLoaderAware;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Settings for listItems, numbered, alpha, bullet,...
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ListItem extends Font implements ImageLoaderAware {
   private ImageLoader imageLoader;

   @Override
   public void setImageLoader(ImageLoader imageLoader) {
      this.imageLoader = imageLoader;
   }

   public ListItem(ImageLoader imageLoader, EnhancedMap settings) throws VectorPrintException {
      StylerFactoryHelper.initStylingObject(this, null, null, imageLoader, null, settings);
      initParams();
   }
   
   public static final String SYMBOL_PARAM = "symbol";
   public static final String SYMBOLIMAGE_PARAM = "symbolimage";
   
   private void initParams() {
      addParameter(new StringParameter(SYMBOL_PARAM, String.format("text to use as symbol for a list with symbols")).setDefault("- "),ListItem.class);
      addParameter(new URLParameter(SYMBOLIMAGE_PARAM, String.format("image to use as symbol for a list with symbols")),ListItem.class);
   }

   public ListItem() {
      initParams();
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      Chunk symbolChunk = new Chunk(getValue(SYMBOL_PARAM, String.class));
      if (getValue(SYMBOLIMAGE_PARAM, URL.class)!=null) {
         symbolChunk = new Chunk(imageLoader.loadImage(getValue(SYMBOLIMAGE_PARAM, URL.class), 1), 0, 0);
      }
      if (text instanceof com.itextpdf.text.List) {
         com.itextpdf.text.List l = (com.itextpdf.text.List)text;
         l.setListSymbol(super.style(symbolChunk, data));
      } else {
         com.itextpdf.text.ListItem l = (com.itextpdf.text.ListItem)text;
         l.setListSymbol(super.style(symbolChunk, data));
      }
      return text;
   }
   private static final Class<Object>[] classes = new Class[]{com.itextpdf.text.ListItem.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   @Override
   public String getHelp() {
      return "Specify the looks of an item in a list." + " " + super.getHelp();
   }
}
