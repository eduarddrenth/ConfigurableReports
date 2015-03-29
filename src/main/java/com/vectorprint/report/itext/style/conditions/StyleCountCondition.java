/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.conditions;

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

import com.vectorprint.configuration.parameters.IntParameter;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StyleCountCondition extends AbstractCondition {

   public static final String NUMBER = "number";
   private int actualNumber = -1;

   public StyleCountCondition() {
      addParameter(new IntParameter(NUMBER, "number of executions"),StyleCountCondition.class);
   }

   /**
    *
    * @param data the value of data
    * @param element the value of element
    * @return the boolean
    */
   @Override
   public boolean shouldStyle(Object data, Object element) {
      return ++actualNumber < getValue(NUMBER, Integer.class);
   }
   @Override
   public String getHelp() {
      return "Only style a certain amount of times. " + super.getHelp(); 
   }
}
