
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

//~--- non-JDK imports --------------------------------------------------------

import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.BOTTOM;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.BT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LB;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LBT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LEFT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LR;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LRB;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LRT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.LT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.RB;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.RBT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.RIGHT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.RT;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.TOP;
import static com.vectorprint.report.itext.style.BaseStyler.POSITION.TRBL;

import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.PositionParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * padding for cells
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Padding extends AbstractStyler  {

   public static final String PADDING = "padding";

   public Padding() {

      addParameter(new FloatParameter(PADDING, "padding").setDefault(1f),Padding.class);
      addParameter(new PositionParameter(TOPRIGTHBOTTOMLEFT_PARAM, Arrays.asList(POSITION.values()).toString()).setDefault(TRBL),Padding.class);
   }

   public PdfPCell style(PdfPCell cell, Object data) {
      switch (getWhichPadding()) {
         case LEFT:
         case LB:
         case LBT:
         case LR:
         case LRB:
         case LRT:
         case LT:
         case TRBL:
            cell.setPaddingLeft(getPadding());
      }
      switch (getWhichPadding()) {
         case LBT:
         case LRT:
         case LT:
         case TRBL:
         case RBT:
         case RT:
         case TOP:
         case BT:
            cell.setPaddingTop(getPadding());
      }
      switch (getWhichPadding()) {
         case LB:
         case LBT:
         case LRB:
         case TRBL:
         case RB:
         case RBT:
         case BOTTOM:
         case BT:
            cell.setPaddingBottom(getPadding());
      }
      switch (getWhichPadding()) {
         case LR:
         case LRB:
         case LRT:
         case TRBL:
         case RIGHT:
         case RB:
         case RBT:
         case RT:
            cell.setPaddingRight(getPadding());
      }

      return cell;
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text instanceof PdfPCell) {
         return (E) style((PdfPCell) text, data);
      }

      return text;
   }

   private static final Class<Object>[] classes = new Class[]{PdfPCell.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public float getPadding() {
      return getValue(PADDING,Float.class);
   }

   public void setPadding(float padding) {
      setValue(PADDING, padding);
   }

   public POSITION getWhichPadding() {
      return getValue(TOPRIGTHBOTTOMLEFT_PARAM,POSITION.class);
   }

   public void setWhichPadding(POSITION whichPadding) {
      setValue(TOPRIGTHBOTTOMLEFT_PARAM, whichPadding);
   }

   @Override
   public String getHelp() {
      return "Specify padding for cells. " + super.getHelp(); 
   }
}
