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
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.DocumentAware;
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;
import com.vectorprint.report.itext.mappingconfig.model.DataConfig;
import com.vectorprint.report.itext.mappingconfig.model.ElementConfig;
import com.vectorprint.report.itext.mappingconfig.model.StartContainerConfig;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import java.lang.reflect.InvocationTargetException;
import java.util.Deque;

/**
 * suggested methods for an annotation processor, used from {@link ReportGenerator#processData(com.vectorprint.report.data.ReportDataHolder) }
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface DatamappingProcessor extends ElementProducer, StylerFactory, DocumentAware {

   /**
    * add a container to the stack of containers and, when applicable, add data to it
    * @param cs
    * @param containers
    * @param data
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws DocumentException
    * @throws NoSuchMethodException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException 
    */
   void addContainer(StartContainerConfig cs, Deque containers, Object data) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;

   /**
    * add an element to the document or to a container if one is on the stack
    * @param elementAnnotation
    * @param containers
    * @param data
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws DocumentException
    * @throws NoSuchMethodException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException 
    */
   void addElement(ElementConfig elementAnnotation, Deque containers, Object data) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;

   /**
    * attempt to add an element to a container
    *
    * @param container
    * @param element
    * @throws DocumentException
    * @throws com.vectorprint.VectorPrintException
    */
   void addToContainer(Element container, Element element) throws DocumentException, VectorPrintException;

   /**
    * Based on a {@link DataConfig} attempts to convert a String into a {@link ReportValue}.
    *
    * @param dataObject
    * @param dataConfig  
    * @return the possibly converted value argument
    * @throws VectorPrintException
    * @throws java.lang.NoSuchMethodException
    * @throws java.lang.IllegalAccessException
    * @throws java.lang.reflect.InvocationTargetException
    */
   Object determineData(Object dataObject, DataConfig dataConfig) throws VectorPrintException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

   /**
    * removes containers from the stack upto and including the n-th container of the given type. When the container to end is
    * a {@link SimpleColumns} or it is the last on the stack it is added to the document.
    *
    * @param container
    * @param depth
    * @param stack
    * @throws DocumentException
    */
   void endContainer(CONTAINER_ELEMENT container, int depth, Deque stack) throws DocumentException;

   /**
    * determine the type of the container to add to the document based directly on {@link StartContainerConfig#getContainertype() }
    * or on the result of a call to the method indicated by {@link StartContainerConfig#getContainertypemethod()  }
    * @param cs
    * @param data
    * @return
    * @throws NoSuchMethodException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws VectorPrintException 
    */
   CONTAINER_ELEMENT getType(StartContainerConfig cs, Object data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, VectorPrintException;

   /**
    * determine the type of the element to add to the document based directly on {@link ElementConfig#getElementtype()  }
    * or on the result of a call to the method indicated by {@link ElementConfig#getElementtypemethod()   }
    * @param e
    * @param data
    * @return
    * @throws NoSuchMethodException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws VectorPrintException 
    */
   Class<? extends Element> getType(ElementConfig e, Object data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, VectorPrintException;

}
