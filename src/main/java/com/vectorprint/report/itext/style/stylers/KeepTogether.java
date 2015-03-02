
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
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * keep a table or paragraph on one page
 * @author Eduard Drenth at VectorPrint.nl
 */
public class KeepTogether extends AbstractStyler  {

   public KeepTogether() {
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof Paragraph) {
         ((Paragraph) text).setKeepTogether(true);
      } else if (text instanceof PdfPTable) {
         ((PdfPTable) text).setKeepTogether(true);
      }
      return text;
   }
   private static final Class<Object>[] classes = new Class[]{PdfPTable.class, Paragraph.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   @Override
   public String getHelp() {
      return "Try to keep tables and paragraphs on one page. " + super.getHelp(); 
   }
}
