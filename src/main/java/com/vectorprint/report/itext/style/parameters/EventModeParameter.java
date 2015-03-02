package com.vectorprint.report.itext.style.parameters;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2014 VectorPrint
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
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.report.itext.style.stylers.Advanced;

/**
 * 
 * @author Eduard Drenth at VectorPrint.nl
 */
public class EventModeParameter extends ParameterImpl<Advanced.EVENTMODE>{

   public EventModeParameter(String key, String help) {
      super(key, help);
   }

   @Override
   public Advanced.EVENTMODE convert(String value) throws VectorPrintRuntimeException {
      return Advanced.EVENTMODE.valueOf(value.toUpperCase());
   }

}
