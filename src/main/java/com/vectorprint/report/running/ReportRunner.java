
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
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.configuration.decoration.ParsingProperties;
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
import static com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl.*;
import static com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl.*;
import com.vectorprint.report.itext.style.parameters.ReportBindingHelper;

/**
 * This implementation does not depend on iText and may be subclassed to generate reports of other tastes. It overrides
 * {@link ReportBuilder} with String[] for arguments.
 *
 * @param <RD> the type of data for the Runner
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportRunner<RD extends ReportDataHolder> implements ReportBuilder<String[], RD> {

   private static final Logger log = Logger.getLogger(ReportRunner.class.getName());
   /**
    * return value for {@link #buildReport(java.lang.String[]) } indicating a property was present causing a direct
    * return of the method.
    */
   public static final int EXITFROMPROPERTYCODE = -1;
   public static final int EXITNOSETTINGS = -2;
   public static final String DATACLASS_HELP = "annotate your dataclasses, extend " + DataCollectorImpl.class.getName() + " and provide its classname using the property " + DATACLASS;
   private final EnhancedMap settings;

   public ReportRunner(EnhancedMap properties) {
      if (properties == null) {
         throw new IllegalArgumentException("properties may not be null");
      }
      this.settings = properties;
   }

   /**
    * Calls {@link #buildReport(java.lang.String[], java.io.OutputStream) }, uses the setting {@link #OUTPUT}.
    *
    * @throws java.lang.Exception
    * @param args the command line arguments
    * @return the value returned by {@link ReportGenerator#generate(com.vectorprint.itextreport.data.ReportDataHolder, java.io.OutputStream)},
    * {@link #EXITFROMPROPERTYCODE}, {@link #EXITNOSETTINGS} or {@link #INVALIDPDF}
    */
   @Override
   public final int buildReport(String[] args) throws Exception {

      try {
         if (args != null && args.length > 0) {
            bindingFactory.getParser(new StringReader(args[0])).parse(settings);
         }

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

            if (to.indexOf(':') == -1) {
               to = "file:" + to;
            }

            URL u = new URL(to);

            if ("file".equals(u.getProtocol())) {
               return buildReport(null, new FileOutputStream(u.getFile()));
            } else {
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
    * called when a setting {@link #HELP} is present, calls {@link Help#printHelp(java.io.PrintStream) } and {@link EnhancedMap#printHelp()
    * }.
    *
    * @param properties
    */
   protected void showHelp(EnhancedMap properties) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

      Help.printHelp(System.out);

      System.out.println(properties.printHelp());

   }

   /**
    * Instantiates a datacollector based on a property {@link #DATACLASS}
    *
    * @return
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
    * Instantiates a report generator based on a property {@link #REPORTCLASS}, defaults to {@link
    * BaseReportGenerator}
    *
    * @return
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
   public static final String SETTINGS_HELP = "provide path to your settingsfile as an argument, or put " + CONFIG_FILE + " in the current working directory or in the root one of your jars\n";

   /**
    * looks for {@link #CONFIG_FILE} in the working directory or in the classpath ({@link ClassLoader#getResourceAsStream(java.lang.String)
    * }), when found creates {@link VectorPrintProperties } and calls {@link ReportRunner#buildReport(java.lang.String[])
    * }.
    *
    * @return {@link #EXITNOSETTINGS} when no config found
    * @throws Exception
    */
   public static int findSettingsAndBuild() throws Exception {
      if (new File(CONFIG_FILE).canRead()) {
         return new ReportRunner(new ParsingProperties(new Settings(), CONFIG_FILE)).buildReport(null);
      } else {
         InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream('/' + CONFIG_FILE);
         if (in != null) {
            return new ReportRunner(new ParsingProperties(new Settings(), CONFIG_FILE)).buildReport(null);
         }
      }
      return EXITNOSETTINGS;
   }

   private static <T> Class<T> findClass(String systemProperty, Class<T> clazz) throws ClassNotFoundException {
      if (System.getProperty(systemProperty) != null) {
         return (Class<T>) Class.forName(System.getProperty(systemProperty));
      } else {
         return clazz;
      }
   }

   protected static void initSyntaxFactories() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      ParameterizableBindingFactoryImpl.getFactory(findClass(PARAMPARSER, PARAMPARSERCLASS),
          findClass(PARAMSERIALIZER, PARAMSERIALIZERCLASS),
          findClass(PARAMHELPER, ReportBindingHelper.class).newInstance(), true);
      EnhancedMapBindingFactoryImpl.getFactory(findClass(SETTINGSPARSER, SETTINGSPARSERCLASS),
          findClass(SETTINGSSERIALIZER, SETTINGSSERIALIZERCLASS),
          findClass(SETTINGSHELPER, SETTINGSHELPERCLASS).newInstance(), true);
      bindingFactory = EnhancedMapBindingFactoryImpl.getDefaultFactory();
   }

   /**
    * if the first argument is a file, this file is assumed to be the settings file, otherwise settings are searched
    * using {@link #CONFIG_FILE}. When no settings are found help will be printed to standard out.
    *
    * @param args
    */
   public static void main(String[] args) throws Exception {
      // lookup settings using default filename
      // call constructor and run
      if (args != null) {
         if (args.length > 0 && new File(args[0]).canRead()) {
            String[] shiftArgs = new String[args.length - 1];
            if (shiftArgs.length > 0) {
               System.arraycopy(args, 1, shiftArgs, 0, shiftArgs.length);
            }
            /*
             before anything (also before parsing properties) we need to init the factories dealing with syntax:
            
             ParameterizableBindingFactory and EnhancedMapBindingFactory
            
             we need to know 6 classes:
            
             2 parser classes, 2 serializer classes and 2 bindinghelper class
            
             */
            initSyntaxFactories();
            System.exit(new ReportRunner(new ParsingProperties(new Settings(), args[0])).buildReport(shiftArgs));
         }
      }
      if (EXITNOSETTINGS == findSettingsAndBuild()) {
         System.out.println(SETTINGS_HELP);
         Help.printHelp(System.out);
         System.exit(EXITNOSETTINGS);
      }
   }

   private static EnhancedMapBindingFactory bindingFactory;

   /**
    * Bottleneck method, writes report to stream argument, calls {@link BaseReportGenerator#generate(com.vectorprint.report.data.ReportDataHolder, java.io.OutputStream)
    * }.
    *
    * @see EnhancedMap#addFromArguments(java.lang.String[])
    * @see DataCollector
    * @see ReportGenerator
    * @param args
    * @param out
    * @return the value returned by {@link ReportGenerator#generate(com.vectorprint.itextreport.data.ReportDataHolder, java.io.OutputStream)},
    * {@link #EXITFROMPROPERTYCODE}, {@link #EXITNOSETTINGS} or {@link #INVALIDPDF}
    * @throws Exception
    */
   @Override
   public int buildReport(String[] args, final OutputStream out) throws Exception {

      try {
         if (args != null && args.length > 0) {
            bindingFactory.getParser(new StringReader(args[0])).parse(settings);
         }

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
         log.info(String.format("Settings (%s) not used sofar: %s", getSettings().getId(), getSettings().getUnusedKeys()));
         log.info(String.format("Settings (%s) not present, default used: %s", getSettings().getId(), getSettings().getKeysNotPresent()));
         if (out != null) {
            out.close();
         }
      }
   }
}
