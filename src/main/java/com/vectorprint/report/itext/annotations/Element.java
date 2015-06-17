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

import com.itextpdf.text.Phrase;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.style.StylerFactory;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * You can annotate classes in your data to be added to a report as implementations of {@link com.itextpdf.text.Element}
 * and styled according to styleClasses. When a container is active the element will be added to the active container if
 * possible.
 *
 * @see DataType
 * @see GetData
 * @see ElementProducer#createElement(java.lang.Object, java.lang.Class, java.util.List) 
 * @see StylerFactory#getStylers(java.lang.String[])
 * @author Eduard Drenth at VectorPrint.nl
 */
@Documented
@Inherited
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Element {

   public Class<? extends com.itextpdf.text.Element> iTextClass() default Phrase.class;

   /**
    * an optional method in your data class returning an implementation of {@link com.itextpdf.text.Element}.
    * @return 
    */
   public String iTextClassMethod() default "";

   public String[] styleClasses() default "";
   
   /**
    * the optional name of the function that will yield styleClasses as a String[]
    * @return 
    */
   public String styleClassesMethod() default "";

   /**
    * only used when defining multiple {@link Elements}
    * @return 
    */
   public DataType dataType() default @DataType;
   
   /**
    * the function to call to get the data from an instance of the annotated class
    * @return 
    */
   public GetData dataFunction() default @GetData;
   
}
