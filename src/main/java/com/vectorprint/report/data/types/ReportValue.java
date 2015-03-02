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


/**
 * Baseclass for values to be used in reports, provides a construct for showing empty values and a construct to indicate
 * a {@link Formatter} to use an alternate format.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <T> The actual datatype
 */
public abstract class ReportValue<T extends Comparable> implements Comparable<ReportValue> {

   protected T value = null;
   private String alternateFormat = null;
   private String emptyValue = "-";

   public ReportValue() {
      this(null, null);
   }

   public ReportValue(T value) {
      this(value, null);
   }

   public ReportValue(T value, String alternateFormat) {
      this.value = value;
      this.alternateFormat = alternateFormat;
   }

   /**
    * Returns {@link #getValue() }.toString() or {@link #getEmptyValue() } when values is null
    *
    * @return
    */
   @Override
   public String toString() {
      return (value == null)
          ? emptyValue
          : value.toString();
   }

   @Override
   public int hashCode() {
      return (value == null)
          ? 0
          : value.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (!(obj.getClass().equals(this.getClass()))) {
         return false;
      }

      ReportValue other = (ReportValue) obj;

      if ((other.getValue() == null) && (value != null)) {
         return false;
      }

       return !((other.getValue() != null) && (value == null)) && other.getValue().equals(value);

   }

   @Override
   public int compareTo(ReportValue o) {
      return value.compareTo(o.value);
   }

   public T getValue() {
      return value;
   }

   public String getEmptyValue() {
      return emptyValue;
   }

   public ReportValue<T> setEmptyValue(String emptyValue) {
      this.emptyValue = emptyValue;
      return this;
   }

   public ReportValue<T> setValue(T value) {
      this.value = value;
      return this;
   }

   /**
    * when true a {@link Formatter} should format the value in an alternate way.
    *
    * @return
    */
   public String getAlternateFormat() {
      return alternateFormat;
   }

   /**
    * You can provide an alternate format if you want
    *
    * @param alternateFormat
    * @return
    */
   
   public ReportValue<T> setAlternateFormat(String alternateFormat) {
      this.alternateFormat = alternateFormat;

      return this;
   }
}
