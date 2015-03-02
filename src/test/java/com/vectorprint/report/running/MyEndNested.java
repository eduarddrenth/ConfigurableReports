/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.running;

/*
 * #%L
 * VectorPrintReport
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

import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;
import com.vectorprint.report.itext.annotations.ContainerEnd;
import com.vectorprint.report.itext.annotations.Element;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@ContainerEnd(containerType = CONTAINER_ELEMENT.NESTED_TABLE)
@Element(
    iTextClass = PdfPCell.class,
    styleClasses = "kop"
)
public class MyEndNested {

   @Override
   public String toString() {
      return "END";
   }

   
}
