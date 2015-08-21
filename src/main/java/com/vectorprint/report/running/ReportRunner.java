
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.running;

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
import com.vectorprint.VersionInfo;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapParser;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.jaxb.SettingsFromJAXB;
import com.vectorprint.configuration.jaxb.SettingsXMLHelper;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.DATACLASS;
import static com.vectorprint.report.ReportConstants.HELP;
import static com.vectorprint.report.ReportConstants.REPORTCLASS;
import static com.vectorprint.report.ReportConstants.STREAM;
import static com.vectorprint.report.ReportConstants.SYSOUT;
import static com.vectorprint.report.ReportConstants.VERSION;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.DataCollector;
import com.vectorprint.report.data.DataCollectorImpl;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vectorprint.report.itext.style.parameters.ReportBindingHelper;
import java.io.FileReader;
import java.io.InputStreamReader;
import org.xml.sax.SAXException;

/**
 * This implementation does not depend on iText and may be subclassed to generate reports of other tastes. A
 * ReportRunner needs settings for example to initialize styling for the report. Settings can originate from (in this order):
 * <ul>
 * <li>constructor argument</li>
 * <li>file path containing xml settings declaration</li>
 * <li>file path containing settings</li>
 * <li>default file path ({@link #CONFIG_FILE}) containing settings</li>
 * <li>String argument in build methods containing settings</li>
 * </ul>
 *
 * @param <RD> the type of data for the Runner
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportRunner<RD extends ReportDataHolder> implements ReportBuilder<String[], RD> {

   private static final Logger log = Logger.getLogger(ReportRunner.class.getName());
   /**
    * return value for {@link #buildReport(java.lang.String[]) } indicating a property
    * ({@link ReportConstants#HELP}, {@link ReportConstants#VERSION}) caused a direct return of the method.
    */
   public static final int EXITFROMPROPERTYCODE = -1;
   public static final int EXITNOSETTINGS = -2;
   public static final String DATACLASS_HELP = "annotate your dataclasses, extend " + DataCollectorImpl.class.getName() + " and provide its classname using the property " + DATACLASS;
   private EnhancedMap settings;

   /**
    * initializes settings and {@link EnhancedMapBindingFactory}
    *
    * @see SettingsFromJAXB#fromJaxb(java.io.Reader)
    * @see EnhancedMapBindingFactory#getParser(java.io.Reader)
    * @param properties
    */
   public ReportRunner(EnhancedMap properties) {
      if (properties == null) {
         throw new IllegalArgumentException("properties may not be null");
      }
      this.settings = properties;
      bindingFactory = EnhancedMapBindingFactoryImpl.getDefaultFactory();
   }

   public ReportRunner() {
      bindingFactory = EnhancedMapBindingFactoryImpl.getDefaultFactory();
   }

   /**
    * Called from {@link #buildReport(java.lang.String[]) } and {@link #buildReport(java.lang.String[], java.io.OutputStream) }. Uses 
    * at most 2 arguments the first containing the path to a file, the second containing the path to a file. Both arguments are optional.
    * When this report runner does not have any settings yet initialize them using {@link #initSettingsFromFile(java.lang.String) }, when settings
    * are still not found instantiate new settings.
    * If there is an argument containing settings {@link EnhancedMapParser#parse(com.vectorprint.configuration.EnhancedMap) } will be called.
    * 
    * @param args at most two are used, may be null
    * @throws Exception when a failure occurs, also when settings are not initialized properly
    */
   protected void initSettings(String[] args) throws Exception {
      if (args != null && args.length > 0) {
         int secondArg = 0;
         boolean needSettingsArg = false;
         if (settings == null) {
            settings = initSettingsFromFile(args[0]);
            if (settings != null) {
               secondArg = 1;
            } else {
               needSettingsArg = true;
            }
         }
         if (secondArg < args.length) {
            if (needSettingsArg) {
               settings = new CachingProperties(new Settings());
            }
            bindingFactory.getParser(new StringReader(args[secondArg])).parse(settings);
         } else if (needSettingsArg) {
            System.out.println(SETTINGS_HELP);
            System.exit(EXITNOSETTINGS);
         }
      }
      if (settings == null) {
         System.out.println(SETTINGS_HELP);
         System.exit(EXITNOSETTINGS);
      }
   }

   /**
    * Calls {@link #buildReport(java.lang.String[], java.io.OutputStream) }, uses the setting {@link #OUTPUT}.
    *
    * @throws java.lang.Exception
    * @param args the command line arguments
    * @return the value returned by {@link ReportGenerator#generate(com.vectorprint.report.data.ReportDataHolder, java.io.OutputStream) },
    * {@link #EXITFROMPROPERTYCODE}, {@link #EXITNOSETTINGS}
    */
   @Override
   public final int buildReport(String[] args) throws Exception {

      try {
         initSettings(args);

         if (settings.containsKey(VERSION)) {
            for (VersionInfo.VersionInformation mi : VersionInfo.getVersionInfo().values()) {
               System.out.println(mi);
            }

            return EXITFROMPROPERTYCODE;
         }

         if (settings.containsKey(HELP)) {
            showHelp(settings);

            return EXITFROMPROPERTYCODE;
         }

         if (STREAM.equals(settings.getProperty(OUTPUT))) {
            OutputStream o = System.out;

            System.setOut(new PrintStream(settings.getProperty(getClass().getSimpleName() + ".out", SYSOUT),
                "UTF-8"));
            log.info("printing report to standard output");

            // stream
            return buildReport(null, o);
         } else {
            log.info(String.format("printing report to %s", settings.getProperty(OUTPUT)));

            String to = settings.getProperty(OUTPUT);

            File f = new File(to);

            if (f.canRead()) {
               return buildReport(null, new FileOutputStream(f));
            } else {
               URL u = BindingHelper.URL_PARSER.convert(to);
               URLConnection conn = u.openConnection();

               conn.setDoOutput(true);
               conn.setDoInput(false);
               return buildReport(null, conn.getOutputStream());
            }

         }
      } catch (Exception exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      }
   }

   /**
    * Key of the setting that determines the output when calling {@link #buildReport(java.lang.String[]) }
    */
   public static final String OUTPUT = "output";

   /**
    * called when a setting {@link ReportConstants#HELP} is present, calls {@link Help#printHelp(java.io.PrintStream) } and {@link EnhancedMap#printHelp()
    * }.
    *
    * @param properties
    * @throws java.io.IOException
    * @throws java.io.FileNotFoundException
    * @throws java.lang.ClassNotFoundException
    * @throws java.lang.InstantiationException
    * @throws java.lang.IllegalAccessException
    * @throws java.lang.NoSuchMethodException
    * @throws java.lang.reflect.InvocationTargetException
    */
   protected void showHelp(EnhancedMap properties) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

      Help.printHelp(System.out);

      System.out.println(properties.printHelp());

   }

   /**
    * Instantiates a datacollector based on a property {@link ReportConstants#DATACLASS}
    *
    * @return
    * @throws com.vectorprint.VectorPrintException
    */
   @Override
   public DataCollector<RD> getDataCollector() throws VectorPrintException {
      try {
         if (!getSettings().containsKey(DATACLASS)) {
            throw new VectorPrintException(DATACLASS_HELP);
         }
         Class dataClass = Class.forName(getSettings().getProperty(DATACLASS));

         return (DataCollector<RD>) dataClass.newInstance();
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
   }

   /**
    * Instantiates a report generator based on a property {@link ReportConstants#REPORTCLASS}, defaults to {@link
    * BaseReportGenerator}.
    *
    * @return
    * @throws com.vectorprint.VectorPrintException
    */
   @Override
   public ReportGenerator<RD> getReportGenerator() throws VectorPrintException {
      try {
         Class reportClass = getSettings().getClassProperty(BaseReportGenerator.class, REPORTCLASS);

         return (ReportGenerator<RD>) reportClass.newInstance();
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      } catch (ClassNotFoundException ex) {
         throw new VectorPrintException(ex);
      }
   }

   public EnhancedMap getSettings() {
      return settings;
   }

   /**
    * when no argument is given this file will be searched in the current working directory or in the root of the
    * classpath
    *
    * @see #main(java.lang.String[])
    */
   public static final String CONFIG_FILE = "report.properties";
   public static final String SETTINGS_HELP = "Provide the path to your settingsfile as argument. "
       + "A settingsfile contains either xml (xsd available in Config jar) declaring settings or it contains settings.\n"
       + "You can also just put " + CONFIG_FILE + " in the current working directory or in the root one of your jars.\n"
       + "In Your settings you must at least provide the name of your " + DataCollector.class.getName()
       + "in a setting \"" + ReportConstants.DATACLASS + "\".\nFurthermore you probably want to provide styling information"
       + "for the data yielded by your collector.\n";

   /**
    * looks for {@link #CONFIG_FILE} in the working directory or in the classpath ({@link ClassLoader#getResourceAsStream(java.lang.String)
    * }), when found creates {@link CachingProperties} holding {@link ParsingProperties}.
    *
    * @return settings or null
    * @throws Exception
    */
   public static EnhancedMap findSettings() throws Exception {
      if (new File(CONFIG_FILE).canRead()) {
         return new CachingProperties(new ParsingProperties(new Settings(), CONFIG_FILE));
      } else {
         InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream('/' + CONFIG_FILE);
         if (in != null) {
            return new CachingProperties(new ParsingProperties(new Settings(), new InputStreamReader(in)));
         }
      }
      return null;
   }

   /**
    * When the argument is null call {@link #findSettings() }. When the argument is a file that exists it is assumed to
    * either be an xml file declaring settings or a file holding setting. Either {@link SettingsFromJAXB#fromJaxb(java.io.Reader)
    * } or {@link ParsingProperties#ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.lang.String...)  } will
    * be called.
    * 
    * @see SettingsXMLHelper#XSD
    * @param arg name of a file or null
    * @return settings or null
    * @throws Exception
    */
   public static EnhancedMap initSettingsFromFile(String arg) throws Exception {
      if (arg != null) {
         if (new File(arg).canRead()) {
            try {
               SettingsXMLHelper.validateXml(arg);
               return new SettingsFromJAXB().fromJaxb(new FileReader(arg));
            } catch (SAXException sAXException) {
               log.warning(String.format("%s does not contain settings xml, trying to parse settings directly", arg));
            }
            return new CachingProperties(new ParsingProperties(new Settings(), arg));
         }
      }
      return findSettings();
   }

   /**
    *
    *
    * @param args
    */
   public static void main(String[] args) throws Exception {
      System.exit(new ReportRunner().buildReport(args));
   }

   private final EnhancedMapBindingFactory bindingFactory;

   /**
    * Bottleneck method, writes report to stream argument, calls {@link BaseReportGenerator#generate(com.vectorprint.report.data.ReportDataHolder, java.io.OutputStream)
    * }.
    *
    * @see DataCollector
    * @see ReportGenerator
    * @param args
    * @param out
    * @return the value returned by {@link ReportGenerator#generate(com.vectorprint.itextreport.data.ReportDataHolder, java.io.OutputStream)},
    * {@link #EXITFROMPROPERTYCODE}
    * @throws Exception
    */
   @Override
   public int buildReport(String[] args, final OutputStream out) throws Exception {

      try {
         initSettings(args);

         if (settings.containsKey(VERSION)) {
            for (VersionInfo.VersionInformation mi : VersionInfo.getVersionInfo().values()) {
               System.out.println(mi);
            }

            return EXITFROMPROPERTYCODE;
         }

         if (settings.containsKey(HELP)) {
            showHelp(settings);

            return EXITFROMPROPERTYCODE;
         }

         DataCollector<RD> dc = getDataCollector();
         // init bindinghelper now
         String clazz = System.getProperty(ParameterizableBindingFactoryImpl.PARAMHELPER);
         if (clazz == null) {
            System.setProperty(ParameterizableBindingFactoryImpl.PARAMHELPER, dc.getDefaultBindingHelperClass().getName());
         } else {
            Class<?> forName = Class.forName(clazz);
            if (!ReportBindingHelper.class.isAssignableFrom(forName)) {
               throw new VectorPrintException(String.format("%s, from system property %s is not a %s", clazz, ParameterizableBindingFactoryImpl.PARAMHELPER,
                   ReportBindingHelper.class.getName()));
            } else {
               System.setProperty(ParameterizableBindingFactoryImpl.PARAMHELPER, clazz);
            }
         }
         ReportGenerator<RD> rg = getReportGenerator();

         StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(rg, settings);
         StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(dc, settings);

         return rg.generate(dc.collect(), out);
      } catch (IOException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (ClassNotFoundException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (InstantiationException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (IllegalAccessException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (NoSuchMethodException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (InvocationTargetException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (VectorPrintException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } catch (RuntimeException exception) {
         log.log(Level.SEVERE, exception.getMessage(), exception);

         throw exception;
      } finally {
         log.info(String.format("Settings (from %s) not used sofar: %s", getSettings().getId(), getSettings().getUnusedKeys()));
         log.info(String.format("Settings (from %s) not present, for which a default was used: %s", getSettings().getId(), getSettings().getKeysNotPresent()));
         if (out != null) {
            out.close();
         }
      }
   }
}
