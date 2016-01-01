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

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.conditions.DataNotNullCondition;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * draws a line under text
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Underline<DATATYPE> extends AdvancedImpl<DATATYPE> {

   public static final String LINEWIDTH = "linewidth";

   protected float calculateShift(float shift, com.itextpdf.text.Font f) {
      return shift * (f.getSize() / 10);
   }

   @Override
   public void draw(Rectangle rect, String genericTag) throws VectorPrintException {
      if (genericTag==null) {
         if (log.isLoggable(Level.FINE)) {
            log.fine("not drawing underline because genericTag is null (no data for underline)");
         }
         return;
      }
      DelayedData delayed = getDelayed(genericTag);
      PdfContentByte canvas = getPreparedCanvas();
      try {
         com.itextpdf.text.Font f = delayed.getChunk().getFont();
         canvas.setColorStroke((getColor() == null) ? f.getColor() : itextHelper.fromColor(getColor()));
         canvas.setLineWidth(getLineWidth());
         canvas.moveTo(rect.getLeft(), rect.getBottom());
         canvas.lineTo(rect.getLeft() + rect.getWidth(), rect.getBottom());
         canvas.stroke();
      } catch (Exception ex) {
         resetCanvas(canvas);
         throw new VectorPrintException(ex);
      }
      resetCanvas(canvas);
   }

   public Underline() {
      initParam();
      addCondition(new DataNotNullCondition());
   }

   private void initParam() {
      addParameter(new FloatParameter(LINEWIDTH, "float").setDefault(ItextHelper.mmToPts(1)),Underline.class);
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb: underline color (default is font color)"),Underline.class);
   }

   public Underline(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(document, writer, settings);
      initParam();
   }


   public Color getColor() {
      return getValue(COLOR_PARAM, Color.class);
   }

   public void setColor(Color color) {
      setValue(COLOR_PARAM, color);
   }

   public float getLineWidth() {
      return getValue(LINEWIDTH, Float.class);
   }

   public void setLineWidth(float lineWidth) {
      setValue(LINEWIDTH, lineWidth);
   }
   @Override
   public String getHelp() {
      return "Underline text." + " " + super.getHelp();
   }
   private static final Class<Object>[] classes = new Class[]{Chunk.class, TextElementArray.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
}
