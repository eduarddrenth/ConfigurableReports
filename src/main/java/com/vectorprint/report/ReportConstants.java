
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report;

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
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.jaxb.SettingsFromJAXB;
import com.vectorprint.report.data.DataCollector;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.mappingconfig.DatamappingHelper;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.parameters.binding.JsonReportParameterBindingFactory;
import com.vectorprint.report.running.ReportRunner;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ReportConstants {

   /**
    * boolean setting to print a footer on each page or not.
    *
    * @see EventHelper#PAGEFOOTERSTYLE
    * @see EventHelper#PAGEFOOTERSTYLEKEY`
    * @see EventHelper#PAGEFOOTERTABLEKEY
    * @see EventHelper#renderFooter(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
    */
   public static final String PRINTFOOTER = "printfooter";
   /**
    * name of the setting to change the currency symbol
    *
    * @see Formatter#DEFAULTCURRENCYSYMBOL
    */
   public static final String CURRENCYSYMBOL = "currencysymbol";
   /**
    * setting to configure how many bytes used in Buffered streams.
    */
   public static final String BUFFERSIZE = "buffersize";
   public static final int DEFAULTBUFFERSIZE = 102400;
   /**
    * commandline argument to give visual feedback in the report and other debugging information. Also the name of the
    * layer group containing debugging info.
    */
   public static final String DEBUG = "debug";
   /**
    * commandline argument to show help
    */
   public static final String HELP = "-help";
   /**
    * commandline argument to show version
    */
   public static final String VERSION = "-v";
   /**
    * name of the style for looking up a {@link DocumentStyler} by the {@link StylerFactory}
    */
   public static final String DOCUMENTSETTINGS = "documentsettings";
   /**
    * name of the run property that determines the implementation of the {@link BaseReportGenerator} used
    */
   public static final String REPORTCLASS = "reportclass";
   /**
    * name of the run property that determines the implementation of the {@link DataCollector} used
    */
   public static final String DATACLASS = "dataclass";
   /**
    * if this Systen property is set {@link JsonReportParameterBindingFactory json syntax} will be used
    */
   public static final String JSON = "json";
   /**
    * name of the run property that determines the filename where System.out will be written to when streaming the
    * report
    */
   public static final String SYSOUT = "sysout";
   /**
    * when the run property output is set to this value, the report will be written to standard out
    */
   public static final String STREAM = "STREAM";
   /**
    * color used for debugging info
    */
   public static final String DEBUGCOLOR = "debugcolor";

   /**
    * name of the setting that holds a url to the xml configuration for data mapping
    *
    * @see DatamappingHelper
    * @see com.vectorprint.report.itext.mappingconfig
    */
   public static final String DATAMAPPINGXML = "datamappingxml";
   /**
    * Name of the file containing xml (see {@link SettingsFromJAXB}) or settings (see {@link ParsingProperties}). The
    * file will be searched in the current working directory or in the root of the classpath. You can override the name of this file
    * by passing a file path argument to the {@link ReportRunner} buildReport methods.
    *
    * @see ReportRunner#buildReport(java.lang.String[]) 
    */
   public static final String CONFIG_FILE = "report.properties";

   /**
    * names for margin properties
    */
   public enum MARGIN {

      margin_top, margin_right, margin_bottom, margin_left
   }

   /**
    * name of the setting that points to a url of a icccolorprofile to be used for outputcolors
    *
    * @see ItextHelper#fromColor(java.awt.Color)
    * @see ICC_ColorSpace#ICC_ColorSpace(java.awt.color.ICC_Profile)
    * @see ICC_Profile#getInstance(java.io.InputStream)
    */
   public static final String ICCCOLORPROFILE = "icccolorprofile";
   /**
    * built in srgb color profile
    */
   public static final String DEFAULTICCPROFILE = "/sRGB_IEC61966-2-1_black_scaled.icc";
   /**
    * name of the setting to provide info about the icc profile used
    */
   public static final String ICCINFO = "iccinfo";
   /**
    * name of a boolean setting that may be used. Setting this to false will try to write the stacktrace to the report.
    */
   public static final String STOPONERROR = "stoponerror";
}
