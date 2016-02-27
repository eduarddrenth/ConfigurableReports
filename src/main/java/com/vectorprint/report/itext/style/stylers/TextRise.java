
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
import com.itextpdf.text.TextElementArray;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------
/**
 * Support rising or lowering text (sub and superscript).
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class TextRise extends AbstractStyler {

   public static final String TEXTRISE = "textrise";

   public TextRise() {
      super();

      addParameter(new FloatParameter(TEXTRISE, "positive lifts text, negative lowers").setDefault(0f), TextRise.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {

      if (getValue(TEXTRISE, Float.class) != 0) {
         ((Chunk) text).setTextRise(getValue(TEXTRISE, Float.class));
      }

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{Chunk.class, TextElementArray.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   @Override
   public String getHelp() {
      return "supports rising/lowering chunks of text" + super.getHelp();
   }

}
