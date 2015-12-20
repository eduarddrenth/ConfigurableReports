
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
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.StringArrayParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.style.ElementProducing;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * add a cell to a table
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters (
    parameters = {
       @Param (
           key = AdvancedImpl.DATA,
           clazz = StringParameter.class
       ),
       @Param (
           key = AddCell.STYLECLASS,
           clazz = StringArrayParameter.class
       )
    }
)
public class AddCell extends AbstractStyler implements ElementProducing {
   public static final String STYLECLASS = "styleclass";
   
   private ElementProducer elementProducer;
   private StylerFactory stylerFactory;

   private static final Class<Object>[] classes = new Class[]{PdfPTable.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   
   @Override
   public String getHelp() {
      return "Add a styled cell to a table. " + super.getHelp(); 
   }

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      try {
         java.util.List<BaseStyler> st = (isParameterSet(STYLECLASS)) ? stylerFactory.getStylers(getValue(STYLECLASS, String[].class)) : null;
         ((PdfPTable)element).addCell(elementProducer.createElement(
             getValue(AdvancedImpl.DATA, String.class),
             PdfPCell.class,
             st));
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
      return element;
   }

   @Override
   public void setElementProducer(ElementProducer elementProducer) {
      this.elementProducer = elementProducer;
   }

   @Override
   public void setStylerFactory(StylerFactory stylerFactory) {
      this.stylerFactory = stylerFactory;
   }
}
