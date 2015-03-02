package com.vectorprint.report.itext.mappingconfig.model;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2014 VectorPrint
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataConfig {

   private DatatypeConfig datatype;
   private List<String> styleclasses = new ArrayList<String>(1);
   private String styleclassesmethod;

   protected String valueasstringmethod = "toString";

   public DatatypeConfig getDatatype() {
      return datatype;
   }

   public DataConfig setDatatype(DatatypeConfig value) {
      this.datatype = value;
      return this;
   }

   public List<String> getStyleclasses() {
      return this.styleclasses;
   }

   public DataConfig addStyleClasses(String... styleClasses) {
      this.styleclasses.addAll(Arrays.asList(styleClasses));
      return this;
   }
   
   public String getStyleclassesmethod() {
      return styleclassesmethod;
   }

   public DataConfig setStyleclassesmethod(String value) {
      this.styleclassesmethod = value;
      return this;
   }

   public String getValueasstringmethod() {
         return valueasstringmethod;
   }

   public DataConfig setValueasstringmethod(String value) {
      this.valueasstringmethod = value;
      return this;
   }

}
