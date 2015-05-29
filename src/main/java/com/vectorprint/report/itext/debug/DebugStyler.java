
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.style.stylers.AdvancedImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsible for providing visual debugging feedback for tables and cells and for collecting styling information
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DebugStyler<DATATYPE> extends AdvancedImpl<DATATYPE> {

   List<String> styleSetup = new ArrayList<String>(100);

   private boolean doSet = true;

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (doSet) {
         doSet = false;
         if (text instanceof PdfPCell) {
            ((PdfPCell) text).setCellEvent(new CellAndTableDebugger(styleSetup, getSettings(), this));
         } else if (text instanceof PdfPTable) {
            ((PdfPTable) text).setTableEvent(new CellAndTableDebugger(styleSetup, getSettings(), this));
         }
      }

      return super.style(text, data);
   }

   public List<String> getStyleSetup() {
      return styleSetup;
   }
   private static final Class<Object>[] classes = new Class[]{PdfPCell.class, PdfPTable.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   @Override
   public void draw(Rectangle rect, String genericTag) throws VectorPrintException {
      if (getSettings().getBooleanProperty(Boolean.FALSE, DEBUG)) {
         PdfContentByte canvas = getWriter().getDirectContent();

         DebugHelper.debugRect(canvas, rect, new float[]{1, 3}, 0.3f, getSettings(), getLayerManager());

         DebugHelper.styleLink(canvas, DebugHelper.getFirstNotDefaultStyleClass(styleSetup),
             "",
             rect.getLeft(), rect.getTop(), getSettings(), getLayerManager());
      }
   }

   public LayerManager getLm() {
      return getLayerManager();
   }

   @Override
   public boolean shouldStyle(Object data, Object element) {
      return true;
   }
}
