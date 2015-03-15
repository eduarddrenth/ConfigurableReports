
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
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.VectorPrintProperties;
import com.vectorprint.configuration.annotation.Settings;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.observing.TrimKeyValue;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterHelper;
import com.vectorprint.configuration.parameters.ParameterizableImpl;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;
import com.vectorprint.configuration.parser.ObjectParser;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.configuration.parser.TokenMgrError;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.DocumentAware;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import static com.vectorprint.report.itext.style.StylerFactoryHelper.LOGGER;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.conditions.PageNumberCondition;
import static com.vectorprint.report.itext.style.stylers.StylerHelper.supported;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * Baseclass for stylers providing support for some general functionality such as parameters, conditions, help and
 * Element class support.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters(
    parameters = {
       @Param(
           key = AbstractStyler.CONDITONS,
           help = "each element should point to a property declaring one or more styling conditions)",
           clazz = StringParameter.class),
       @Param(
           key = AbstractStyler.STYLEAFTER ,
           help = "call the style method after an element is added to the document, default is to call it before addition.",
           clazz = BooleanParameter.class,
           defaultValue = "false")
    })
public abstract class AbstractStyler extends ParameterizableImpl implements BaseStyler, DocumentAware {

   protected static final Logger log = Logger.getLogger(AbstractStyler.class.getName());
   /**
    * config key used when none is present in configuration
    *
    * @see StyleHelper#getConditionConfig(java.util.Collection)
    * @see StylingCondition#getConfigKey()
    */
   public static final String NOT_FROM_CONFIGURATION = "not from configuration";
   @Settings
   private EnhancedMap settings;
   public static final String CONDITONS = "conditions";
   private java.util.List<StylingCondition> conditions = new ArrayList<StylingCondition>(1);
   private Document document;
   private PdfWriter writer;
   private String styleClass;
   protected ItextHelper itextHelper;
   /**
    * 
    */
   public static final String STYLEAFTER = "styleafteradding";

   @Override
   public Document getDocument() {
      return document;
   }

   @Override
   public PdfWriter getWriter() {
      return writer;
   }

   public AbstractStyler(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      StylerFactoryHelper.initStylingObject(this, writer, document, null, null, settings);
   }

   public AbstractStyler() {
   }

   /**
    * returns true when element is null and creates() return true, or when element is assignable from one of the classes
    * in getClassesToStyle().
    *
    * @param element
    * @return
    */
   @Override
   public boolean canStyle(Object element) {
      if (element == null) {
         return creates();
      } else {
         return supported(getSupportedClasses(), element);
      }
   }

   @Override
   public boolean creates() {
      return false;
   }

