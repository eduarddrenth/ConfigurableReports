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

import com.itextpdf.text.pdf.BaseField;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ColorParameter;
import static com.vectorprint.report.itext.style.BaseStyler.COLOR_PARAM;
import java.awt.Color;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FieldBackground extends AbstractFieldStyler {
   

   public FieldBackground() {
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb"),FieldBackground.class);
   }   

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      element = super.style(element, data);
      BaseField bf = getFromCell(element);
      if (bf != null) {
         bf.setBackgroundColor(itextHelper.fromColor(getValue(COLOR_PARAM, Color.class)));
      }
      return element;
   }
   @Override
   public String getHelp() {
      return "Background color of a form field." + " " + super.getHelp();
   }
}
