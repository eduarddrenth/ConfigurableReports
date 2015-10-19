/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.mappingconfig;

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
import com.itextpdf.text.Chapter;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;
import com.vectorprint.report.itext.mappingconfig.model.DataConfig;
import com.vectorprint.report.itext.mappingconfig.model.ElementConfig;
import com.vectorprint.report.itext.mappingconfig.model.StartContainerConfig;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import com.vectorprint.report.itext.style.stylers.Table;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractDatamappingProcessor implements DatamappingProcessor {

   private static final Logger log = Logger.getLogger(AbstractDatamappingProcessor.class.getName());

   protected void removePreviousChapter(Deque<Element> stack) throws DocumentException {
      Element toRemove;
      while ((toRemove = stack.peek()) != null) {
         if (toRemove instanceof Chapter) {
            stack.remove();
            getDocument().add(toRemove);
            break;
         } else {
            stack.remove();
         }
      }
   }

   public static boolean nullOrEmpty(String s) {
      return null == s || s.isEmpty();
   }

   @Override
   public CONTAINER_ELEMENT getType(StartContainerConfig cs, Object data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, VectorPrintException {
      if (nullOrEmpty(cs.getContainertypemethod())) {
         return cs.getContainertype();
      } else {
         Class dataClass = data.getClass();
         Method m = dataClass.getMethod(cs.getContainertypemethod(), null);
         Object res = m.invoke(data, null);
         if (res instanceof CONTAINER_ELEMENT) {
            return (CONTAINER_ELEMENT) res;
         } else {
            throw new VectorPrintException(String.format("%s should return a CONTAINER_ELEMENT", cs.getContainertypemethod()));
         }
      }
   }

   @Override
   public Class<? extends Element> getType(ElementConfig e, Object data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, VectorPrintException {
      if (nullOrEmpty(e.getElementtypemethod())) {
         return e.getElementtype();
      } else {
         Class dataClass = data.getClass();
         Method m = dataClass.getMethod(e.getElementtypemethod(), null);
         Object res = m.invoke(data, null);
         if (res instanceof Class && Element.class.isAssignableFrom((Class) res)) {
            return (Class<? extends Element>) res;
         } else {
            throw new VectorPrintException(String.format("%s should return a subclass of Element", e.getElementtypemethod()));
         }
      }
   }

   private static String[] getStyleClasses(DataConfig cs, Object data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      List<String> styleClassesList = cs.getStyleclasses();
      if (!nullOrEmpty(cs.getStyleclassesmethod()) && data != null) {
         Method m = data.getClass().getMethod(cs.getStyleclassesmethod(), null);
         styleClassesList = Arrays.asList((String[]) m.invoke(data));
      }
      return (styleClassesList != null) ? styleClassesList.toArray(new String[styleClassesList.size()]) : new String[]{};
   }

   private class ToBeAdded {

      private final PdfPTable parentTable;
      private final PdfPTable childTable;
      private final PdfPCell cellWithTable;

      public ToBeAdded(PdfPTable parentTable, PdfPTable childTable, PdfPCell cellWithTable) {
         this.parentTable = parentTable;
         this.childTable = childTable;
         this.cellWithTable = cellWithTable;
      }

   }

   @Override
   public void addContainer(StartContainerConfig cs, Deque containers, Object data) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
      if (cs != null) {
         Object currentParent = containers.peek();
         String[] styleClasses = getStyleClasses(cs, data);
         CONTAINER_ELEMENT ce = getType(cs, data);
         if (ce.equals(CONTAINER_ELEMENT.SECTION)) {
            if (cs.getSectionlevel() == 1) {
               // now remove the previous chapters and sections
               endContainer(CONTAINER_ELEMENT.SECTION, 1, containers);
               removePreviousChapter(containers);
            }
            // sections are linked already
            currentParent = null;
            containers.push(getIndex(String.valueOf(data), cs.getSectionlevel(), getStylers(styleClasses)));
         } else if (ce.equals(CONTAINER_ELEMENT.NESTED_TABLE)) {
            // create the nested container
            PdfPTable t = createElement(null, PdfPTable.class, getStylers(styleClasses));
            if (t == null) {
               throw new VectorPrintException(String.format("Your styling needs to include %s", Table.class.getSimpleName()));
            }
            // create the cell containing the table
            PdfPCell cell = createElement(t, PdfPCell.class, getStylers(styleClasses));
            // remember the nested table, its cell and parent table
            containers.push(new ToBeAdded((PdfPTable) currentParent, t, cell));
            // linked already and later
            currentParent = null;
         } else {
            Object d = (cs.isAdddata()) ? determineData(data, cs) : null;
            if (ce.equals(CONTAINER_ELEMENT.COLUMS)) {
               containers.push(createColumns(getStylers(styleClasses)));
            } else {
               containers.push(createElement(d, ce.getiTextClass(), getStylers(styleClasses)));
            }
         }
         if (currentParent != null) {
            tryAdd(currentParent, containers.peek(), containers);
         }
      }
   }

   private void tryAdd(Object container, Object element, Deque stack) throws VectorPrintException, DocumentException {
      if (container instanceof ToBeAdded) {
         container = ((ToBeAdded) container).childTable;
      }
      if (container instanceof Element) {
         if (element instanceof Element) {
            addToContainer((Element) container, (Element) element);
         } else {
            log.fine(String.format("not adding %s to %s", element.getClass().getName(), container.getClass().getName()));
         }
         return;
      } else if (container instanceof SimpleColumns) {
         SimpleColumns sc = (SimpleColumns) container;
         boolean needStackRefresh = sc.willWriteAfterNextAdd() && !(stack.getLast() instanceof SimpleColumns);
         if (needStackRefresh) {
            getDocument().add((Element) stack.getLast());
            stack.clear();
         }
         sc.addContent(element);
         if (needStackRefresh) {
            stack.add(sc);
         }
         return;
      }
      throw new VectorPrintException(String.format("don't know how to add %s to %s", container.getClass().getName(), element.getClass().getName()));
   }

   @Override
   public void addElement(ElementConfig elementAnnotation, Deque containers, Object data) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
      if (elementAnnotation != null) {
         // create the element
         Element toAdd = createElement(determineData(data, elementAnnotation), getType(elementAnnotation, data), getStylers(getStyleClasses(elementAnnotation, data)));
         // add to document or current container
         if (containers.isEmpty()) {
            getDocument().add(toAdd);
         } else {
            tryAdd(containers.peek(), toAdd, containers);
         }
      }
   }

   /**
    * attempts to add an element to a container
    *
    * @param container
    * @param element
    * @throws DocumentException
    */
   @Override
   public void addToContainer(Element container, Element element) throws DocumentException, VectorPrintException {
      if (container instanceof TextElementArray) {
         ((TextElementArray) container).add(element);
      } else if (container instanceof PdfPTable) {
         if (element instanceof PdfPCell) {
            ((PdfPTable) container).addCell((PdfPCell) element);
         } else if (element instanceof Phrase) {
            ((PdfPTable) container).addCell((Phrase) element);
         } else if (element instanceof PdfPTable) {
            ((PdfPTable) container).addCell((PdfPTable) element);
         } else if (element instanceof Image) {
            ((PdfPTable) container).addCell((Image) element);
         } else {
            throw new VectorPrintException(String.format("don't know how to add %s to %s", (element == null) ? "null" : element.getClass().getName(), (container == null) ? "null" : container.getClass().getName()));
         }
      } else if (container instanceof PdfPCell) {
         if (element instanceof PdfPTable) {
            throw new VectorPrintException(String.format("use %s.%s if you want to nest tables", CONTAINER_ELEMENT.class.getName(), CONTAINER_ELEMENT.NESTED_TABLE));
         }
         ((PdfPCell) container).addElement(element);
      } else if (container instanceof ColumnText) {
         ((ColumnText) container).addElement(element);
      } else {
         throw new VectorPrintException(String.format("don't know how to add %s to %s", (element == null) ? "null" : element.getClass().getName(), (container == null) ? "null" : container.getClass().getName()));
      }
   }

   /**
    * Based on a {@link DataConfig} attempts to convert a String into a {@link ReportValue}.
    *
    * @param type the definition of the {@link DataConfig}
    * @param dataObject the object holding the data to be used
    * @return the possibly converted value argument
    * @throws VectorPrintException
    */
   @Override
   public Object determineData(Object dataObject, DataConfig type) throws VectorPrintException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      Class dataClass = dataObject.getClass();
      Method m = dataClass.getMethod(type.getValueasstringmethod(), null);
      String value = String.valueOf(m.invoke(dataObject, null));
      Object d = value;
      if (type != null) {
         Class dataTypeClass = type.getDatatype().getDataclass();
         if (ReportValue.class.isAssignableFrom(dataClass)) {
            try {
               d = ((ReportValue) dataClass.newInstance()).setValue(value).setAlternateFormat(type.getDatatype().getFormat());
            } catch (InstantiationException ex) {
               throw new VectorPrintException(ex);
            }
         }
      }
      return d;
   }

   /**
    * removes containers from the stack upto and including the n-th container of the given dataType. When the container
    * to end is a {@link SimpleColumns} or it is the last on the stack it is added to the document.
    *
    * @param container
    * @param depth
    * @param stack
    * @throws DocumentException
    */
   @Override
   public void endContainer(CONTAINER_ELEMENT container, int depth, Deque stack) throws DocumentException {
      Class toEnd = container.getiTextClass();
      int curDepth = 0;
      Object toRemove;
      while ((toRemove = stack.peek()) != null) {
         stack.remove();
         if (toEnd.isAssignableFrom(toRemove.getClass())
             || (container == CONTAINER_ELEMENT.NESTED_TABLE && toRemove instanceof ToBeAdded)) {
            curDepth++;
         }
         if (toRemove instanceof SimpleColumns) {
            if (!stack.isEmpty()) {
               getDocument().add((Element) stack.getLast());
               stack.clear();
            }

            ((SimpleColumns) toRemove).write();
         } else {
            if (toRemove instanceof ToBeAdded) {
               ToBeAdded ta = (ToBeAdded) toRemove;
               try {
                  addToContainer(ta.parentTable, ta.cellWithTable);
               } catch (VectorPrintException ex) {
                  throw new VectorPrintRuntimeException(ex);
               }
            } else {
               if (stack.isEmpty()) {
                  getDocument().add((Element) toRemove);
               }
            }

         }
         if (curDepth == depth) {
            break;
         }
      }
   }
}