   /**
    * returns false when a condition is present for which {@link StylingCondition#shouldStyle(java.lang.Object) }
    * is false
    *
    * @param data
    * @param element the value of element
    * @return
    */
   @Override
   public boolean shouldStyle(Object data, Object element) {
      for (StylingCondition sc : conditions) {
         if (!sc.shouldStyle(data, null)) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void addCondition(StylingCondition condition) {
      conditions.add(condition);
   }
   
   private void initConditions() throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException, VectorPrintException, ClassNotFoundException, ParseException {
      String[] conditions = settings.getStringProperties(getValue(CONDITONS, String.class), new String[]{});
      if (conditions.length == 0) {
         throw new VectorPrintException("looked for condition definitions but none found in the settings");
      } else {
         for (String sc : conditions) {
            StylingCondition scn = new ObjectParser(new StringReader(sc)).
                parse(
                    PageNumberCondition.class.getPackage().getName(),
                    settings,
                    StylingCondition.class).setConfigKey(getValue(CONDITONS, String.class));
            StylerFactoryHelper.initStylingObject(
                scn,
                writer,
                document,
                null,
                null,
                settings);

            addCondition(scn);
         }
      }
   }

   @Override
   public void setDocument(Document document, PdfWriter writer) {
      this.document = document;
      this.writer = writer;
      for (StylingCondition condition : this.conditions) {
         if (condition instanceof DocumentAware) {
            ((DocumentAware) condition).setDocument(document, writer);
         }
      }
   }

   @Override
   public String getHelp() {
      return "Supports conditional styling and limiting styling to certain iText classes.";
   }

   @Override
   public String getStyleClass() {
      return styleClass;
   }

   @Override
   public BaseStyler setConfigKey(String styleClass) {
      this.styleClass = styleClass;
      return this;
   }

   @Override
   public java.util.List<StylingCondition> getConditions() {
      return conditions;
   }

   /**
    * initialize a styler from defaults or arguments. Defaults are searched in {@link EnhancedMap properties} using the
    * concatenation of {@link Class#getSimpleName() }, a "." and the {@link #getParameterInfo() name of a setting} for
    * the styler. Subclasses of {@link AbstractStyler} will be searched starting with the actual class, ending with the
    * direct subclass of {@link AbstractStyler}.
    *
    * @param args
    */
   @Override
   public void setup(Map<String, String> args, Map<String, String> settings) {
      itextHelper = new ItextHelper();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(itextHelper, this.settings);
      super.setup(args, settings);
      if (getParameter(CONDITONS, String.class) != null && getParameter(CONDITONS, String.class).getValue() != null) {
         try {
            initConditions();
         } catch (TokenMgrError ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (ClassNotFoundException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (NoSuchFieldException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalArgumentException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (VectorPrintException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (ParseException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (RuntimeException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      // fix programmatically added conditions
      for (StylingCondition condition : this.conditions) {
         if (condition.getSettings() == null) {
            StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(condition, this.settings);
         }
         if (condition.getConfigKey() == null) {
            condition.setConfigKey(NOT_FROM_CONFIGURATION);
         }
      }
   }

   @Override
   public String toConfig() {
      return ParameterHelper.toConfig(this, true);
   }

   private static volatile EnhancedMap cssNames;

   public static Map<String, String> loadCssNames() {
      EnhancedMap em = cssNames;
      if (em == null) {
         synchronized (AbstractStyler.class) {
            em = cssNames;
            if (em == null) {
               try {
                  cssNames = em = new ParsingProperties(new PreparingProperties(new VectorPrintProperties(),StylerHelper.toList(new TrimKeyValue())),
                      new InputStreamReader(AbstractStyler.class.getResourceAsStream(CSS_NAMESPROPERTIES) ));
               } catch (IOException ex) {
                  throw new VectorPrintRuntimeException(ex);
               } catch (ParseException ex) {
                  throw new VectorPrintRuntimeException(ex);
               }
            }
         }
      }
      return em;
   }

   @Override
   public String getCssEquivalent(Parameter parameter) {
      loadCssNames();
      return cssNames.get(getClass().getSimpleName() + '.' + parameter.getKey());
   }
   public static final String CSS_NAMESPROPERTIES = "/cssNames.properties";

   private static final String[] EMPTY = new String[]{};

   @Override
   public Collection<Parameter> findForCssProperty(String cssProperty) {
      loadCssNames();
      Collection<Parameter> parameters = new HashSet<Parameter>(1);
      for (Map.Entry<String, String> e : cssNames.entrySet()) {
         if (!e.getValue().isEmpty() && getClass().getSimpleName().equals(e.getKey().substring(0, e.getKey().indexOf('.')))) {
            for (String p : cssNames.getStringProperties(e.getKey(), EMPTY)) {
               if (p.equals(cssProperty)) {
                  String paramKey = e.getKey().substring(e.getKey().indexOf('.') + 1);
                  parameters.add(getParameters().get(paramKey));
                  LOGGER.info(String.format("parameter %s in %s implements css property %s", getClass().getName(),getParameters().get(paramKey).toString(), cssProperty));
               }
            }
         }
      }
      return parameters;
   }

   /**
    * return true when parameter {@link #STYLEAFTER} is true.
    * @return 
    */
   @Override
   public boolean styleAfterAdding() {
      return getValue(STYLEAFTER, Boolean.class);
   }

   public EnhancedMap getSettings() {
      return settings;
   }

}
