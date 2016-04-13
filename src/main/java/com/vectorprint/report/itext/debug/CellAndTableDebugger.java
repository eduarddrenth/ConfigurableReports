
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.itext.DefaultElementProducer;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.ZebraStripes;
import java.util.ArrayList;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * This class shows the styles used for a PdfPcell or PdfPTable and draws a small border around the cell or table. All
 * cells created by a {@link DefaultElementProducer} with styles provided by a {@link DefaultStylerFactory} can be
 * debugged by setting a property DEBUG to true.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class CellAndTableDebugger extends ZebraStripes implements PdfPCellEvent {

   List<String> styleSetup = new ArrayList<>(100);
   private BaseColor colorToDebug = null;
   DebugStyler ds;

   public CellAndTableDebugger(List<String> setup, EnhancedMap settings, DebugStyler ds) {
      styleSetup = setup;
      setSettings(settings);
      this.ds = ds;
   }

   @Override
   public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {

      if (colorToDebug != null) {
         DebugHelper.debugBackground(canvases[PdfPTable.BACKGROUNDCANVAS], position, colorToDebug, "bg", getSettings(), ds.getLm());
      }
      DebugHelper.debugRect(canvases[PdfPTable.TEXTCANVAS], position, new float[]{2, 2}, 0.3f, getSettings(),ds.getLm());
      DebugHelper.styleLink(canvases[PdfPTable.TEXTCANVAS], DebugHelper.getFirstNotDefaultStyleClass(styleSetup),
          " (cell event)",
          position.getLeft(), position.getTop(), getSettings(),ds.getLm());

   }

   @Override
   protected void paintRow(int row, Rectangle rect, PdfContentByte[] canvases, boolean last, boolean first) {
      if (first) {
         DebugHelper.styleLink(canvases[PdfPTable.TEXTCANVAS], DebugHelper.getFirstNotDefaultStyleClass(styleSetup),
             " (table event)",
             rect.getLeft(), rect.getTop() + 6, getSettings(),ds.getLm());
      }
   }

   void setColorToDebug(BaseColor colorToDebug) {
      this.colorToDebug = colorToDebug;
   }
   
}
