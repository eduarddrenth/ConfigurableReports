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
import com.vectorprint.report.itext.annotations.ContainerStart;
import com.vectorprint.report.itext.annotations.Element;
import com.vectorprint.report.itext.annotations.MultipleFromData;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@ContainerStart(
    containerType = CONTAINER_ELEMENT.TABLE,
    styleClasses = "table"
)
@MultipleFromData(
    dataListMethod = "getData",
    element = @Element(iTextClass = PdfPCell.class,styleClasses = "kop")
    )
@ContainerEnd(containerType = CONTAINER_ELEMENT.TABLE)
public class MyTableFromData {
   
   public List<String> getData() {
      return Arrays.asList(new String[] {"1","2","3","4"});
   }


}
