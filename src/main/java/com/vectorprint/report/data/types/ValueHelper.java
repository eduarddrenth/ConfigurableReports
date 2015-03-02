
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
//~--- JDK imports ------------------------------------------------------------
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * provides convenience methods to create {@link ReportValue}s as well as a method to get hold of a calendar that solves
 * problems with week numbers.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ValueHelper {

   public static DateValue createDate(long date) {
      return (DateValue) new DateValue().setValue(new Date(date));
   }

   public static DateValue createDate(Date d) {
      return (DateValue) new DateValue().setValue(d);
   }

   public static DurationValue createDuration(long millis) {
      return createDuration(Double.toString(millis));
   }

   public static DurationValue createDuration(String millis) {
      DurationValue mv = (DurationValue) new DurationValue().setValue(new BigDecimal(millis));
      return mv;
   }

   public static PeriodValue createPeriod(Date start, Date end) {
      return (PeriodValue) new PeriodValue().setValue(new Period(start, end));
   }

   public static PeriodValue createPeriod(long start, long end) {
      return (PeriodValue) new PeriodValue().setValue(new Period(new Date(start), new Date(end)));
   }

   public static MoneyValue createMoney(double bd) {
      return createMoney(Double.toString(bd));
   }

   public static MoneyValue createMoney(String bd) {
      MoneyValue mv = (MoneyValue) new MoneyValue().setValue(new BigDecimal(bd));
      return mv;
   }

   public static NumberValue createNumber(String bd) {
      NumberValue mv = (NumberValue) new NumberValue().setValue(new BigDecimal(bd));
      return mv;
   }

   public static PercentageValue createPercentageValue(String bd) {
      PercentageValue mv = (PercentageValue) new PercentageValue().setValue(new BigDecimal(bd));
      return mv;
   }

   public static PercentageValue createPercentage(double bd) {
      return createPercentageValue(Double.toString(bd));
   }

   public static NumberValue createNumber(double bd) {
      return createNumber(Double.toString(bd));
   }

   public static TextValue createText(String text) {
      return (TextValue) new TextValue().setValue(text);
   }

   /**
    * returns {@link Calendar#getInstance() } with {@link Calendar#setMinimalDaysInFirstWeek(int) } set to 4.
    *
    * @return
    */
   public static Calendar getCalendar() {
      Calendar c = Calendar.getInstance();

      c.setMinimalDaysInFirstWeek(4);

      return c;
   }
}
