
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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.itext.debug.DebuggablePdfPTable;
import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * setup a table
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Table extends Spacing {

   public static final String NUMCOLPARAM = "columns";
   public static final String NUMHEADERROWS = "headerrows";
   public static final String NUMFOOTERROWS = "footerrows";
   public static final String WIDTHSPARAM = "widths";
   public static final String RELATIVEWIDTHPARAM = "relativewidth";

   public Table() {
      addParameter(new IntParameter(NUMCOLPARAM, "integer").setDefault(2),Table.class);
      addParameter(new IntParameter(NUMFOOTERROWS, "integer"),Table.class);
      addParameter(new IntParameter(NUMHEADERROWS, "integer"),Table.class);
      Parameter<float[]> p = new FloatArrayParameter(WIDTHSPARAM, "float / float[] (1.2|1.3|...)").setDefault(new float[] {50f,50f});
      addParameter(p,Table.class);
      addParameter(new IntParameter(RELATIVEWIDTHPARAM, "integer").setDefault(100),Table.class);
   }

   private PdfPTable style(PdfPTable t) throws VectorPrintException {
      super.style(t, null);
      t.setWidthPercentage(getRelwidth());

      try {
         float[] w = getWidths();

         t.setWidths(w);
         t.setTotalWidth(w);
         t.setHeaderRows(getHeaderrows());
         t.setFooterRows(getFooterrows());
      } catch (DocumentException ex) {
         throw new VectorPrintException("number of widths differs from number of columns in table", ex);
      }

      return t;
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text == null) {
         return (E) style(new DebuggablePdfPTable(getColumns()), data);
      } else {
         return (E) style((PdfPTable) text);
      }
   }

   @Override
   public boolean creates() {
      return true;
   }

   public int getRelwidth() {
      return getValue(RELATIVEWIDTHPARAM,Integer.class);
   }

   public void setRelwidth(int relwidth) {
      setValue(RELATIVEWIDTHPARAM, relwidth);
   }

   public int getColumns() {
      return getValue(NUMCOLPARAM,Integer.class);
   }

   public void setColumns(int columns) {
      setValue(NUMCOLPARAM, columns);
   }

   public int getHeaderrows() {
      return getValue(NUMHEADERROWS,Integer.class);
   }

   public void setHeaderrows(int headerrows) {
      setValue(NUMHEADERROWS, headerrows);
   }

   public int getFooterrows() {
      return getValue(NUMFOOTERROWS,Integer.class);
   }

   public void setFooterrows(int footerrows) {
      setValue(NUMFOOTERROWS, footerrows);
   }

   public float[] getWidths() {
      return getValue(WIDTHSPARAM,float[].class);
   }

   public void setWidths(float[] widths) {
      setValue(WIDTHSPARAM, widths);
   }
   private static final Class<Object>[] classes = new Class[]{PdfPTable.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   
   @Override
   public String getHelp() {
      return "Specify settings and create a table. " + super.getHelp(); 
   }
}
