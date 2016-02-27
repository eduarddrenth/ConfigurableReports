/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.running;

import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;
import com.vectorprint.report.itext.annotations.ContainerEnd;
import com.vectorprint.report.itext.annotations.Element;
import com.vectorprint.report.itext.annotations.Elements;
import com.vectorprint.report.itext.annotations.GetData;

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
/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@Elements(
    elements = {
       @Element(
           iTextClass = Image.class,
           styleClasses = {"inlineimage"}
       ),
       @Element(
           iTextClass = Phrase.class,
         dataFunction = @GetData (getValueAsStringMethod = "txt")
       )
    }
)
@ContainerEnd(
    containerType = CONTAINER_ELEMENT.PARAGRAPH
)
public class MyInlineImage {

   @Override
   public String toString() {
      return "not needed";
   }
   
   public String txt() {
      return " De rest van de lange text waar het plaatje in zit";
   }

}
