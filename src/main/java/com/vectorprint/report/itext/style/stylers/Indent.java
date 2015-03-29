
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
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;

import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * indentation for sections, paragraphs or cells
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Indent extends AbstractStyler  {

   public static final String INDENT_PARAM = "indent";
   public static final String INDENT_LEFT_PARAM = "indentleft";
   public static final String INDENT_RIGHT_PARAM = "indentright";

   public Indent() {
      addParameter(new FloatParameter(INDENT_PARAM, "float"),Indent.class);
      addParameter(new FloatParameter(INDENT_LEFT_PARAM, "float"),Indent.class);
      addParameter(new FloatParameter(INDENT_RIGHT_PARAM, "float"),Indent.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof Section) {
         Section cell = (Section) text;

         cell.setIndentationLeft(getIndentLeft());
         cell.setIndentationRight(getIndentRight());
      } else if (text instanceof Paragraph) {
         Paragraph par = (Paragraph) text;

         par.setIndentationLeft(getIndentLeft());
         par.setIndentationRight(getIndentRight());

      } else if (text instanceof List) {
         ((List)text).setIndentationLeft(getIndentLeft());
         ((List)text).setIndentationLeft(getIndentRight());
      } else if (text instanceof ListItem) {
         ((com.itextpdf.text.ListItem)text).setIndentationLeft(getIndentLeft());
         ((com.itextpdf.text.ListItem)text).setIndentationLeft(getIndentRight());
      } else if (text instanceof PdfPCell) {
         ((PdfPCell) text).setIndent(getIndentLeft());
      }
      return text;
   }
   private static final Class<Object>[] classes = new Class[]{Section.class, Paragraph.class, PdfPCell.class, List.class, com.itextpdf.text.ListItem.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public float getIndentLeft() {
      float i = getValue(INDENT_PARAM,Float.class);
      float l = getValue(INDENT_LEFT_PARAM,Float.class);
      return (i>l)?i:l;
   }

   public void setIndentLeft(float indentLeft) {
      setValue(INDENT_LEFT_PARAM, indentLeft);
   }

   public float getIndentRight() {
      return getValue(INDENT_RIGHT_PARAM,Float.class);
   }

   public void setIndentRight(float indentRight) {
      setValue(INDENT_RIGHT_PARAM, indentRight);
   }
   @Override
   public String getHelp() {
      return "Specify indentation for paragraphs, sections, cells, etc.. " + super.getHelp(); 
   }
}
