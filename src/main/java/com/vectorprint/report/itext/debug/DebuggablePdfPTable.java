
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
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DebuggablePdfPTable extends PdfPTable {

   public DebuggablePdfPTable() {
   }

   public DebuggablePdfPTable(float[] relativeWidths) {
      super(relativeWidths);
   }

   public DebuggablePdfPTable(int numColumns) {
      super(numColumns);
   }

   private Collection<NestedTableEvent> childEvents = new HashSet<NestedTableEvent>(1);
   private boolean nestingAdded = false;

   @Override
   public PdfPCell addCell(PdfPCell cell) {
      Object o = (cell.getCompositeElements() == null) ? null : cell.getCompositeElements().get(0);
      if (o == null) {
         o = cell.getTable();
      }
      if (o instanceof PdfPTable) {
         if (!nestingAdded) {
            setTableEvent(new NestingTableEvent());
            nestingAdded = true;
         }
         NestedTableEvent pe = new NestedTableEvent(((PdfPTable) o).getTableEvent());
         ((PdfPTable) o).setTableEvent(null);
         ((PdfPTable) o).setTableEvent(pe);
         childEvents.add(pe);
      }
      return super.addCell(cell);
   }

   private class NestedTableEvent implements PdfPTableEvent {

      private PdfPTableEvent orig = null;
      private PdfPTable table;
      private float[][] widths;
      private int headerRows;
      private float[] heights;
      private int rowStart;

      public NestedTableEvent(PdfPTableEvent event) {
         orig = event;
      }

      private void process() {
            orig.tableLayout(table, widths, heights, headerRows, rowStart, canvases);
      }

      @Override
      public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
         this.table = table;
         this.widths = widths;
         this.heights = heights;
         this.headerRows = headerRows;
         this.rowStart = rowStart;
      }
   }
   private PdfContentByte[] canvases;

   private class NestingTableEvent implements PdfPTableEvent {

      @Override
      public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
         DebuggablePdfPTable.this.canvases = canvases;
         for (NestedTableEvent nt : childEvents) {
            nt.process();
         }
      }
   }
}
