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

import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.DoubleArrayParameter;
import com.vectorprint.configuration.parameters.IntArrayParameter;
import java.util.Arrays;

/**
 * Condition that compares the numeric data to number parameter(s) of this condition.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class NumberCondition extends AbstractCondition {

   public static final String NUMBERS = "numbers";
   public static final String GREATER = "greater";
   public static final String LESSER = "lesser";
   public static final String EVEN = "even";
   public static final String ODD = "odd";
   public static final String BETWEEN = "between";

   public NumberCondition() {
      addParameter(new DoubleArrayParameter(NUMBERS,"the numbers to compare to"),NumberCondition.class);
      addParameter(new BooleanParameter(GREATER,"number should be greater than the first one provided"),NumberCondition.class);
      addParameter(new BooleanParameter(LESSER,"number should be lesser than the first one provided"),NumberCondition.class);
      addParameter(new BooleanParameter(EVEN,"number should be even"),NumberCondition.class);
      addParameter(new BooleanParameter(ODD, "number should be odd"),NumberCondition.class);
      addParameter(new BooleanParameter(BETWEEN, "number should be between the first and the second one provided"),NumberCondition.class);
   }

   /**
    *
    * @param data the value of data
    * @param element the value of element
    * @return the boolean
    */
   @Override
   public boolean shouldStyle(Object data, Object element) {
      double nr = Double.parseDouble(data.toString());
      if (getValue(ODD, Boolean.class)) {
         return nr % 2 != 0;
      }
      if (getValue(EVEN, Boolean.class)) {
         return nr % 2 == 0;
      }
      if (getValue(BETWEEN, Boolean.class)) {
         return getValue(NUMBERS, Double[].class)[0] < nr && nr < getValue(NUMBERS, Double[].class)[1];
      }
      return (getValue(GREATER, Boolean.class) || getValue(LESSER, Boolean.class))
          ? (getValue(GREATER, Boolean.class))
            ? getValue(NUMBERS, Double[].class)[0] > nr
            : getValue(NUMBERS, Double[].class)[0] < nr
          : Arrays.binarySearch(getValue(NUMBERS, Double[].class), nr) >= 0;
   }

   @Override
   public String getHelp() {
      return "Only style when a numeric condition is met. " + super.getHelp(); 
   }
}
