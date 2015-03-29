
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

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * background color for rectangles
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Background extends AbstractStyler  {


   public Background() {

      addParameter(new ColorParameter(COLOR_PARAM, "#rgb"),Background.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      Rectangle cell = (Rectangle) text;

      if (getBackgroundColor() != null) {
         cell.setBackgroundColor(itextHelper.fromColor(getBackgroundColor()));
      }

      return text;
   }
   private static final Class<Object>[] classes = new Class[]{Rectangle.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public Color getBackgroundColor() {
      return getValue(COLOR_PARAM, Color.class);
   }

   public void setBackgroundColor(Color backgroundColor) {
      setValue(COLOR_PARAM, backgroundColor);
   }

   @Override
   public String getHelp() {
      return "Background color for rectangles. " + super.getHelp(); 
   }
}
