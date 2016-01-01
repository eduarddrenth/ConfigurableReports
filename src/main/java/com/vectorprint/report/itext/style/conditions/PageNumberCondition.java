/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.conditions;

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

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.report.itext.DocumentAware;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PageNumberCondition extends NumberCondition implements DocumentAware {

   private PdfWriter writer;
   private Document document;

   @Override
   public Document getDocument() {
      return document;
   }

   @Override
   public PdfWriter getWriter() {
      return writer;
   }

   /**
    *
    * @param data the value of data
    * @param element the value of element
    * @return the boolean
    */
   @Override
   public boolean shouldStyle(Object data, Object element) {
      return super.shouldStyle(writer.getCurrentPageNumber(), element);
   }

   @Override
   public void setDocument(Document document, PdfWriter writer) {
      this.writer = writer;
      this.document = document;
   }
   @Override
   public String getHelp() {
      return "Only style on certain pages."; 
   }
}
