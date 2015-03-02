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

import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.data.types.TextValue;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this in containers and elements to indicate a certain datatype
 * 
 * @see ContainerStart#dataType() 
 * @see Element#dataType() 
 * @see ReportValue
 * @see ReportGenerator#processData(com.vectorprint.report.data.ReportDataHolder) 
 * @author Eduard Drenth at VectorPrint.nl
 */
@Inherited
@Documented
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DataType {
   
   public Class<? extends ReportValue> dataClass() default TextValue.class;
   public String format() default "";

}
