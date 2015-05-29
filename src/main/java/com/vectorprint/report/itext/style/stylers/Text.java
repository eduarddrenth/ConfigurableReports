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
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.debug.DebugHelper;
import static com.vectorprint.report.itext.style.stylers.Image.ROTATE;
import java.awt.Color;

/**
 * print text near a chunk with a generic tag
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Text extends AbstractPositioning<String> {

   public Text() {
      initParams();
   }

   private void initParams() {
      addParameter(new IntParameter(SIZE_PARAM, "fontsize").setDefault(com.itextpdf.text.Font.DEFAULTSIZE),Text.class);
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb").setDefault(Color.BLACK),Text.class);
      addParameter(new StringParameter(Font.FAMILY_PARAM, "string").setDefault(FontFactory.HELVETICA),Text.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(ROTATE, "float"),Text.class);
   }

   public Text(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(document, writer, settings);
      initParams();
   }

   @Override
   protected void draw(PdfContentByte canvas, float x, float y, float width, float height, String genericTag) throws VectorPrintException {
      BaseFont bf = FontFactory.getFont(getAlias()).getBaseFont();
      if (bf == null) {
         throw new VectorPrintRuntimeException("font " + getAlias() + " does not have a basefont, check your fontloading");
      }
      canvas.setFontAndSize(bf, getSize());
      canvas.setColorFill(itextHelper.fromColor((isDrawShadow())?getShadowColor():getColor()));
      canvas.setColorStroke(itextHelper.fromColor((isDrawShadow())?getShadowColor():getColor()));
      canvas.beginText();
      canvas.showTextAligned(Element.ALIGN_LEFT, getData(), x, y, getRotate());
      canvas.endText();
      if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.DEBUG)) {
         DebugHelper.styleLink(canvas, getStyleClass(),
             " (styling)",
             x, y, getSettings(), getLayerManager());
      }
   }

   public int getSize() {
      return getValue(SIZE_PARAM, Integer.class);
   }

   public void setSize(int size) {
      setValue(SIZE_PARAM, size);
   }

   public Color getColor() {
      return getValue(COLOR_PARAM, Color.class);
   }

   public void setColor(Color color) {
      setValue(COLOR_PARAM, color);
   }

   public String getAlias() {
      return getValue(Font.FAMILY_PARAM, String.class);
   }

   public void setAlias(String alias) {
      setValue(Font.FAMILY_PARAM, alias);
   }

   public float getRotate() {
      return getValue(ROTATE, Float.class);
   }

   public void setRotate(float rotate) {
      setValue(ROTATE, rotate);
   }
   @Override
   public String getHelp() {
      return "Draw text at a position or near text or an image. " + super.getHelp(); 
   }
}
