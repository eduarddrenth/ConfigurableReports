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
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.MultipleValueParser;
import com.vectorprint.configuration.parameters.ValueParser;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.report.itext.ItextHelper;

/**
 * A Parameter converting from millimeters in configuration to points in iText
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FloatArrayParameter extends com.vectorprint.configuration.parameters.FloatArrayParameter {

   public FloatArrayParameter(String key, String help) {
      super(key, help);
   }

   private static final ToPtsFloatParser TO_PTS_FLOAT_PARSER = new ToPtsFloatParser();

   public static class ToPtsFloatParser implements ValueParser<Float> {

      @Override
      public Float parseString(String val) {
         return ItextHelper.mmToPts(Float.parseFloat(val));
      }
   }

   /**
    *
    * @throws VectorPrintRuntimeException
    */
   @Override
   public Float[] convert(String value) throws VectorPrintRuntimeException {
      try {
         return ArrayHelper.toArray(MultipleValueParser.getParamInstance().parseValues(value, TO_PTS_FLOAT_PARSER));
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   protected String valueToString(Object value) {
      return super.valueToString(ItextHelper.ptsToMm((Float) value));
   }

}
