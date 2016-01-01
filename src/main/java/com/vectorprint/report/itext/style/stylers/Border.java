
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

import com.itextpdf.text.Rectangle;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ColorParameter;
import static com.vectorprint.report.itext.style.BaseStyler.COLOR_PARAM;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.PositionParameter;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * printing borders for rectangles
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Border extends AbstractStyler  {

   public static final String BORDERWIDTH = "borderwidth";

   public Border() {

      addParameter(new PositionParameter(TOPRIGTHBOTTOMLEFT_PARAM, Arrays.asList(POSITION.values()).toString()).setDefault(POSITION.TRBL),Border.class);
      addParameter(new FloatParameter(BORDERWIDTH, "borderwidth"),Border.class);
      addParameter(new ColorParameter(COLOR_PARAM, "#rgb"),Border.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      Rectangle cell = (Rectangle) text;

      cell.setBorderWidth(getValue(BORDERWIDTH, Float.class));
      cell.setBorder(getValue(TOPRIGTHBOTTOMLEFT_PARAM, POSITION.class).getPosition());

      if (getValue(COLOR_PARAM, Color.class) != null) {
         cell.setBorderColor(itextHelper.fromColor(getValue(COLOR_PARAM, Color.class)));
      }

      return text;
   }
   private static final Class<Object>[] classes = new Class[]{Rectangle.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public POSITION getBorder() {
      return getValue(TOPRIGTHBOTTOMLEFT_PARAM, POSITION.class);
   }

   public void setBorder(POSITION border) {
      setValue(TOPRIGTHBOTTOMLEFT_PARAM, border);
   }

   public Color getBorderColor() {
      return getValue(COLOR_PARAM, Color.class);
   }

   public void setBorderColor(Color borderColor) {
      setValue(COLOR_PARAM, borderColor);
   }

   public float getBorderWidth() {
      return getValue(BORDERWIDTH, Float.class);
   }

   public void setBorderWidth(float borderWidth) {
      setValue(BORDERWIDTH, borderWidth);
   }
   
   @Override
   public String getHelp() {
      return "Draw borders for rectangles." + " " + super.getHelp();
   }
}
