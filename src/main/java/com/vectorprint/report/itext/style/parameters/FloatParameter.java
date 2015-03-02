/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.parameters;

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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.report.itext.ItextHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A Parameter able to convert from millimeters in configuration to points in iText
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FloatParameter extends com.vectorprint.configuration.parameters.FloatParameter{

   private boolean mmToPts = true;
   
   public FloatParameter(String key, String help) {
      this(key, help, true);
   }
   
   public FloatParameter(String key, String help, boolean mmToPts) {
      super(key, help);
      this.mmToPts = mmToPts;
   }

   @Override
   public Float convert(String value) throws VectorPrintRuntimeException {
         return (mmToPts)?ItextHelper.mmToPts(Float.parseFloat(value)):Float.parseFloat(value);
   }
   
   @Override
   protected String valueToString(Object value) {
      return (mmToPts)?super.valueToString(ItextHelper.ptsToMm((Float)value)):super.valueToString(value);
   }

   @Override
   public Parameter<Float> clone() {
      try {
         Constructor con = getClass().getConstructor(String.class,String.class,boolean.class);
         ParameterImpl o = (ParameterImpl) con.newInstance(getKey(),getHelp(),mmToPts);
         return o.setDefault(getDefault()).setValue(setDefault(null).getValue());
      } catch (NoSuchMethodException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (SecurityException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InvocationTargetException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }


}
