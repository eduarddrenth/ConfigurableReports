package com.vectorprint.report.itext.style;

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
import com.vectorprint.report.itext.DocumentAware;
import com.vectorprint.report.itext.ImageLoaderAware;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.ClassHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.ElementProducing;
import com.vectorprint.report.itext.ImageLoader;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.LayerManagerAware;
import com.vectorprint.report.itext.style.stylers.Font;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StylerFactoryHelper {
   public static final SettingsAnnotationProcessor SETTINGS_ANNOTATION_PROCESSOR = new SettingsAnnotationProcessorImpl();
   static {
      Logger.getLogger(SettingsAnnotationProcessorImpl.class.getName()).setLevel(Level.SEVERE);
   }
       
   public static final Logger LOGGER = Logger.getLogger(StylerFactoryHelper.class.getName());
   /**
    *
    * @param s
    * @param writer
    * @param document
    * @param imageLoader
    * @param layerManager
    * @param settings
    * @param elementProducer
    * @param stylerFactory
    * @throws VectorPrintException
    */
   public static void initStylingObject(Object s, PdfWriter writer, Document document, ImageLoader imageLoader, LayerManager layerManager, EnhancedMap settings, ElementProducer elementProducer, StylerFactory stylerFactory) throws VectorPrintException {
      if (s instanceof DocumentAware) {
         ((DocumentAware) s).setDocument(document, writer);
      }
      if (s instanceof ImageLoaderAware) {
         ((ImageLoaderAware) s).setImageLoader(imageLoader);
      }
      if (s instanceof LayerManagerAware) {
         ((LayerManagerAware) s).setLayerManager(layerManager);
      }
      if (s instanceof ElementProducing) {
         ((ElementProducing)s).setElementProducer(elementProducer);
         ((ElementProducing)s).setStylerFactory(stylerFactory);
      }
   }
   
   public static void initStylingObject(Object s, PdfWriter writer, Document document, ImageLoader imageLoader, LayerManager layerManager, EnhancedMap settings) throws VectorPrintException {
      initStylingObject(s, writer, document, imageLoader, layerManager, settings, null, null);
   }
   
   /**
    * Call {@link #findForCssName(java.lang.String, boolean) } with false 
    */
   public static Collection<BaseStyler> findForCssName(String cssName) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
         return findForCssName(cssName, false);
   }

   /**
    * find BaseStylers that implements a css property.
    * @param cssName
    * @param required when true throw an illegalargumentexception when no styler is found for the css name
    * @see BaseStyler#findForCssProperty(java.lang.String)
    * @throws ClassNotFoundException
    * @throws IOException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @return the java.util.Collection<com.vectorprint.report.itext.style.BaseStyler>
    */
   public static Collection<BaseStyler> findForCssName(String cssName, boolean required) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Collection<BaseStyler> stylers = new ArrayList<BaseStyler>(1);
      for (Class<?> c : ClassHelper.fromPackage(Font.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers())&&BaseStyler.class.isAssignableFrom(c)) {
            BaseStyler bs = (BaseStyler) c.newInstance();
            if (bs.findForCssProperty(cssName) != null && !bs.findForCssProperty(cssName).isEmpty()) {
               stylers.add(bs);
               if (LOGGER.isLoggable(Level.FINE)) {
                  LOGGER.fine(String.format("found %s supporting css property %s", cssName, bs.getClass().getName()));
               }
            }
         }
      }
      if (stylers.isEmpty()) {
         if (required) {
            throw new IllegalArgumentException(String.format("no styler supports css property %s", cssName));
         } else {
            LOGGER.warning(String.format("no styler supports css property %s", cssName));
         }
      }
      return stylers;
   }

}
