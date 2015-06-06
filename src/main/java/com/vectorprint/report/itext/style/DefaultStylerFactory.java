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
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.configuration.binding.parameters.ParameterizableParser;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.ImageLoader;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.debug.DebugStyler;
import com.vectorprint.report.itext.style.stylers.Advanced;
import com.vectorprint.report.itext.style.stylers.Font;
import com.vectorprint.report.itext.style.stylers.ImportPdf;
import com.vectorprint.report.itext.style.stylers.ImportTiff;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
public class DefaultStylerFactory implements StylerFactory {

   /**
    * name of the optional property containing advanced stylers that will be executed for each page
    */
   public static final String PAGESTYLERS = "PAGESTYLERS";
   /**
    * name of the optional property containing stylers that will be executed before all other stylers
    *
    * @see #DOFIRSTLAST
    */
   public static final String DEFAULTSTYLERSFIRST = "DEFAULTSTYLERSFIRST";
   /**
    * name of the optional property containing stylers that will be executed after all other stylers
    *
    * @see #DOFIRSTLAST
    */
   public static final String DEFAULTSTYLERSLAST = "DEFAULTSTYLERSLAST";
   /**
    * The default document will be A4 with margins of 25 mm when a setting {@link ReportConstants#DOCUMENTSETTINGS} is
    * not defined
    */
   public static final String DEFAULTDOCUMENTSETTINGS = "DocumentSettings(margin_top=25,margin_left=25,margin_right=25,margin_bottom=25,width=297,height=210)";
   private Map<String, String> styleSetup = new HashMap<String, String>(100);
   /**
    * put your own stylers in this package
    */
   public static final String STYLERPACKAGENAME = Font.class.getPackage().getName();
   private static final Logger log = Logger.getLogger(DefaultStylerFactory.class.getName());
   private Map<String, Object> cache = new HashMap<String, Object>(100);
   @SettingsField
   private EnhancedMap settings;
   private Document document;
   private PdfWriter writer;
   private ImageLoader imageLoader;
   private LayerManager layerManager;
   private static final ParameterizableBindingFactory BINDING_FACTORY = ParameterizableBindingFactoryImpl.getDefaultFactory();
   /**
    * name of the boolean setting use {@link #DEFAULTSTYLERSFIRST} and {@link #DEFAULTSTYLERSLAST} or not
    */
   public static final String DOFIRSTLAST = "dofirstlast";
   @Setting(keys = DOFIRSTLAST)
   private boolean doFirstLast = true;

   private ParameterizableParser getParser(StringReader sr) {
      return BINDING_FACTORY.getParser(sr);
   }

   @Override
   public DocumentStyler getDocumentStyler() throws VectorPrintException {
      String className = settings.getProperty(DEFAULTDOCUMENTSETTINGS, ReportConstants.DOCUMENTSETTINGS);
      if (settings.getBooleanProperty(Boolean.FALSE, DEBUG)) {
         styleSetup.put(ReportConstants.DOCUMENTSETTINGS, className);
         DocumentStyler ds = (DocumentStyler) getParser(new StringReader(className))
             .setSettings(settings).setPackageName(STYLERPACKAGENAME)
             .parseParameterizable();
         StylerFactoryHelper.initStylingObject(ds, writer, document, imageLoader, layerManager, settings);
         Collection<BaseStyler> c = new ArrayList<BaseStyler>(1);
         c.add((BaseStyler) ds);
         cache.put(ReportConstants.DOCUMENTSETTINGS, c);
         return ds;
      } else {
         DocumentStyler ds = (DocumentStyler) getParser(new StringReader(className))
             .setSettings(settings).setPackageName(STYLERPACKAGENAME)
             .parseParameterizable();
         StylerFactoryHelper.initStylingObject(ds, writer, document, imageLoader, layerManager, settings);
         return ds;
      }
   }

