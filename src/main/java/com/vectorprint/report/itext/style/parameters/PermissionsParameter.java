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
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.configuration.parser.MultiValueParamParser;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import java.io.StringReader;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PermissionsParameter extends IntParameter {

   public PermissionsParameter(String key, String help) {
      super(key, help);
   }

   /**
    * The value can be one or more {@link DocumentSettings.PERMISSION} values, or a number. For the number option see
    * {@link PdfWriter#ALLOW_ASSEMBLY} etc.
    *
    * @see MultiValueParamParser
    * @param value
    * @return
    * @throws VectorPrintRuntimeException
    */
   @Override
   public Integer convert(String value) throws VectorPrintRuntimeException {
      try {
         int p = 0;
         for (String s : new MultiValueParamParser(new StringReader(value)).parse()) {
            p |= DocumentSettings.PERMISSION.valueOf(s.toUpperCase()).getPermission();
         }
         return p;
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalArgumentException ex) {
         return Integer.parseInt(value);
      }
   }

}
