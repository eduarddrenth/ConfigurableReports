
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

import com.itextpdf.text.Phrase;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * specify leading (height) for phrases
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Leading extends AbstractStyler  {

   public static final String LEADINGPARAM = "leading";

   public Leading() {

      addParameter(new FloatParameter(LEADINGPARAM, "the leading (height) of text"),Leading.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      ((Phrase) text).setLeading(getLeading());
      return text;
   }
   private static final Class<Object>[] classes = new Class[]{Phrase.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public float getLeading() {
      return getValue(LEADINGPARAM, Float.class);
   }

   public void setLeading(float leading) {
      setValue(LEADINGPARAM, leading);
   }
   @Override
   public String getHelp() {
      return "Specify leading (height) of phrases." + " " + super.getHelp();
   }
}