   private final List<ImportPdf> impdf = new ArrayList<ImportPdf>(1);
   private final List<ImportTiff> imtiff = new ArrayList<ImportTiff>(1);
   private final List<SimpleColumns> imcol = new ArrayList<SimpleColumns>(1);

   private <S extends BaseStyler> Collection<S> getStylers(String styleClass, String pkg)
       throws VectorPrintException {

      if (!cache.containsKey(styleClass)) {

         String[] classNames = settings.getStringProperties(null, styleClass);
         if (classNames == null) {
            classNames = new String[0];
         }
         List<S> stylers = new ArrayList<S>(classNames.length);

         for (String classNameWithParams : classNames) {
            if (classNameWithParams.isEmpty()) {
               continue;
            }

            stylers.add((S) getStyler(classNameWithParams, pkg).setStyleClass(styleClass));
         }

         cache.put(styleClass, stylers);
         return stylers;
      } else {
         return (List<S>) cache.get(styleClass);
      }

   }

   /**
    * turns a configuration like FieldFont(family=verdana) into a styler
    *
    * @param <S>
    * @param classNameWithParams
    * @param clazz
    * @param pkg
    * @return
    * @throws VectorPrintException
    */
   private <S extends BaseStyler> S getStyler(String classNameWithParams, String pkg) throws VectorPrintException {
      S st = (S) getParser(new StringReader(classNameWithParams))
             .setSettings(settings).setPackageName(pkg)
             .parseParameterizable();
      StylerFactoryHelper.initStylingObject(st, writer, document, imageLoader, layerManager, settings, (ElementProducer) imageLoader, this);
      if (st instanceof ImportPdf) {
         impdf.add((ImportPdf) st);
      } else {
         for (ImportPdf ipdf : impdf) {
            ipdf.addStyler(st);
         }
      }
      if (st instanceof SimpleColumns) {
         imcol.add((SimpleColumns) st);
      } else {
         for (SimpleColumns sc : imcol) {
            sc.addStyler(st);
         }
      }
      if (st instanceof ImportTiff) {
         imtiff.add((ImportTiff) st);
      } else {
         for (ImportTiff ipt : imtiff) {
            ipt.addStyler(st);
         }
      }
      return st;
   }

   /**
    * return a debug styler that will be appended to the stylers for providing debugging info in reports
    *
    * @param names
    * @return
    */
   private DebugStyler debugStylers(String... names) throws VectorPrintException {
      if (settings.getBooleanProperty(false, DEBUG)) {
         DebugStyler dst = new DebugStyler();
         StylerFactoryHelper.initStylingObject(dst, writer, document, null, layerManager, settings);
         for (String n : names) {
            dst.getStyleSetup().add(n);
            styleSetup.put(n, EnhancedMapBindingFactoryImpl.getDefaultFactory().getBindingHelper().serializeValue(settings.getStringProperties(null,n)));
         }

         return dst;
      }

      return null;
   }

   /**
    *
    * @param styleClasses
    * @return
    * @throws VectorPrintException
    */
   @Override
   public List<BaseStyler> getStylers(String... styleClasses) throws VectorPrintException {
      List<BaseStyler> stylers = preStyle(new ArrayList<BaseStyler>(styleClasses.length + 4), styleClasses);

      for (String name : styleClasses) {
         stylers.addAll(getStylers(name, STYLERPACKAGENAME));
      }

      postStyle(stylers, styleClasses);
      return stylers;
   }

