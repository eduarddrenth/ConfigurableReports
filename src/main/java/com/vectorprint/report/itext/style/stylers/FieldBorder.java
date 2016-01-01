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
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ColorParameter;
import static com.vectorprint.report.itext.style.BaseStyler.COLOR_PARAM;
import static com.vectorprint.report.itext.style.BaseStyler.TOPRIGTHBOTTOMLEFT_PARAM;
import com.vectorprint.report.itext.style.parameters.BorderStyleParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.PositionParameter;
import static com.vectorprint.report.itext.style.stylers.Border.BORDERWIDTH;
import java.awt.Color;
import java.util.Arrays;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FieldBorder extends AbstractFieldStyler {

   public static final String BORDERSTYLEPARAM = "borderstyle";

   public enum BORDERSTYLE {

      SOLID(PdfBorderDictionary.STYLE_SOLID),
      DASHED(PdfBorderDictionary.STYLE_DASHED),
      BEVELED(PdfBorderDictionary.STYLE_BEVELED),
      INSET(PdfBorderDictionary.STYLE_INSET),
      UNDERLINE(PdfBorderDictionary.STYLE_UNDERLINE);

      private BORDERSTYLE(int style) {
         this.style = style;
      }
      private int style;

      public int getStyle() {
         return style;
      }

   }

   public FieldBorder() {
      addParameter(new PositionParameter(TOPRIGTHBOTTOMLEFT_PARAM, Arrays.asList(POSITION.values()).toString()).setDefault(POSITION.NONE),FieldBorder.class);
      addParameter(new FloatParameter(BORDERWIDTH, "borderwidth"),FieldBorder.class);
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb"),FieldBorder.class);
      addParameter(new BorderStyleParameter(BORDERSTYLEPARAM, "style of the field border").setDefault(BORDERSTYLE.SOLID),FieldBorder.class);
   }

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      element = super.style(element, data);
      BaseField bf = getFromCell(element);
      if (bf != null) {
         bf.setBorderColor(itextHelper.fromColor(getValue(COLOR_PARAM, Color.class)));
         bf.setBorderWidth(getValue(BORDERWIDTH, Float.class));
         bf.setBorderStyle(getValue(BORDERSTYLEPARAM, BORDERSTYLE.class).getStyle());
      }
      return element;
   }

   @Override
   public String getHelp() {
      return "Border of a form field." + " " + super.getHelp();
   }
}
