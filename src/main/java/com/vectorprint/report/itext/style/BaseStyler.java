package com.vectorprint.report.itext.style;

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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.DocumentAware;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import java.util.Collection;
import java.util.List;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------
/**
 * Stylers are responsible for styling iText building blocks (Elements, fields) for reports. Stylers can be found using
 * a {@link StylerFactory}, after finding them stylers can be used as argument calling a {@link ElementProducer#createElement(java.lang.Object, java.lang.Class, java.util.List) }, the returned element can then be added to the document, see {@link BaseReportGenerator#createAndAddElement(java.lang.Object, java.lang.Class, java.lang.String...)}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @see BaseReportGenerator#createAndAddElement(java.lang.Object, java.lang.Class, java.lang.String...) 
 */
public interface BaseStyler extends Parameterizable, DocumentAware {

   /**
    *
    */
   public static final String COLOR_PARAM = "color";
   public static final String SIZE_PARAM = "size";
   public static final String TOPRIGTHBOTTOMLEFT_PARAM = "position";
   public static final String ALIGNPARAM = "align";

   /**
    * enum constants for indicating top, right, left and bottom combinations for example for borders on rectangles
    */
   public enum POSITION {

      TOP(Rectangle.TOP),
      RIGHT(Rectangle.RIGHT),
      BOTTOM(Rectangle.BOTTOM),
      LEFT(Rectangle.LEFT),
      TRBL(Rectangle.BOX),
      LR(Rectangle.LEFT + Rectangle.RIGHT),
      LRB(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM),
      LRT(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.TOP),
      LB(Rectangle.LEFT + Rectangle.BOTTOM),
      LBT(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.TOP),
      LT(Rectangle.LEFT + Rectangle.TOP),
      RB(Rectangle.RIGHT + Rectangle.BOTTOM),
      RBT(Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP),
      RT(Rectangle.RIGHT + Rectangle.TOP),
      BT(Rectangle.BOTTOM + Rectangle.TOP),
      NONE(Rectangle.NO_BORDER);

      private int position;

      private POSITION(int position) {
         this.position = position;
      }

      public int getPosition() {
         return position;
      }

   }

   /**
    * enum constants for horizontal and vertical alignment combinations
    */
   public enum ALIGN {

      LEFT_TOP(PdfPCell.ALIGN_TOP, PdfPCell.ALIGN_LEFT), LEFT_MIDDLE(PdfPCell.ALIGN_MIDDLE, PdfPCell.ALIGN_LEFT), LEFT(PdfPCell.ALIGN_MIDDLE, PdfPCell.ALIGN_LEFT),
      LEFT_BOTTOM(PdfPCell.ALIGN_BOTTOM, PdfPCell.ALIGN_LEFT), RIGHT_TOP(PdfPCell.ALIGN_TOP, PdfPCell.ALIGN_RIGHT),
      RIGHT_MIDDLE(PdfPCell.ALIGN_MIDDLE, PdfPCell.ALIGN_RIGHT), RIGHT(PdfPCell.ALIGN_MIDDLE, PdfPCell.ALIGN_RIGHT),
      RIGHT_BOTTOM(PdfPCell.ALIGN_BOTTOM, PdfPCell.ALIGN_RIGHT), CENTER_TOP(PdfPCell.ALIGN_TOP, PdfPCell.ALIGN_CENTER),
      CENTER_MIDDLE(PdfPCell.ALIGN_MIDDLE, PdfPCell.ALIGN_CENTER),
      CENTER_BOTTOM(PdfPCell.ALIGN_BOTTOM, PdfPCell.ALIGN_CENTER);

      public static final String LEFTPART = "left";
      public static final String RIGHTPART = "right";
      public static final String CENTER = "center";
      public static final String BOTTOM = "bottom";
      public static final String TOP = "top";
      public static final String MIDDLE = "middle";

      private int vertical, horizontal;

      private ALIGN(int vert, int hor) {
         this.horizontal = hor;
         this.vertical = vert;
      }

      public int getHorizontal() {
         return horizontal;
      }

      public int getVertical() {
         return vertical;
      }

