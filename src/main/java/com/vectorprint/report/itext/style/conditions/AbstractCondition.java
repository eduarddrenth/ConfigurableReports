
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
//~--- non-JDK imports --------------------------------------------------------
import com.vectorprint.configuration.parameters.ParameterizableImpl;
import com.vectorprint.report.itext.style.StylingCondition;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractCondition extends ParameterizableImpl implements StylingCondition {

   private static final Logger log = Logger.getLogger(AbstractCondition.class.getName());
   private String key;   

   /**
    * Calls !{@link #shouldStyle(java.lang.Object, java.lang.Object) } with data and null. Returns true
    * when shouldStyle returns false vise versa.
    * @param data the value of data
    * @return the boolean
    */
   @Override
   public boolean shouldNotDraw(Object data) {
      return !shouldStyle(data, null);
   }

   @Override
   public StylingCondition setConfigKey(String key) {
      this.key = key;
      return this;
   }

   @Override
   public String getConfigKey() {
      return key;
   }

   @Override
   public String getHelp() {
      return "condition to determine when to style or not";
   }

}
