package com.vectorprint.report.itext.annotations;

/*
 * #%L
 * VectorPrintReport
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


import com.itextpdf.text.Anchor;
import com.itextpdf.text.Element;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;

/**
 * A choice of possible container elements, elements that can hold others.
 * @author Eduard Drenth at VectorPrint.nl
 */
public enum CONTAINER_ELEMENT {

   COLUMS(SimpleColumns.class),
   /**
    * if you want to start a nested table use {@link CONTAINER_ELEMENT#NESTED_TABLE}
    */
   TABLE(PdfPTable.class),
   /**
    * if you want to start a nested table use {@link CONTAINER_ELEMENT#NESTED_TABLE}
    */
   CELL(PdfPCell.class),
   /**
    * use this if you want to start a nested table, the combination of {@link CONTAINER_ELEMENT#CELL} and {@link CONTAINER_ELEMENT#TABLE} won't work
    */
   NESTED_TABLE(PdfPCell.class),
   /**
    * a chapter or section, see {@link ContainerStart#sectionLevel() } and {@link ElementProducer#getIndex(java.lang.String, int, java.util.Collection)
    * }. The title for a section will be taken from the data
    */
   SECTION(Section.class),
   PARAGRAPH(Paragraph.class),
   PHRASE(Phrase.class),
   ANCHOR(Anchor.class),
   LIST(List.class),
   LISTITEM(ListItem.class),
   /**
    * needed as default for {@link Element#startContainer() }
    */
   NOCONTAINER(Element.class);
   private Class iTextClass;

   private CONTAINER_ELEMENT(Class iTextClass) {
      this.iTextClass = iTextClass;
   }

   /**
    * The iText class that is going to be used as container.
    * @return 
    */
   public Class getiTextClass() {
      return iTextClass;
   }
}
