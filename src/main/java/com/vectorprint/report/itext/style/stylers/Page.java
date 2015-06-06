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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.awt.Color;
import java.util.logging.Level;

/**
 * change page settings, size, margins and background. Size and margins are effective from the page following the
 * current page, background is effective for the current page only
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Page extends AdvancedImpl<Object> {

   public Page() {
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb (background of page MAY HIDE CONTENT!)"),Page.class);

      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_top.name(), "float"),Page.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_right.name(), "float "),Page.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_bottom.name(), "float "),Page.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_left.name(), "float "),Page.class);
      addParameter(new FloatParameter(DocumentSettings.WIDTH, "float ").setDefault(ItextHelper.mmToPts(210f)),Page.class);
      addParameter(new FloatParameter(DocumentSettings.HEIGHT, "float ").setDefault(ItextHelper.mmToPts(297f)),Page.class);

   }

   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      pageSettings();
      return element;
   }

   private void pageSettings() {
      if (getBackground() != null) {
         if (log.isLoggable(Level.FINE)) {
            log.fine("Possibly page background is written on top of page content making it invisible");
         }
         PdfContentByte bg = getWriter().getDirectContentUnder();
         Rectangle rect = getWriter().getPageSize();
         rect.setBackgroundColor(itextHelper.fromColor(getBackground()));
         bg.rectangle(rect);
         bg.closePathFillStroke();
      }
      getDocument().setPageSize(new Rectangle(getWidth(), getHeight()));
      getDocument().setMargins(getMargin_left(), getMargin_right(), getMargin_top(), getMargin_bottom());
   }

   @Override
   public void draw(Rectangle rect, String genericTag) throws VectorPrintException {
      pageSettings();
      super.draw(rect, genericTag);
   }

   public Color getBackground() {
      return getValue(COLOR_PARAM, Color.class);
   }

   public void setBackground(Color background) {
      setValue(COLOR_PARAM, background);
   }

   public float getMargin_top() {
      return getValue(ReportConstants.MARGIN.margin_top.name(), Float.class);
   }

   public void setMargin_top(float margin_top) {
      setValue(ReportConstants.MARGIN.margin_top.name(), margin_top);
   }

   public float getMargin_right() {
      return getValue(ReportConstants.MARGIN.margin_right.name(), Float.class);
   }

   public void setMargin_right(float margin_right) {
      setValue(ReportConstants.MARGIN.margin_right.name(), margin_right);
   }

   public float getMargin_bottom() {
      return getValue(ReportConstants.MARGIN.margin_bottom.name(), Float.class);
   }

   public void setMargin_bottom(float margin_bottom) {
      setValue(ReportConstants.MARGIN.margin_bottom.name(), margin_bottom);
   }

   public float getMargin_left() {
      return getValue(ReportConstants.MARGIN.margin_left.name(), Float.class);
   }

   public void setMargin_left(float margin_left) {
      setValue(ReportConstants.MARGIN.margin_left.name(), margin_left);
   }

   public float getWidth() {
      return getValue(DocumentSettings.WIDTH, Float.class);
   }

   public void setWidth(float width) {
      setValue(DocumentSettings.WIDTH, width);
   }

   public float getHeight() {
      return getValue(DocumentSettings.HEIGHT, Float.class);
   }

   public void setHeight(float height) {
      setValue(DocumentSettings.HEIGHT, height);
   }

   @Override
   public String getHelp() {
      return "Specify settings for the pages following. " + super.getHelp();
   }
}