      /**
       *
       * @param horizontal the ALIGN of which you want to use the horizontal value
       * @param vertical the vertical value
       * @return
       */
      public static ALIGN forHorizontal(ALIGN horizontal, String vertical) {
         if (horizontal != null) {
            String vert = "unknown";
            if (BOTTOM.equalsIgnoreCase(vertical)||TOP.equalsIgnoreCase(vertical)||MIDDLE.equalsIgnoreCase(vertical)) {
               vert = vertical;
            }
            switch (horizontal) {
               case CENTER_BOTTOM:
               case CENTER_MIDDLE:
               case CENTER_TOP:
                  return ALIGN.valueOf((CENTER + '_' + vert).toUpperCase());
               case LEFT_BOTTOM:
               case LEFT_MIDDLE:
               case LEFT_TOP:
                  return ALIGN.valueOf((LEFTPART + '_' + vert).toUpperCase());
               case RIGHT_BOTTOM:
               case RIGHT_MIDDLE:
               case RIGHT_TOP:
                  return ALIGN.valueOf((RIGHTPART + '_' + vert).toUpperCase());
            }
         }
         return null;
      }
      /**
       *
       * @param vertical the ALIGN of which you want to use the vertical value
       * @param horizontal the horizontal value
       * @return
       */
      public static ALIGN forVertical(ALIGN vertical, String horizontal) {
         if (vertical != null) {
            String hor = "unknown";
            if (LEFTPART.equalsIgnoreCase(horizontal)||RIGHTPART.equalsIgnoreCase(horizontal)||CENTER.equalsIgnoreCase(horizontal)) {
               hor = horizontal;
            }
            switch (vertical) {
               case CENTER_BOTTOM:
               case LEFT_BOTTOM:
               case RIGHT_BOTTOM:
                  return ALIGN.valueOf((hor + '_' + BOTTOM).toUpperCase());
               case CENTER_MIDDLE:
               case LEFT_MIDDLE:
               case RIGHT_MIDDLE:
                  return ALIGN.valueOf((hor + '_' + MIDDLE).toUpperCase());
               case CENTER_TOP:
               case LEFT_TOP:
               case RIGHT_TOP:
                  return ALIGN.valueOf((hor + '_' + TOP).toUpperCase());
            }
         }
         return null;
      }
   }

   /**
    * The actual method that performs the styling.
    *
    * @param element
    * @param data
    * @return
    * @throws VectorPrintException
    */
   <E> E style(E element, Object data) throws VectorPrintException;

   /**
    * Can the Styler style a certain OBJECTTOSTYLE, used by {@link ElementProducer}.
    *
    * @param element
    * @return
    */
   boolean canStyle(Object element);

   /**
    * Does the styler create an Element, used by {@link ElementProducer}.
    *
    * @return
    */
   boolean creates();

   /**
    * A list of classes all supported by this styler.
    *
    * @return
    */
   Set<Class> getSupportedClasses();

   /**
    * should the styler style the element, based on the data for the element and possibly other state in the styler.
    *
    * @param data
    * @param element the value of element
    * @return true when styling should be performed
    * @see #addCondition(com.vectorprint.report.itext.style.StylingCondition)
    */
   boolean shouldStyle(Object data, Object element);
   
   /**
    * 
    * @return true when styling should be done after adding the element to the document
    */
   boolean styleAfterAdding();

   /**
    * add a condition to this styler to determine wether or not to style
    *
    * @param condition
    */
   void addCondition(StylingCondition condition);

   /**
    * get the conditions for this styler
    *
    * @return
    */
   List<StylingCondition> getConditions();

   /**
    * print some help for a styler
    *
    * @return
    */
   String getHelp();

   /**
    * set the key in the configuration that declared this styler
    *
    * @see DefaultStylerFactory
    * @param className
    */
   BaseStyler setStyleClass(String className);

   /**
    * get the key in the configuration that declared this styler
    *
    * @return
    */
   String getStyleClass();

   /**
    * Get the css equivalent for a parameter.
    *
    * @param parameter
    * @return the css equivalent for a parameter or null
    */
   String[] getCssEquivalent(Parameter parameter);

   /**
    * find one or more parameters that implement a css property, this is configured in a file
    * {@link AbstractStyler#CSS_NAMESPROPERTIES}.
    *
    * @see StylerFactoryHelper#findForCssName(java.lang.String)
    * @param cssProperty
    * @return
    */
   Collection<Parameter> findForCssProperty(String cssProperty);

}
