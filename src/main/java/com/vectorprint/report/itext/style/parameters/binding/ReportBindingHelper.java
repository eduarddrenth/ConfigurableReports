/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.parameters.binding;

/*
 * #%L
 * ConfigurableReports
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 VectorPrint
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
import com.vectorprint.configuration.binding.parameters.AbstractParamBindingHelperDecorator;
import com.vectorprint.configuration.binding.parameters.EscapingBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.parameters.BaseFontWrapper;
import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.io.Serializable;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportBindingHelper extends AbstractParamBindingHelperDecorator {

   public ReportBindingHelper() {
      super(new EscapingBindingHelper());
   }
   
   protected ReportBindingHelper(ParamBindingHelper bh) {
      super(bh);
   }

   @Override
   public <TYPE extends Serializable> void setValueOrDefault(Parameter<TYPE> parameter, TYPE value, boolean setDefault) {
      if (value==null) {
         super.setValueOrDefault(parameter, value, setDefault);
         return;
      }
      if (parameter instanceof FloatParameter) {
         Float mmToPts = ItextHelper.mmToPts((Float) value);
         super.setValueOrDefault(parameter, (TYPE) mmToPts, setDefault);
      } else if (parameter instanceof FloatArrayParameter) {
         float[] f = (float[]) value;
         float[] mmToPts = new float[f.length];
         int i = 0;
         for (float ff : f) {
            mmToPts[i++] = ItextHelper.mmToPts(ff);
         }
         super.setValueOrDefault(parameter, (TYPE) mmToPts, setDefault);
      } else {
         super.setValueOrDefault(parameter, value, setDefault);
      }

   }

   @Override
   public <TYPE extends Serializable> TYPE getValueToSerialize(Parameter<TYPE> p, boolean useDefault) {
      if (p instanceof FloatParameter) {
         Float f = (Float) super.getValueToSerialize(p, useDefault);
         if (f==null) {
            return null;
         }
         Float mm = ItextHelper.ptsToMm(f);
         return (TYPE) mm;
      } else if (p instanceof FloatArrayParameter) {
         float[] pts = (float[]) super.getValueToSerialize(p, useDefault);
         if (pts==null) {
            return null;
         }
         float[] mm = new float[pts.length];
         int i = 0;
         for (float ff : pts) {
            mm[i++] = ItextHelper.ptsToMm(ff);
         }
         return (TYPE) mm;
      } else {
         return super.getValueToSerialize(p, useDefault);
      }
   }

   @Override
   public <T> T convert(String value, Class<T> clazz) {
      if (BaseFontWrapper.class.equals(clazz)) {
         Font f = FontFactory.getFont(value);
         if (f.getBaseFont() == null) {
            throw new VectorPrintRuntimeException("No basefont for: " + value);
         }
         return (T) new BaseFontWrapper(f.getBaseFont());
      } else {
         return super.convert(value, clazz);
      }
   }

}
