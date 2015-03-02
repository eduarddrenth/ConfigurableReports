package com.vectorprint.report.data.types;

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
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.report.ReportConstants;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * formats {@link ReportValue}'s according to format Strings. Uses {@link Locale#getDefault() }, which can be changed specifying
 * system properties (i.e. -Duser.language=nl -Duser.country=NL).
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Formatter {

   private static final Logger log = Logger.getLogger(Formatter.class.getName());
   public static final String DEFAULTCURRENCYSYMBOL = "€";
   public static final String DEFAULT_CURRENCY_FORMAT = "\u00a4 #,###.##";
   public static final String DEFAULT_NUMBER_FORMAT = "#,###.##";
   public static final String DEFAULT_PERCENTAGE_FORMAT = "#,###.## '%'";
   public static final String DEFAULT_DURATION_FORMAT = "#,###.##";
   public static final String DEFAULT_DURATION_SUFFIX = " sec. ";
   public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";
   public static final String DEFAULT_DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
   private String dateFormat = DEFAULT_DATE_FORMAT;
   private String datetimeFormat = DEFAULT_DATETIME_FORMAT;
   private String numberFormat = DEFAULT_NUMBER_FORMAT;
   private String durationFormat = DEFAULT_DURATION_FORMAT;
   private String durationSuffix = DEFAULT_DURATION_SUFFIX;
   private String percentageFormat = DEFAULT_PERCENTAGE_FORMAT;
   private String currencyFormat = DEFAULT_CURRENCY_FORMAT;
   @Setting(key = ReportConstants.CURRENCYSYMBOL)
   private String currencySymbol = DEFAULTCURRENCYSYMBOL;
   @Setting(key = ReportConstants.DEBUG)
   private boolean debug = false;
  

   private static final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

   private DecimalFormat getFormat(String format) {
      dfs.setCurrencySymbol(currencySymbol);
      DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
      nf.setDecimalFormatSymbols(dfs);
      nf.applyPattern(format);
      return nf;
   }

   /**
    * formats values according to the format patterns specified in this formatter, or uses 
    * {@link ReportValue#getAlternateFormat() } if provided.
    * @param val
    * @return 
    */
   public String formatValue(ReportValue val) {
      if (log.isLoggable(Level.FINE) || debug) {
         log.fine("raw: " + val + ", formatted: " + doFormatValue(val));
      }
      return doFormatValue(val);
   }

   private String doFormatValue(ReportValue val) {
      if (val == null) {
         return "";
      }

      if (val.getValue() == null) {
         return val.getEmptyValue();
      }

      boolean alt = val.getAlternateFormat()!=null&&!"".equals(val.getAlternateFormat());

      if (val instanceof TextValue) {
         return String.valueOf(val.getValue());
      } else if (val instanceof MoneyValue) {
         return getFormat((alt)?val.getAlternateFormat():currencyFormat).format(val.getValue());
      } else if (val instanceof DateValue) {
         return new SimpleDateFormat((alt)?val.getAlternateFormat():dateFormat).format(((DateValue)val).value);
      } else if (val instanceof PeriodValue) {
         return new SimpleDateFormat((alt)?val.getAlternateFormat():datetimeFormat).format(((PeriodValue)val).value.getStart()) + " - " +
             new SimpleDateFormat((alt)?val.getAlternateFormat():datetimeFormat).format(((PeriodValue)val).value.getEnd());
      } else if (val instanceof NumberValue) {
         return getFormat((alt)?val.getAlternateFormat():numberFormat).format(val.getValue());
      } else if (val instanceof PercentageValue) {
         return getFormat((alt)?val.getAlternateFormat():percentageFormat).format(val.getValue());
      } else if (val instanceof DurationValue) {
         return getFormat((alt)?val.getAlternateFormat():durationFormat).format(val.getValue()) + durationSuffix;
      } else {

         // Is another object
         if (log.isLoggable(Level.FINE)) {
            log.fine("no formatting for value: " + val.getValue() + " type: " + val.getClass());
         }

         return String.valueOf(val.getValue());
      }
   }

   public String getDateFormat() {
      return dateFormat;
   }

   public void setDateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
   }

   public String getPercentageFormat() {
      return percentageFormat;
   }

   public void setPercentageFormat(String percentageFormat) {
      this.percentageFormat = percentageFormat;
   }

   public String getCurrencyFormat() {
      return currencyFormat;
   }

   public void setCurrencyFormat(String currencyFormat) {
      this.currencyFormat = currencyFormat;
   }

   public String getCurrencySymbol() {
      return currencySymbol;
   }

   public void setCurrencySymbol(String currencySymbol) {
      this.currencySymbol = currencySymbol;
   }

   public String getNumberFormat() {
      return numberFormat;
   }

   public void setNumberFormat(String numberFormat) {
      this.numberFormat = numberFormat;
   }

   public String getDurationFormat() {
      return durationFormat;
   }

   public void setDurationFormat(String durationFormat) {
      this.durationFormat = durationFormat;
   }

   public String getDurationSuffix() {
      return durationSuffix;
   }

   public void setDurationSuffix(String durationSuffix) {
      this.durationSuffix = durationSuffix;
   }

   public String getDatetimeFormat() {
      return datetimeFormat;
   }

   public void setDatetimeFormat(String datetimeFormat) {
      this.datetimeFormat = datetimeFormat;
   }

}
