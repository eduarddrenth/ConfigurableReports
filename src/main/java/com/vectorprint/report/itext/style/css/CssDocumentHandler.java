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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.binding.parameters.ParameterizableSerializer;
import com.vectorprint.configuration.binding.settings.EnhancedMapSerializer;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.FormFieldStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.stylers.Border;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import com.vectorprint.report.itext.style.stylers.Padding;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * Use an instance of this class in {@link Parser#setDocumentHandler(org.w3c.css.sac.DocumentHandler) }, call {@link Parser#parseStyleSheet(org.w3c.css.sac.InputSource)
 * }. After this you can call {@link #printStylers(java.io.OutputStream) } to write a stylesheet suitable for a
 * {@link StylerFactory}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class CssDocumentHandler implements CssToBaseStylers {

   /**
    * each entry is a map for a style class containing another map that contains a List of stylers for css properties
    */
   private final Map<String, Collection<BaseStyler>> cssStylers = new HashMap<String, Collection<BaseStyler>>(10);

   private static final Logger LOGGER = Logger.getLogger(CssDocumentHandler.class.getName());
   
   private Boolean mustFindStylersForCssNames = true;

   public void setMustFindStylersForCssNames(Boolean mustFindStylersForCssNames) {
      if (mustFindStylersForCssNames!=null) {
         this.mustFindStylersForCssNames = mustFindStylersForCssNames;
      }
   }
   
   private Collection<BaseStyler> getOrCreate(String cssClass) {
      if (!cssStylers.containsKey(cssClass)) {
         cssStylers.put(cssClass, new ArrayList<BaseStyler>(3));
      }
      return cssStylers.get(cssClass);
   }

   @Override
   public void startDocument(InputSource is) throws CSSException {
      cssStylers.clear();
      styling = null;
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine("start doc");
      }
   }

   @Override
   public void endDocument(InputSource is) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine("end doc");
      }
   }

   @Override
   public void comment(String string) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "comment {0}", string);
      }
   }

   @Override
   public void ignorableAtRule(String string) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "rule {0}", string);
      }
   }

   @Override
   public void namespaceDeclaration(String string, String string1) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "namespace {0} {1}", new Object[]{string, string1});
      }
   }

   @Override
   public void importStyle(String string, SACMediaList sacml, String string1) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "import {0}", string);
      }
   }

   @Override
   public void startMedia(SACMediaList sacml) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "start media {0}", sacml.toString());
      }
   }

   @Override
   public void endMedia(SACMediaList sacml) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "end media {0}", sacml.toString());
      }
   }

   @Override
   public void startPage(String string, String string1) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "start page {0} {1}", new Object[]{string, string1});
      }
   }

   @Override
   public void endPage(String string, String string1) throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.log(Level.FINE, "end page {0} {1}", new Object[]{string, string1});
      }
   }

   @Override
   public void startFontFace() throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine("start fontface");
      }
   }

   @Override
   public void endFontFace() throws CSSException {
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine("end fontface");
      }
   }

   private SelectorList current = null;

   /**
    * remembers the current list
    * @param sl
    * @throws CSSException 
    */
   @Override
   public void startSelector(SelectorList sl) throws CSSException {
      current = sl;
   }

   /**
    * forget the current list
    * @param sl
    * @throws CSSException 
    */
   @Override
   public void endSelector(SelectorList sl) throws CSSException {
      current = null;
   }

   /**
    * for each {@link Selector} in the current list, call {@link #toBaseStylerParam(org.w3c.css.sac.Selector, java.lang.String, org.w3c.css.sac.LexicalUnit) }
    * @param string
    * @param lu
    * @param bln
    * @throws CSSException 
    */
   @Override
   public void property(String string, LexicalUnit lu, boolean bln) throws CSSException {
      for (int i = 0; i < current.getLength(); i++) {
         toBaseStylerParam(current.item(i), string, lu);
      }
   }

   @Override
   public String getClassFromSelector(Selector selector) {
      if (selector.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
         Condition c = ((ConditionalSelector) selector).getCondition();
         if (c.getConditionType() == Condition.SAC_CLASS_CONDITION) {
            AttributeCondition a = (AttributeCondition) c;
            // assume this will be the class attribute
            return a.getValue();
         } else {
            LOGGER.log(Level.WARNING, "condition type not supported: {0} in {1}", new Object[]{c.getConditionType(), c});
         }
      } else {
         LOGGER.log(Level.WARNING, "selector type not supported: {0} in {1}", new Object[]{selector.getSelectorType(), selector});
      }
      return null;
   }

   @Override
   public Collection<BaseStyler> toBaseStylerParam(Selector selector, String key, LexicalUnit value) {
      String cssClass = null;
      if (key.equals("margin")) {
         LOGGER.log(Level.WARNING, "forcing css class to {0} for {1}", new String[]{ReportConstants.DOCUMENTSETTINGS, key});
         cssClass = ReportConstants.DOCUMENTSETTINGS;
      } else {
         cssClass = getClassFromSelector(selector);
      }
      if (cssClass == null) {
         return null;
      }
      Collection<BaseStyler> stylers = new ArrayList<BaseStyler>(1);
      for (BaseStyler bss : getOrCreate(cssClass)) {
         if (!bss.findForCssProperty(key).isEmpty()) {
            stylers.add(bss);
         }
      }
      boolean toAdd = false;
      if (stylers.isEmpty()) {
         try {
            stylers = StylerFactoryHelper.findForCssName(key,mustFindStylersForCssNames);
            toAdd = true;
         } catch (ClassNotFoundException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IOException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (NoSuchMethodException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InvocationTargetException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
         if (stylers.isEmpty()) {
            return stylers;
         }
      }
      List<Padding> pToAdd = new ArrayList<Padding>(0) {

         @Override
         public boolean add(Padding e) {
            if (e!=null) {
               return super.add(e);
            } else {
               return false;
            }
         }
         
      };
      if ("padding".equals(key)) {
         // TODO if we have padding we need 1 to 4 Padding stylers depending on the number of paddings set in the combined css padding.
         // Add them here, prepare Padding#whichPadding, interact with fillParameters....?
         stylers.clear();
         for (Iterator<BaseStyler> it = getOrCreate(cssClass).iterator(); it.hasNext();) {
            BaseStyler bss = it.next();
            if (!bss.findForCssProperty(key).isEmpty()) {
               it.remove();
            }
         }
         switch (getUnits(value).size()) {
            case 1:// trbl same
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.TRBL,stylers));
               break;
            case 2:// tb - lr, need two stylers
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.BT,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.LR,stylers));
               break;
            case 3:// t - lr - b, need three stylers
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.TOP,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.LR,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.BOTTOM,stylers));
               break;
            case 4:// trbl different, need four stylers
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.TOP,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.RIGHT,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.BOTTOM,stylers));
               pToAdd.add(getOrAddPadding(BaseStyler.POSITION.LEFT,stylers));
               break;
         }
      } else if ("padding-top".equals(key)) {
         pToAdd.add(getOrAddPadding(BaseStyler.POSITION.TOP, stylers));
      } else if ("padding-right".equals(key)) {
         pToAdd.add(getOrAddPadding(BaseStyler.POSITION.RIGHT, stylers));
      } else if ("padding-bottom".equals(key)) {
         pToAdd.add(getOrAddPadding(BaseStyler.POSITION.BOTTOM, stylers));
      } else if ("padding-left".equals(key)) {
         pToAdd.add(getOrAddPadding(BaseStyler.POSITION.LEFT, stylers));
      }
      if (toAdd) {
         getOrCreate(cssClass).addAll(stylers);
      } else if (!pToAdd.isEmpty()) {
         getOrCreate(cssClass).addAll(pToAdd);
      }
      for (BaseStyler bs : stylers) {
         if (bs instanceof FormFieldStyler) {
            LOGGER.warning(String.format("skipping %s, FormFieldStylers not supported yet",bs.getClass().getName()));
            continue;
         }
         Collection<Parameter> ps = bs.findForCssProperty(key);
         if (ps.size() == 1) {
            Parameter p = ps.iterator().next();
            if (value.getFloatValue() > 0) {
               p.setValue(getPoints(value));
            } else if ("list-style-type".equals(key)) {
               String listType = value.getStringValue();
               if ("lower-alpha".equals(listType)) {
                  p.setValue("ALPHA");
               } else if ("decimal".equals(listType)) {
                  p.setValue("NUMERIC");
               }
            } else if ("text-align".equals(key)) {
               String align = value.getStringValue();
               if (p.getValue() != null) {
                  p.setValue(BaseStyler.ALIGN.forVertical((BaseStyler.ALIGN) p.getValue(), align));
               } else {
                  p.setValue(BaseStyler.ALIGN.valueOf((align + '_' + BaseStyler.ALIGN.BOTTOM).toUpperCase()));
               }
            } else if ("vertical-align".equals(key)) {
               String align = value.getStringValue();
               if (p.getValue() != null) {
                  p.setValue(BaseStyler.ALIGN.forHorizontal((BaseStyler.ALIGN) p.getValue(), align));
               } else {
                  p.setValue(BaseStyler.ALIGN.valueOf((BaseStyler.ALIGN.LEFTPART + '_' + align).toUpperCase()));
               }
            } else {
               p.setValue((Serializable) ParamBindingService.getInstance().getFactory().getBindingHelper().convert(fromLexicalUnit(value),p.getValueClass()));
            }
         } else {
            fillParameters(bs, ps, key, value);
         }
      }
      return stylers;
   }

   
   private Padding getOrAddPadding(BaseStyler.POSITION position, Collection<BaseStyler> stylers) {
      for (BaseStyler bs : stylers) {
         if (position.equals(((Padding)bs).getWhichPadding())) {
            return null;
         }
      }
      Padding p = new Padding();
      p.setWhichPadding(position);
      stylers.add(p);
      return p;
   }
   
   private void setPadding(LexicalUnit lu, Padding p, BaseStyler.POSITION position) {
      if (position.equals(p.getWhichPadding())) {
         p.setPadding(getPoints(lu));
      }
   }

   @Override
   public void fillParameters(BaseStyler bs, Collection<Parameter> params, String key, LexicalUnit value) {
      if ("border".equals(key) && getPoints(value) != null) {
         // TODO values in css border are not required, need more intelligence here
         Border b = (Border) bs;
         b.setValue(Border.BORDERWIDTH, getPoints(value));
         b.setValue(BaseStyler.COLOR_PARAM, (Serializable) ParamBindingService.getInstance().getFactory().getBindingHelper().convert(fromLexicalUnit(value.getNextLexicalUnit().getNextLexicalUnit()),Color.class));
         LOGGER.warning(String.format("ignoring %s", value.getNextLexicalUnit().toString()));
         return;
      } else if ("margin".equals(key)) {
         DocumentSettings ds = (DocumentSettings) bs;
         List<LexicalUnit> l = getUnits(value);
         Float f = getPoints(value);
         ds.setMargin_top(f);
         ds.setMargin_bottom((l.size() == 1 || l.size() == 2) ? f : getPoints(l.get(2)));
         ds.setMargin_right((l.size() != 1) ? getPoints(l.get(1)) : f);
         switch (l.size()) {
            case 2:// tb - lr
            case 3:// t - lr - b
               f = getPoints(l.get(1));
               break;
            case 4:// trbl
               f = getPoints(l.get(3));
               break;
         }
         ds.setMargin_left(f);
         return;
      } else if ("padding".equals(key)) {
         Padding p = (Padding) bs;
         List<LexicalUnit> l = getUnits(value);
         switch (l.size()) {
            case 1:// trbl same
               setPadding(l.get(0), p, BaseStyler.POSITION.TRBL);
               break;
            case 2:// tb - lr, need two stylers
               setPadding(l.get(0), p, BaseStyler.POSITION.BT);
               setPadding(l.get(1), p, BaseStyler.POSITION.LR);
               break;
            case 3:// t - lr - b, need three stylers
               setPadding(l.get(0), p, BaseStyler.POSITION.TOP);
               setPadding(l.get(1), p, BaseStyler.POSITION.LR);
               setPadding(l.get(2), p, BaseStyler.POSITION.BOTTOM);
               break;
            case 4:// trbl different, need four stylers
               setPadding(l.get(0), p, BaseStyler.POSITION.TOP);
               setPadding(l.get(1), p, BaseStyler.POSITION.RIGHT);
               setPadding(l.get(2), p, BaseStyler.POSITION.BOTTOM);
               setPadding(l.get(3), p, BaseStyler.POSITION.LEFT);
               break;
         }
         return;
      } else if ("padding-top".equals(key)) {
         setPadding(value, (Padding) bs, BaseStyler.POSITION.TOP);
         return;
      } else if ("padding-right".equals(key)) {
         setPadding(value, (Padding) bs, BaseStyler.POSITION.RIGHT);
         return;
      } else if ("padding-bottom".equals(key)) {
         setPadding(value, (Padding) bs, BaseStyler.POSITION.BOTTOM);
         return;
      } else if ("padding-left".equals(key)) {
         setPadding(value, (Padding) bs, BaseStyler.POSITION.LEFT);
         return;
      }

      // TODO special cases where multiple parameters take care of implementing one cssProperty
      throw new UnsupportedOperationException(String.format("Not supported yet to use %s for implementing %s defined by %s in %s", params, value, key, bs.getClass().getSimpleName()));
   }

   private List<LexicalUnit> getUnits(LexicalUnit start) {
      List<LexicalUnit> l = new ArrayList<LexicalUnit>(1);
      l.add(start);
      while ((start = start.getNextLexicalUnit()) != null) {
         l.add(start);
      }
      return l;
   }

   @Override
   public Float getPoints(LexicalUnit lu) {
      switch (lu.getLexicalUnitType()) {
         case LexicalUnit.SAC_CENTIMETER:
            return ItextHelper.mmToPts(lu.getFloatValue() * 10);
         case LexicalUnit.SAC_INCH:
            return ItextHelper.round(lu.getFloatValue()/72f, 3);
         case LexicalUnit.SAC_MILLIMETER:
            return ItextHelper.mmToPts(lu.getFloatValue());
         case LexicalUnit.SAC_POINT:
         case LexicalUnit.SAC_PIXEL: // take point for pixel
            return lu.getFloatValue();
         default:
            return null;
      }
   }

   @Override
   public String fromLexicalUnit(LexicalUnit lu) {
      if (lu.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR) {
         // we have rgb(x,y,z) here
         LexicalUnit r = lu.getParameters();
         LexicalUnit g = r.getNextLexicalUnit().getNextLexicalUnit();
         LexicalUnit b = g.getNextLexicalUnit().getNextLexicalUnit();
         return '#' + Integer.toHexString((int) r.getFloatValue()) + Integer.toHexString((int) g.getFloatValue()) + Integer.toHexString((int) b.getFloatValue());
      } else if (lu.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE) {
         return lu.getStringValue();
      } else if (lu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
         return lu.getStringValue();
      }
      return lu.toString();
   }

   @Override
   public Collection<BaseStyler> getStylersFound(Selector selector) {
      return getStylersFound(getClassFromSelector(selector));
   }

   private Collection<BaseStyler> getStylersFound(String selector) {
      return getOrCreate(selector);
   }
   

   @Override
   public void printStylers(OutputStream os) throws IOException {
      EnhancedMapSerializer ems = SettingsBindingService.getInstance().getFactory().getSerializer();
      EnhancedMap settings = new Settings(cssStylers.size());

      for (Map.Entry<String, Collection<BaseStyler>> e : cssStylers.entrySet()) {
         List<String> config = new ArrayList<String>(e.getValue().size());
         for (BaseStyler b : e.getValue()) {
            StringWriter sw = new StringWriter();
            ParameterizableSerializer ps = ParamBindingService.getInstance().getFactory().getSerializer();
            ps.serialize(b, sw);
            config.add(sw.toString());
         }
         settings.put(e.getKey(), config.toArray(new String[config.size()]));
      }
      
      OutputStreamWriter osw = new OutputStreamWriter(os);
      ems.serialize(settings, osw);
   }

   @Override
   public EnhancedMap getStyling() throws IOException {
      if (styling != null) {
         return styling;
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      printStylers(out);
         styling = new ParsingProperties(new Settings(),new StringReader(out.toString()));
         return styling;
   }

   EnhancedMap styling;
}
