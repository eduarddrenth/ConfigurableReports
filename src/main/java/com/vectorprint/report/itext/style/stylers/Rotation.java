
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
 * set rotation for cells
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Rotation extends AbstractStyler  {

   public static final String ROTATION = "rotation";

   public Rotation() {

      addParameter(new IntParameter(ROTATION, "0, 90, 180 or 270"),Rotation.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      PdfPCell cell = (PdfPCell) text;

      cell.setRotation(getRotation());

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public int getRotation() {
      return getValue(ROTATION, Integer.class);
   }

   public void setRotation(int rotation) {
      setValue(ROTATION, rotation);
   }
   @Override
   public String getHelp() {
      return "Specify rotation of a cell." + " " + super.getHelp();
   }
}
