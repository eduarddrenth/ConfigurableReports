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

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.ParameterImpl;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class BasefontParameter extends ParameterImpl<BaseFontWrapper>{

   public BasefontParameter(String key, String help) {
      super(key, help);
   }

   @Override
   public BaseFontWrapper convert(String value) throws VectorPrintRuntimeException {
      Font f = FontFactory.getFont(value);
      if (f.getBaseFont()==null) {
         throw new VectorPrintRuntimeException("No basefont for: " +value);
      }
      return new BaseFontWrapper(f.getBaseFont());
   }

   @Override
   protected String valueToString(Object value) {
      return (value!=null)?((BaseFontWrapper)value).getFontName():"";
   }

}
