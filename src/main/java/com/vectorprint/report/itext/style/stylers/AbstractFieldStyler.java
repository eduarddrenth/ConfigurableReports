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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PushbuttonField;
import com.itextpdf.text.pdf.RadioCheckField;
import com.itextpdf.text.pdf.TextField;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.StringArrayParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.URLParameter;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.ElementProducing;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.debug.DebuggablePdfPCell;
import com.vectorprint.report.itext.style.BaseStyler;

import com.vectorprint.report.itext.style.FormFieldStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.parameters.FieldTypeParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for drawing form fields on the canvas of a cell.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractFieldStyler extends AbstractPositioning<Object> implements FormFieldStyler, ElementProducing {

   public static final String FIELDTYPE_PARAM = "fieldtype";
   public static final String NAME_PARAM = "name";
   public static final String VALUES_PARAM = "values";
   private ElementProducer elementProducer;
   private StylerFactory stylerFactory;
   private BaseField bf = null;

   public AbstractFieldStyler() {
      addParameter(new FieldTypeParameter(FIELDTYPE_PARAM, Arrays.asList(FIELDTYPE.values()).toString()).setDefault(FIELDTYPE.TEXT));
      addParameter(new FloatParameter(DocumentSettings.HEIGHT, "the height of the field"));
      addParameter(new FloatParameter(DocumentSettings.WIDTH, "the width of the field"));
      addParameter(new StringArrayParameter(VALUES_PARAM, "the value(s) for the form field"));
      addParameter(new StringParameter(NAME_PARAM, "the name of the form field"));
      addParameter(new URLParameter(Image.URLPARAM, "the url for posting"));
      getParameter(USEPADDING, Boolean.class).setDefault(Boolean.TRUE);
   }

   private static final Class<Object>[] classes = new Class[]{PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   @Override
   public String getHelp() {
      return "base class for styling form fields to pdf. " + super.getHelp();
   }

   @Override
   public FIELDTYPE getFieldtype() {
      return getValue(FIELDTYPE_PARAM, FIELDTYPE.class);
   }

   public void setFieldtype(FIELDTYPE fieldtype) {
      setValue(FIELDTYPE_PARAM, fieldtype);
   }

   public String getName() {
      return getValue(NAME_PARAM, String.class);
   }

   public void setName(String name) {
      setValue(NAME_PARAM, name);
   }

   public float getHeight() {
      return getValue(DocumentSettings.HEIGHT, Float.class);
   }

   public void setHeight(float height) {
      setValue(DocumentSettings.HEIGHT, height);
   }

   /**
    * creates the correct BaseField based on the {@link FIELDTYPE} of this styler.
    *
    * @return
    * @throws VectorPrintException
    */
   @Override
   public BaseField create() throws VectorPrintException {
      switch (getFieldtype()) {
         case TEXT:
         case COMBO:
         case LIST:
            return new TextField(getWriter(), null, getName());
         case BUTTON:
            return new PushbuttonField(getWriter(), null, getName());
         case CHECKBOX:
         case RADIO:
            return new RadioCheckField(getWriter(), null, getName(), "Yes");
      }
      throw new VectorPrintException("unable to create BaseField for " + getFieldtype());
   }

   /**
    * calls {@link #create() } and {@link #setFieldValues(com.itextpdf.text.pdf.BaseField) } and registers this
    * FormFieldStyler as a {@link PdfPCellEvent} when element is null or when the {@link DebuggablePdfPCell} does not
    * contain a {@link BaseField} yet. Subclasses should first call this style method and after this call
    * {@link #getFromCell(java.lang.Object)} to get hold of the BaseField and style it. The BaseField is drawn on the
    * {@link PdfPTable#TEXTCANVAS} of the cell.
    *
    * @param <E>
    * @param element
    * @param data
    * @return
    * @throws VectorPrintException
    */
   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      if (stylerFactory.getDocumentStyler().getValue(DocumentSettings.PDFA, Boolean.class)) {
         throw new VectorPrintRuntimeException("forms not supported in PDF/X-1a ");
      }
      bf = getFromCell(element);
      if (bf == null) {
         bf = create();
         setFieldValues();
         ((PdfPCell) element).setCellEvent(this);
      }
      return element;
   }

   /**
    * Set the value(s) for the BaseField, based on {@link FIELDTYPE}.
    *
    * @see #VALUES_PARAM
    */
   @Override
   public void setFieldValues() {
      String[] values = getValue(VALUES_PARAM, String[].class);
      switch (getFieldtype()) {
         case TEXT:
            if (values.length > 0) {
               ((TextField) bf).setText(values[0]);
            }
            break;
         case COMBO:
            if (values.length > 0) {
               ((TextField) bf).setChoices(values);
            }
            break;
         case LIST:
            if (values.length > 0) {
               ((TextField) bf).setChoices(values);
            }
            break;
         case BUTTON:
            ((PushbuttonField) bf).setText(values[0]);
            break;
         case CHECKBOX:
            if (values.length > 0) {
               ((RadioCheckField) bf).setOnValue(values[0]);
            }
            break;
         case RADIO:
            if (values.length > 0) {
               ((RadioCheckField) bf).setOnValue(values[0]);
            }
            break;
      }
   }

   /**
    * Create the PdfFormField that will be used to add a form field to the pdf.
    *
    * @return
    * @throws IOException
    * @throws DocumentException
    * @throws VectorPrintException
    */
   @Override
   public PdfFormField makeField() throws IOException, DocumentException, VectorPrintException {

      switch (getFieldtype()) {
         case TEXT:
            return ((TextField) bf).getTextField();
         case COMBO:
            return ((TextField) bf).getComboField();
         case LIST:
            return ((TextField) bf).getListField();
         case BUTTON:
            return ((PushbuttonField) bf).getField();
         case CHECKBOX:
            return ((RadioCheckField) bf).getCheckField();
         case RADIO:
            return ((RadioCheckField) bf).getRadioField();
      }
      throw new VectorPrintException(String.format("cannot create pdfformfield from %s and %s", (bf != null) ? bf.getClass() : null, String.valueOf(getFieldtype())));
   }

   @Override
   protected void draw(PdfContentByte canvas, float x, float y, float width, float height, String genericTag) throws VectorPrintException {
      StyleHelper styleHelper = elementProducer.getStyleHelper();
      Rectangle box = new Rectangle(x, y, x + width, y - height);
      PdfFormField pff = null;
      if (getValue(DocumentSettings.WIDTH, Float.class) > 0) {
         box.setRight(box.getLeft() + getValue(DocumentSettings.WIDTH, Float.class));
      }
      if (getValue(DocumentSettings.HEIGHT, Float.class) > 0) {
         float diff = box.getHeight() - getValue(DocumentSettings.HEIGHT, Float.class);
         box.setBottom(box.getBottom() + diff / 2);
         box.setTop(box.getTop() - diff / 2);
      }
      bf.setBox(box);
      try {
         List<BaseStyler> stylers = stylerFactory.getStylers(getStyleClass());
         List<FormFieldStyler> ffStylers = StyleHelper.getStylers(stylers, FormFieldStyler.class);
         pff = makeField();
         if (FormFieldStyler.FIELDTYPE.BUTTON.equals(getFieldtype())) {
            for (FormFieldStyler f : ffStylers) {
               if (f.isParameterSet(Image.URLPARAM)) {
                  pff.setAction(PdfAction.createSubmitForm(f.getValue(Image.URLPARAM, URL.class).toString(), null, PdfAction.SUBMIT_HTML_FORMAT));
                  break;
               }
            }
         }
      } catch (IOException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (DocumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (VectorPrintException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
      getWriter().addAnnotation(pff);
      if (stylerFactory.getSettings().getBooleanProperty(DEBUG, false)) {
         DebugHelper.debugRect(canvas, box, new float[]{2, 2}, 0.7f,
             stylerFactory.getSettings(), elementProducer);
         DebugHelper.styleLink(canvas, getStyleClass(), "", box.getLeft(), box.getTop(),
             stylerFactory.getSettings(), elementProducer);
      }
   }

   @Override
   public void setElementProducer(ElementProducer elementProducer) {
      this.elementProducer = elementProducer;
   }

   @Override
   public void setStylerFactory(StylerFactory stylerFactory) {
      this.stylerFactory = stylerFactory;
   }

   @Override
   public BaseField getBaseField() {
      return bf;
   }

   protected ElementProducer getElementProducer() {
      return elementProducer;
   }

   protected StylerFactory getStylerFactory() {
      return stylerFactory;
   }

   protected BaseField getBf() {
      return bf;
   }

   @Override
   public BaseField getFromCell(Object element) {
      if (element instanceof DebuggablePdfPCell) {
         return ((DebuggablePdfPCell) element).getBaseField();
      } else {
         return null;
      }
   }

   @Override
   public final EVENTMODE getEventmode() {
      return EVENTMODE.NONE;
   }

}
