/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

import com.itextpdf.text.Chapter;
import com.itextpdf.text.pdf.ColumnText;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.style.StylerFactory;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates a container is to be ended. When the requested container to end is a {@link Chapter} or a
 * {@link ColumnText} it will be added to the report after it has been removed from the stack of containers
 *
 * @see ContainerStart
 * @see ElementProducer#createElement(java.lang.Object, java.lang.Class, java.util.List) 
 * @see StylerFactory#getStylers(java.lang.String[])
 * @author Eduard Drenth at VectorPrint.nl
 */
@Inherited
@Documented
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ContainerEnd {

   public CONTAINER_ELEMENT containerType();

   /**
    * when ending a container of a certain type, this indicates the depth in the nesting of containers
    *
    * @return
    */
   public int depthToEnd() default 1;
}
