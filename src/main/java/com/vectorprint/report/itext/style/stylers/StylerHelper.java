/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.stylers;

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

import com.vectorprint.configuration.observing.PrepareKeyValue;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * some helper functions for stylers
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StylerHelper {
   
   private static final Logger log = Logger.getLogger(StylerHelper.class.getName());
   
   private StylerHelper() {}

   /**
    * return true when element is assignable from one of the classes in the argument collection.
    *
    * @param classes
    * @param element
    * @return
    */
   public static boolean supported(Set<Class> classes, Object element) {
      for (Class c : classes) {
         if (c.isAssignableFrom(element.getClass())) {
            if (log.isLoggable(Level.FINE)) {
               log.fine(element.getClass().getName() + " will be styled, it is assignable from " + c.getName());
            }
            return true;
         }
      }
      return false;
   }
   
   public static final java.util.List<PrepareKeyValue<String,String>> toList(PrepareKeyValue<String,String> prepareKeyValue) {
      java.util.List<PrepareKeyValue<String,String>> l = new ArrayList<PrepareKeyValue<String,String>>(1);
      l.add(prepareKeyValue);
      return l;
   }

}
