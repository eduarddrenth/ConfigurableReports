
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

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.style.parameters.AlignParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import static com.vectorprint.report.itext.style.stylers.DocumentSettings.WIDTH;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * align images in a cell, needed because horizontal align does not work for images in cells.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ImageAlign extends AbstractStyler  {

   public ImageAlign() {

      addParameter(new FloatParameter(WIDTH, "width of cell").setDefault(-1f),ImageAlign.class);
      addParameter(new AlignParameter(ALIGNPARAM, Arrays.asList(ALIGN.values()).toString()).setDefault(ALIGN.CENTER_MIDDLE),ImageAlign.class);
   }

   public PdfPCell style(PdfPCell cell, Object data) throws VectorPrintException {
      Image img = (Image) data;

      if (getWidth() <= 0) {
         throw new VectorPrintException("You need to specify width of the cell");
      }

      cell.setVerticalAlignment(getAlign().getVertical());

      float paddingLeft = (getWidth() - img.getWidth()) / 2;
      if (getAlign().getHorizontal()==PdfPCell.ALIGN_LEFT) {
         paddingLeft=0;
      } else if (getAlign().getHorizontal()==PdfPCell.ALIGN_RIGHT) {
         paddingLeft = (getWidth() - img.getWidth());
      }

      cell.setPaddingLeft(paddingLeft);

      return cell;
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof PdfPCell) {
         return (E) style((PdfPCell) text, data);
      }

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public ALIGN getAlign() {
      return getValue(ALIGNPARAM, ALIGN.class);
   }

   public void setAlign(ALIGN align) {
      setValue(ALIGNPARAM, align);
   }

   public float getWidth() {
      return getValue(WIDTH, Float.class);
   }

   public void setWidth(float width) {
      setValue(WIDTH, width);
   }
   @Override
   public String getHelp() {
      return "Align an image in a cell." + " " + super.getHelp(); 
   }
}
