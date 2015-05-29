
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.debug;

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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.style.FormFieldStyler;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DebuggablePdfPCell extends PdfPCell {

   private BaseColor bg;
   private EnhancedMap settings;
   
   private BaseField bf;
   
   public DebuggablePdfPCell() {
   }

   public DebuggablePdfPCell(Image image) {
      super(image);
   }

   public DebuggablePdfPCell(PdfPCell cell) {
      super(cell);
   }

   public DebuggablePdfPCell(PdfPTable table) {
      super(table);
   }

   public DebuggablePdfPCell(Phrase phrase) {
      super(phrase);
   }

   public DebuggablePdfPCell(Image image, boolean fit) {
      super(image, fit);
   }

   public DebuggablePdfPCell(PdfPTable table, PdfPCell style) {
      super(table, style);
   }

   @Override
   public void setBackgroundColor(BaseColor backgroundColor) {
      if (settings.getBooleanProperty(false, ReportConstants.DEBUG)) {
         bg = backgroundColor;
      } else {
         super.setBackgroundColor(backgroundColor);
      }
   }

   @Override
   public void setCellEvent(PdfPCellEvent cellEvent) {

      if (cellEvent instanceof CellAndTableDebugger) {
         ((CellAndTableDebugger) cellEvent).setColorToDebug(bg);
      }
      if (cellEvent instanceof FormFieldStyler) {
         bf = ((FormFieldStyler)cellEvent).getBaseField();
      }
      super.setCellEvent(cellEvent);
   }

   public BaseField getBaseField() {
      return bf;
   }
   
}
