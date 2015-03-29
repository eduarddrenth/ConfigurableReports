
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

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;

import com.vectorprint.report.itext.style.parameters.FloatParameter;
import static com.vectorprint.report.itext.style.stylers.DocumentSettings.HEIGHT;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * height of cells or phrases
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Height extends AbstractStyler  {

   public Height() {

      addParameter(new FloatParameter(HEIGHT, "leading for text, set fixed height for cells"),Height.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof PdfPCell) {
         ((PdfPCell) text).setMinimumHeight(getHeight());
      } else if (text instanceof Phrase) {
         ((Phrase) text).setLeading(getHeight());
      }

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{Phrase.class, PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public float getHeight() {
      return getValue(HEIGHT, Float.class);
   }

   public void setHeight(float height) {
      setValue(HEIGHT, height);
   }
   @Override
   public String getHelp() {
      return "Height of phrases or cells. " + super.getHelp(); 
   }
}
