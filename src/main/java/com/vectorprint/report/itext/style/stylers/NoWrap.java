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
import com.itextpdf.text.SplitCharacter;
import com.itextpdf.text.pdf.PdfChunk;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * don't wrap text in a cell
 * @author Eduard Drenth at VectorPrint.nl
 */
public class NoWrap extends AbstractStyler  {

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof PdfPCell) {
         ((PdfPCell) text).setFixedHeight(((PdfPCell) text).getPhrase().getFont().getCalculatedSize() + 3f);

         return text;
      } else  if (text instanceof Chunk) {
         ((Chunk) text).setSplitCharacter(new SplitCharacter() {
            @Override
            public boolean isSplitCharacter(int i, int i1, int i2, char[] chars, PdfChunk[] pcs) {
               return false;
            }
         });
      }

      return text;
   }
   private static final Class<Object>[] classes = new Class[]{PdfPCell.class, Chunk.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }
   
   @Override
   public String getHelp() {
      return "Prevent wrapping of text." + " " + super.getHelp();
   }
}
