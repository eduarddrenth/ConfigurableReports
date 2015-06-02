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
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import static com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl.PARAMHELPER;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.FindableProperties;
import com.vectorprint.configuration.decoration.HelpSupportedProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ThreadSafeProperties;
import com.vectorprint.configuration.observing.HandleEmptyValues;
import com.vectorprint.configuration.observing.PrepareKeyValue;
import com.vectorprint.configuration.observing.TrimKeyValue;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.style.parameters.ReportBindingHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * A Threadsafe reportbuilder; you can safely call {@link #buildReport(java.lang.String[]) } at the same time from
 * different threads on an instance of this class. When threads are reused (pooled) unwanted access to thread variables
 * may occur. When an environment variable {@link #REPORT_CONFIG} is set it is assumed to point to the base url where
 * property files are found, otherwise {@link #CONFIG_URL} is used}.
 *
 * @see MultipleProperties
 * @see ThreadSafeProperties
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ThreadSafeReportBuilder<RD extends ReportDataHolder> extends ReportRunner<RD> {

   private static final Logger log = Logger.getLogger(ThreadSafeReportBuilder.class.getName());
   /**
    * the default config base url, also used as key to store the actual baseurl
    */
   public static final String CONFIG_URL = "config";
   /**
    * name of the optional environment variable that points to the config base url
    */
   public static final String REPORT_CONFIG = "report_config_dir";
   /**
    * name of the file that should hold settings for styling the report
    */
   public static final String STYLINGCONFIG = "styling.properties";
   /**
    * name of the file that should hold settings related to pages in the report
    */
   public static final String PAGECONFIG = "page.properties";
   /**
    * name of the file that should hold settings related to running the report
    */
   public static final String RUNCONFIG = "run.properties";
   /**
    * name of the file that should hold settings for styling charts
    */
   public static final String CHARTCONFIG = "chart.properties";
   /**
    * name of the file that should hold settings for styling tables
    */
   public static final String TABLECONFIG = "table.properties";
   /**
    * name of the file that should hold messages for the application
    */
   public static final String MESSAGECONFIG = "messages.properties";
   /**
    * default set of property fileNames }
    */
   static final Collection<String> DEFAULTPROPERTYURLS = Collections.unmodifiableList(Arrays.asList(new String[]{
      RUNCONFIG, STYLINGCONFIG, PAGECONFIG, CHARTCONFIG, TABLECONFIG, MESSAGECONFIG
   }));
   public static final String HELPFILE = "help.properties";
   private ThreadLocal<String> configbaseurl = new InheritableThreadLocal<String>();

   /**
    * constructor tries to figure out where the configs are using {@link #REPORT_CONFIG} and {@link #CONFIG_URL}. Uses
    * {@link #DEFAULTPROPERTYURLS} to load settings.
    *
    * @param allowEmptyValues when true allow empty values for properties
    * @param trimKeyValues when true trim keys and values for properties
    * @throws IOException
    */
   public ThreadSafeReportBuilder(boolean allowEmptyValues, boolean trimKeyValues) throws IOException, VectorPrintException, ParseException {
      this((System.getenv().containsKey(REPORT_CONFIG))
          ? System.getenv(REPORT_CONFIG)
          : CONFIG_URL, DEFAULTPROPERTYURLS.toArray(new String[DEFAULTPROPERTYURLS.size()]), allowEmptyValues, trimKeyValues);
   }

   /**
    * calls {@link #ThreadSafeReportBuilder(boolean, boolean) } with false
    *
    * @throws IOException
    * @throws VectorPrintException
    */
   public ThreadSafeReportBuilder() throws IOException, VectorPrintException, ParseException {
      this(false, false);
   }

   /**
    * Calls {@link #initObservers(boolean, boolean) } and
    * {@link #ThreadSafeReportBuilder(java.lang.String, java.lang.String[], java.util.List)}.
    *
    * @param allowEmptyValues when true allow empty values for properties
    * @param trimKeyValues when true trim keys and values for properties
    * @param configUrl
    * @throws IOException
    */
   public ThreadSafeReportBuilder(String configUrl, String[] propertyUrls, boolean allowEmptyValues, boolean trimKeyValues)
       throws IOException, VectorPrintException, ParseException {
      this(configUrl, propertyUrls, initObservers(allowEmptyValues, trimKeyValues));
   }

   /**
    * constructor calls {@link #ThreadSafeReportBuilder(EnhancedMap) } using {@link #initProperties(java.lang.String[], java.lang.String, java.util.List)
    * }.
    *
    * @param configUrl
    * @param propertyUrls the urls where properties files can be found
    * @param observers the observers that will be added to the properties
    * @see PrepareKeyValue
    * @throws IOException
    */
   public ThreadSafeReportBuilder(String configUrl, String[] propertyUrls,
       List<PrepareKeyValue<String, String[]>> observers)
       throws IOException, VectorPrintException, ParseException {
      this(initProperties(propertyUrls, configUrl, observers));
   }

   /**
    * bottleneck constructor, calls the super.
    *
    * @param properties
    */
   private ThreadSafeReportBuilder(EnhancedMap properties) {
      super(properties);
   }

   /**
    * adds a {@link TrimKeyValue} and a {@link HandleEmptyValues}, registers {@link ReportConstants#VERSION} and
    * {@link ReportConstants#HELP} as keys that never have a value.
    *
    * @param allowEmptyValues passed to the constructor of {@link HandleEmptyValues}
    * @param trimKeyValues when true add a {@link TrimKeyValue}
    * @return
    */
   public static List<PrepareKeyValue<String, String[]>> initObservers(boolean allowEmptyValues, boolean trimKeyValues) {
      List<PrepareKeyValue<String, String[]>> observers = new LinkedList<PrepareKeyValue<String, String[]>>();
      HandleEmptyValues emptiesNOTOK = new HandleEmptyValues(allowEmptyValues);

      emptiesNOTOK.addKeyToSkip(ReportConstants.VERSION);
      emptiesNOTOK.addKeyToSkip(ReportConstants.HELP);
      observers.add(emptiesNOTOK);
      if (trimKeyValues) {
         observers.add(new TrimKeyValue());
      }

      return observers;
   }

   /**
    * Initializes {@link ParsingProperties} from the arguments, adds a property {@link #CONFIG_URL} and calls {@link #wrapProperties(com.vectorprint.configuration.EnhancedMap)
    * }.
    *
    * @param propertyFileNames
    * @param configUrl
    * @param observers
    * @return
    * @throws IOException
    * @throws VectorPrintException
    */
   public static EnhancedMap initProperties(String[] propertyFileNames, String configUrl,
       List<PrepareKeyValue<String, String[]>> observers)
       throws IOException, VectorPrintException {
      if (propertyFileNames == null || propertyFileNames.length == 0) {
         throw new VectorPrintException("we need at least one property file");
      }
      ParsingProperties pp = null;

      for (String name : propertyFileNames) {
         if (pp == null) {
            pp = new ParsingProperties(new PreparingProperties(new Settings(), observers), configUrl + "/" + name);
         } else {
            pp.addFromURL(configUrl + File.separator + name);
         }
      }
      pp.put(CONFIG_URL, configUrl);

      return wrapProperties(pp);
   }

   /**
    * called from the constructor, wraps properties in
    * {@link ThreadSafeProperties}, {@link CachingProperties}, {@link HelpSupportedProperties} and
    * {@link FindableProperties}.
    *
    * @param mp
    * @return
    */
   public static EnhancedMap wrapProperties(EnhancedMap mp) throws VectorPrintException {
      if (new File(mp.getProperty(CONFIG_URL) + File.separator + HELPFILE).exists()) {
         try {
            return new ThreadSafeProperties(new CachingProperties(new HelpSupportedProperties(new FindableProperties(mp),
                new File(mp.getProperty(CONFIG_URL) + File.separator + HELPFILE).toURI().toURL())
            ));
         } catch (MalformedURLException ex) {
            throw new VectorPrintException(ex);
         }
      } else {
         log.warning(String.format("%s does not exist, no help available for settings (format <key>=<type>;<help text>)",
             new File(mp.getProperty(CONFIG_URL) + File.separator + HELPFILE).getPath()));
         return new ThreadSafeProperties(new CachingProperties(new FindableProperties(mp)));
      }
   }

   /**
    * the base url for configurations and other application resources, including "file:" for file urls.
    *
    * @return
    */
   public String getConfigBaseUrl() {
      if (configbaseurl.get() == null) {
         configbaseurl.set(getSettings().getProperty(CONFIG_URL));
      }

      return configbaseurl.get();
   }

   public String getConfigBaseDir() throws MalformedURLException {
      return new File(new URL(getConfigBaseUrl()).getPath()).getPath();
   }
}
