/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.conditions;

import com.vectorprint.configuration.parameters.RegexParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;
import java.util.regex.Pattern;

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
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters(
    parameters = {
   @Param(
       clazz = RegexParameter.class,
       key = ElementCondition.SIMPLECLASSREGEX,
       help = "regex to determine which elements should be styled")
})
public class ElementCondition extends AbstractCondition {

   public static final String SIMPLECLASSREGEX = "simpleClassRegex";
   /**
    *
    * @param data the value of data
    * @param element the value of element
    * @return the boolean
    */
   @Override
   public boolean shouldStyle(Object data, Object element) {
      if (element!=null) {
         return getValue(SIMPLECLASSREGEX, Pattern.class).
             matcher(element.getClass().getSimpleName()).find();
      }
      return true;
   }
   @Override
   public String getHelp() {
      return "Only style certain iText elements. " + super.getHelp(); 
   }
}
