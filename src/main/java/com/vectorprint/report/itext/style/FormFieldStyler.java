package com.vectorprint.report.itext.style;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.debug.DebuggablePdfPCell;
import com.vectorprint.report.itext.style.stylers.Advanced;
import java.io.IOException;

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


/**
 * Responsible for drawing a form field based on the position of a table cell.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface FormFieldStyler extends Advanced<Object> {

   /**
    * Creates the correct BaseField based on the {@link FIELDTYPE} of this styler. The FormFieldStyler in the chain of stylers that created the
    * BaseField should be registered as a cell event to the PdfPCell being styled. Subsequent stylers can call {@link #getFromCell(java.lang.Object) } 
    * to get hold of the BaseField and apply their styling to it. When the cell event fires {@link #makeField() } can be used to create the actual form field.
    *
    *
    * @return
    * @throws VectorPrintException
    */
   BaseField create() throws VectorPrintException;

   /**
    * Create the PdfFormField from the BaseField created by {@link #create() }, called from
    * {@link PdfPCellEvent#cellLayout(com.itextpdf.text.pdf.PdfPCell, com.itextpdf.text.Rectangle, com.itextpdf.text.pdf.PdfContentByte[]) }
    *
    * @see PdfWriter#addAnnotation(com.itextpdf.text.pdf.PdfAnnotation) 
    * @return
    * @throws IOException
    * @throws DocumentException
    * @throws VectorPrintException
    */
   PdfFormField makeField() throws IOException, DocumentException, VectorPrintException;

   /**
    * Set the value(s) for the BaseField, based on {@link FIELDTYPE}.
    *
    * @see #VALUES_PARAM
    */
   void setFieldValues();

   /**
    * 
    * @param element the {@link DebuggablePdfPCell} that holds the BaseField, called by FormFieldStylers after the one that created 
    * the BaseField
    * @return 
    */
   BaseField getFromCell(Object element);

   public enum FIELDTYPE {
      TEXT, COMBO, LIST, CHECKBOX, RADIO, BUTTON
   }
   
   FIELDTYPE getFieldtype();
   
   /**
    * Called by {@link DebuggablePdfPCell} to remember the BaseField created
    * @return 
    */
   BaseField getBaseField();
   
}
