package com.vectorprint.report.itext.mappingconfig.model;

import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.ReportValue;

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
public class DatatypeConfig {

   private String format;
   private Class<? extends ReportValue> dataclass;

   public String getFormat() {
      return format;
   }

   public DatatypeConfig setFormat(String value) {
      this.format = value;
      return this;
   }

   public Class<ReportValue> getDataclass() {
      return (Class<ReportValue>) dataclass;
   }

   public DatatypeConfig setDataclass(Class<? extends ReportValue> value) {
      this.dataclass = value;
      return this;
   }

}
