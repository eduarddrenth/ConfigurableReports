
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.stylers;

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

import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.IntParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * setting row and / or colspan on cells
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ColRowSpan extends AbstractStyler  {

   public static final String COLSPAN_PARAM = "colspan";
   public static final String ROWSPAN_PARAM = "rowspan";

   public ColRowSpan() {

      addParameter(new IntParameter(COLSPAN_PARAM, "how many columns to span").setDefault(1),ColRowSpan.class);
      addParameter(new IntParameter(ROWSPAN_PARAM, "how many rows to span").setDefault(1),ColRowSpan.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      PdfPCell cell = (PdfPCell) text;

      cell.setColspan(getValue(COLSPAN_PARAM, Integer.class));
      cell.setRowspan(getValue(ROWSPAN_PARAM, Integer.class));

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public int getColspan() {
      return getValue(COLSPAN_PARAM, Integer.class);
   }

   public void setColspan(int colspan) {
      setValue(COLSPAN_PARAM, colspan);
   }

   public int getRowspan() {
      return getValue(ROWSPAN_PARAM, Integer.class);
   }

   public void setRowspan(int rowspan) {
      setValue(ROWSPAN_PARAM, rowspan);
   }
   @Override
   public String getHelp() {
      return "Let cells span rows and / or columns. " + super.getHelp(); 
   }
}