   private <B extends BaseStyler> void debug(List<B> stylers, String... styleClasses) throws VectorPrintException {
      if (settings.getBooleanProperty(false, DEBUG)) {
         for (String clazz : styleClasses) {
            if (!cache.containsKey(clazz)) {
               cache.put(clazz, stylers);
            }
         }
         int s = (settings.containsKey(DEFAULTSTYLERSLAST))
             ? (settings.containsKey(DEFAULTSTYLERSFIRST)) ? 2 : 1
             : (settings.containsKey(DEFAULTSTYLERSFIRST)) ? 1 : 0;
         String[] styleNames = Arrays.copyOf(styleClasses, styleClasses.length + s);
         if (settings.containsKey(DEFAULTSTYLERSFIRST)) {
            styleNames[styleClasses.length] = DEFAULTSTYLERSFIRST;
         }
         if (settings.containsKey(DEFAULTSTYLERSLAST)) {
            styleNames[styleClasses.length + 1] = DEFAULTSTYLERSLAST;
         }
         DebugStyler ds = debugStylers(styleNames);

         if (ds != null) {
            stylers.add((B) ds);
         }
      }

   }

   @Override
   public Map<String, String> getStylerSetup() {
      return styleSetup;
   }

   @Override
   public void setDocument(Document document, com.itextpdf.text.pdf.PdfWriter writer) {
      this.document = document;
      this.writer = writer;
      if (settings.containsKey(PAGESTYLERS)) {
         try {
            EventHelper ph = (EventHelper) writer.getPageEvent();
            // init page stylers
            Collection<BaseStyler> p = getStylers(PAGESTYLERS, STYLERPACKAGENAME);
            for (BaseStyler s : p) {
               if (s instanceof Advanced) {
                  ph.addStylerForEachPage((Advanced) s);
               } else {
                  log.warning(s.getClass().getSimpleName() + " is not an advanced styler, cannot be used for each page");
               }
            }
            styleSetup.put(PAGESTYLERS, BINDING_FACTORY.getBindingHelper().serializeValue(settings.getStringProperties(null,PAGESTYLERS)));
         } catch (VectorPrintException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }

   @Override
   public void setImageLoader(ImageLoader imageLoader) {
      this.imageLoader = imageLoader;
   }

   @Override
   public void setLayerManager(LayerManager layerManager) {
      this.layerManager = layerManager;
   }

   private boolean containsFirstLast(String... styleClasses) {
      boolean containsFirstLast = false;
      for (String n : styleClasses) {
         if (DEFAULTSTYLERSFIRST.equals(n)) {
            containsFirstLast = true;
            break;
         }
         if (DEFAULTSTYLERSLAST.equals(n)) {
            containsFirstLast = true;
            break;
         }
      }
      return containsFirstLast;
   }

   private <B extends BaseStyler> List<B> preStyle(List<B> stylers, String... styleClasses) throws VectorPrintException {

      if (doFirstLast && !containsFirstLast(styleClasses) && settings.containsKey(DEFAULTSTYLERSFIRST)) {
         stylers.addAll((Collection<? extends B>) getStylers(DEFAULTSTYLERSFIRST, STYLERPACKAGENAME));
      }
      return stylers;
   }

   private <B extends BaseStyler> void postStyle(List<B> stylers, String... styleClasses) throws VectorPrintException {
      if (doFirstLast && !containsFirstLast(styleClasses) && settings.containsKey(DEFAULTSTYLERSLAST)) {
         stylers.addAll((Collection<? extends B>) getStylers(DEFAULTSTYLERSLAST, STYLERPACKAGENAME));
      }

      impdf.clear();
      imtiff.clear();
      imcol.clear();

      debug(stylers, styleClasses);
   }

   @Override
   public List<BaseStyler> getBaseStylersFromCache(String... styleClasses) throws VectorPrintException {
      List<BaseStyler> stylers = new ArrayList<BaseStyler>(styleClasses.length + 4);
      for (String s : styleClasses) {
         stylers.addAll((Collection<BaseStyler>) cache.get(s));
      }
      return stylers;
   }

   @Override
   public Document getDocument() {
      return document;
   }

   @Override
   public PdfWriter getWriter() {
      return writer;
   }

   @Override
   public LayerManager getLayerManager() {
      return layerManager;
   }

   public EnhancedMap getSettings() {
      return settings;
   }

}
