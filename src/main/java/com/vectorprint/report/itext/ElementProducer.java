package com.vectorprint.report.itext;

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
import com.itextpdf.text.Section;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * implementers should provide at least these methods for adding parts to a report.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ElementProducer extends ImageLoader, LayerManager {
   

   /**
    * Create an Element of a certain class, style it and add data to the element.
    *
    * @param <E>
    * @param data
    * @param stylers
    * @param elementClass
    * @return
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   <E extends Element> E createElement(Object data, Class<E> elementClass, List<? extends BaseStyler> stylers)
       throws VectorPrintException, InstantiationException, IllegalAccessException;
   
   /**
    * Create a Section with a title and a nesting level.
    *
    * @param title
    * @param nesting
    * @param stylers
    * @return the Chapter (level 1) or Section created
    */
   Section getIndex(String title, int nesting, List<? extends BaseStyler> stylers)
       throws VectorPrintException, InstantiationException, IllegalAccessException;
   
   /**
    *
    * @param stylers the value of stylers
    */
   SimpleColumns createColumns(List<? extends BaseStyler> stylers) throws VectorPrintException;

    /**
    * Return a formatted representation of the data, suggested way to do this is use {@link ReportValue} and a
    * {@link Formatter}
    * @param data
    * @return
    */
   String formatValue(Object data);
   
   Formatter getFormatter();

   StyleHelper getStyleHelper();
}
