/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.parameters;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.binding.parameters.AbstractParamBindingHelperDecorator;
import com.vectorprint.configuration.binding.parameters.EscapingBindingHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.itext.ItextHelper;
import java.io.Serializable;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportBindingHelper extends AbstractParamBindingHelperDecorator {

   public ReportBindingHelper() {
      super(new EscapingBindingHelper());
   }

   @Override
   public <TYPE extends Serializable> void setValueOrDefault(Parameter<TYPE> parameter, TYPE value, boolean setDefault) {
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
         Float mm = ItextHelper.ptsToMm(f);
         return (TYPE) mm;
      } else if (p instanceof FloatArrayParameter) {
         float[] pts = (float[]) super.getValueToSerialize(p, useDefault);
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
