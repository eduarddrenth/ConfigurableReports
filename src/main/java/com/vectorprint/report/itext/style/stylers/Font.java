
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

import com.itextpdf.text.Chunk;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.configuration.parameters.StringParameter;

import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.FontEncodingParameter;
import com.vectorprint.report.itext.style.parameters.FontStyleParameter;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * font settings for text (chunks and phrases)
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Font extends AbstractStyler {

   public static final String FAMILY_PARAM = "family";
   public static final String STYLE_PARAM = "style";
   public static final String FONTENCODING = "encoding";
   
   public enum ENCODING {
      IDENTITY_H(BaseFont.IDENTITY_H), WINANSI(BaseFont.WINANSI), CP1252(BaseFont.CP1252);
      private String encoding;

      private ENCODING(String encoding) {
         this.encoding = encoding;
      }

      public String getEncoding() {
         return encoding;
      }
      
   }

   public enum STYLE {

      normal(com.itextpdf.text.Font.NORMAL), bold(com.itextpdf.text.Font.BOLD), italic(com.itextpdf.text.Font.ITALIC),
      underline(com.itextpdf.text.Font.UNDERLINE), strike(com.itextpdf.text.Font.STRIKETHRU), bolditalic(com.itextpdf.text.Font.BOLDITALIC);
      private STYLE(int style) {
         this.style = style;
      }
      private int style;

      public int getStyle() {
         return style;
      }
      
   }

   public Font() {
      super();

      addParameter(new FloatParameter(SIZE_PARAM, "fontsize",false).setDefault((float)com.itextpdf.text.Font.DEFAULTSIZE));
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb").setDefault(Color.BLACK));
      addParameter(new FontStyleParameter(STYLE_PARAM, "style for a retrieved font" + Arrays.asList(STYLE.values()).toString()).setDefault(STYLE.normal));
      addParameter(new FontEncodingParameter(FONTENCODING, "encoding for a retrieved font" + Arrays.asList(ENCODING.values()).toString()).setDefault(ENCODING.WINANSI));
      addParameter(new StringParameter(FAMILY_PARAM, "alias for based on which a font is retrieved (preferred over style)").setDefault(FontFactory.HELVETICA));
   }
   
   public com.itextpdf.text.Font getFont() {
      com.itextpdf.text.Font f = FontFactory.getFont(getFamily(), getValue(FONTENCODING, ENCODING.class).getEncoding(), getSize(), getStyle().style);

      if (getColor() != null) {
         f.setColor(itextHelper.fromColor(getColor()));
      }
      return f;
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof PdfPCell) {
         return text;
      }

      com.itextpdf.text.Font f = getFont();

      if (text instanceof Phrase) {
         ((Phrase) text).setFont(f);
      } else if (text instanceof Chunk) {
         ((Chunk) text).setFont(f);
      } else if (text instanceof TextElementArray) {
         for (Object o : ((TextElementArray)text).getChunks()) {
            ((Chunk) o).setFont(f);
         }
      }

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{Chunk.class, TextElementArray.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public float getSize() {
      return getValue(SIZE_PARAM,Float.class);
   }

   public void setSize(float size) {
      setValue(SIZE_PARAM, size);
   }

   public Color getColor() {
      return getValue(COLOR_PARAM,Color.class);
   }

   public void setColor(Color color) {
      setValue(COLOR_PARAM, color);
   }

   public STYLE getStyle() {
      return getValue(STYLE_PARAM,STYLE.class);
   }

   public void setStyle(STYLE style) {
      setValue(STYLE_PARAM, style);
   }

   public String getFamily() {
      return getValue(FAMILY_PARAM,String.class);
   }

   public void setFamily(String family) {
      setValue(FAMILY_PARAM, family);
   }
   @Override
   public String getHelp() {
      return "Font definition for text. " + super.getHelp(); 
   }

}
