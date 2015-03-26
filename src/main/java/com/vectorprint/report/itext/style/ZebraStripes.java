package com.vectorprint.report.itext.style;

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

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsField;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.LayerManagerAware;
import com.vectorprint.report.itext.debug.DebugHelper;
import java.awt.Color;

//~--- JDK imports ------------------------------------------------------------

public class ZebraStripes implements PdfPTableEvent, LayerManagerAware {

   private int endRowsToSkip = 0;
   private int realNumberOfRows = 0;
   private int rowsDealtWith = 0;
   private Color rowBackgroundColor = null;
   private int alternate;
   private Color oddColor;
   private Color evenColor;
   @SettingsField
   private EnhancedMap settings;
   private LayerManager layerManager;
   private ItextHelper itextHelper;

   public ZebraStripes() {
      this(1);
   }

   public ZebraStripes(int alternate) {
      this(alternate, Color.WHITE, Color.LIGHT_GRAY);
   }

   public ZebraStripes(int alternate, Color oddColor, Color evenColor) {
      this.alternate = (alternate > 0)
          ? alternate
          : 1;
      this.oddColor = oddColor;
      this.evenColor = evenColor;
      this.rowBackgroundColor = oddColor;
   }

   public ZebraStripes setSkipLastRows(int endRowsToSkip) {
      this.endRowsToSkip = endRowsToSkip;

      return this;
   }

   public ZebraStripes setRealNumberOfRows(int realNumberOfRows) {
      this.realNumberOfRows = realNumberOfRows;

      return this;
   }

   void setCurrentBackGround(Color color) {
      rowBackgroundColor = color;
   }

   private int getEndRowsToSkip() {
      return (rowsDealtWith >= realNumberOfRows)
          ? endRowsToSkip
          : 0;
   }

   @Override
   public void tableLayout(final PdfPTable table, final float[][] widths, final float[] heights, final int headerRows,
       final int rowStart, final PdfContentByte[] canvases) {
      rowsDealtWith += widths.length;

      final int footer = widths.length - table.getFooterRows();
      final int header = table.getHeaderRows();
      int columns = widths[header].length - 1;
      float w = widths[header][columns];
      int i = 0;
      boolean up = true;

      setCurrentBackGround(oddColor);

      for (int row = header; row < footer; row++) {
         if (row >= footer - getEndRowsToSkip()) {
            continue;
         }

         paintRow(row, new Rectangle(widths[row][0], heights[row + 1], w, heights[row]), canvases, row == footer - 1,
             header == row);

         if (row < footer - 1 - getEndRowsToSkip()) {
            if (up) {
               i++;

               if (i == alternate) {
                  setCurrentBackGround(evenColor);
                  up = false;
               }
            } else {
               i--;

               if (i == 0) {
                  setCurrentBackGround(oddColor);
                  up = true;
               }
            }
         }
      }
   }

   protected void paintRow(int row, Rectangle rect, PdfContentByte[] canvases, boolean last, boolean first) {
      if (settings.getBooleanProperty(DEBUG, false)) {
         DebugHelper.debugBackground(canvases[PdfPTable.BACKGROUNDCANVAS], rect, itextHelper.fromColor(rowBackgroundColor), "zebra", settings, layerManager);
         return;
      }

      rect.setBackgroundColor(itextHelper.fromColor(rowBackgroundColor));
      rect.setBorder(Rectangle.NO_BORDER);
      canvases[PdfPTable.BACKGROUNDCANVAS].rectangle(rect);
   }

   public Color getRowBackgroundColor() {
      return rowBackgroundColor;
   }

   public Color getEvenColor() {
      return evenColor;
   }

   public Color getOddColor() {
      return oddColor;
   }

   public void setEvenColor(Color evenColor) {
      this.evenColor = evenColor;
   }

   public void setOddColor(Color oddColor) {
      this.oddColor = oddColor;
   }

   public void setAlternate(int alternate) {
      this.alternate = alternate;
   }

   @Override
   public void setLayerManager(LayerManager layerManager) {
      this.layerManager = layerManager;
   }

   public void setSettings(EnhancedMap settings) {
      this.settings = settings;
      itextHelper=new ItextHelper();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(itextHelper, settings);
   }

   public EnhancedMap getSettings() {
      return settings;
   }
}
