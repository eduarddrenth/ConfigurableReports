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
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.RadioCheckField;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.report.itext.ItextHelper;
import static com.vectorprint.report.itext.style.BaseStyler.COLOR_PARAM;
import static com.vectorprint.report.itext.style.BaseStyler.SIZE_PARAM;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.FontStyleParameter;
import static com.vectorprint.report.itext.style.stylers.Font.FAMILY_PARAM;
import static com.vectorprint.report.itext.style.stylers.Font.STYLE_PARAM;
import java.awt.Color;
import java.util.Arrays;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FieldFont extends AbstractFieldStyler {


   public FieldFont() {
      addParameter(new FloatParameter(SIZE_PARAM, "fontsize",false).setDefault((float)com.itextpdf.text.Font.DEFAULTSIZE));
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb").setDefault(Color.BLACK));
      addParameter(new StringParameter(FAMILY_PARAM, "alias for based on which a font is retrieved (preferred over style)").setDefault(FontFactory.HELVETICA));
      addParameter(new FontStyleParameter(STYLE_PARAM, "style for a font" + Arrays.asList(Font.STYLE.values()).toString()).setDefault(Font.STYLE.normal));
   }

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      element = super.style(element, data);
      BaseField bf = getFromCell(element);
      if (bf != null) {

         com.itextpdf.text.Font f = FontFactory.getFont(getValue(FAMILY_PARAM, String.class), getValue(SIZE_PARAM, Float.class), getValue(STYLE_PARAM, Font.STYLE.class).getStyle());
         if (f.getBaseFont() == null) {
            throw new VectorPrintRuntimeException("font " + f.getFamilyname() + " does not have a basefont, check your fontloading");
         }
         bf.setFont(f.getBaseFont());
         bf.setFontSize(getValue(SIZE_PARAM, Float.class));
         bf.setTextColor(itextHelper.fromColor(getValue(COLOR_PARAM, Color.class)));
      }
      return element;
   }

   @Override
   public String getHelp() {
      return "Font for a form field. " + super.getHelp();
   }
}
