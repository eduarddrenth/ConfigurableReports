package com.vectorprint.report.itext.mappingconfig.model;

import com.itextpdf.text.Element;

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
public class ElementConfig
    extends DataConfig {

   private Class<? extends Element> elementtype;
   private String elementtypemethod;

   public Class<? extends Element> getElementtype() {
      return elementtype;
   }

   public ElementConfig setElementtype(Class<? extends Element> value) {
      this.elementtype = value;
      return this;
   }

   public String getElementtypemethod() {
      return elementtypemethod;
   }

   public ElementConfig setElementtypemethod(String value) {
      this.elementtypemethod = value;
      return this;
   }

}
