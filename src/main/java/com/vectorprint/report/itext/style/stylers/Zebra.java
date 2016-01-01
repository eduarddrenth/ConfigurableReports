
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
import com.itextpdf.text.pdf.PdfPTable;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.ClassParameter;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.LayerManagerAware;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.ZebraStripes;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------
/**
 * zebra striping for tables
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Zebra extends AbstractStyler implements LayerManagerAware {

   public static final String ODDCOLOR = "oddcolor";
   public static final String EVENCOLOR = "evencolor";
   public static final String ALTERNATE = "alternate";
   private LayerManager layerManager;
   public static final String TABLEEVENTCLASS = "tableeventclass";

   public Zebra() {

      addParameter(new ColorParameter(ODDCOLOR, "#rgb"),Zebra.class);
      addParameter(new ColorParameter(EVENCOLOR, "#rgb"),Zebra.class);
      addParameter(new IntParameter(ALTERNATE, "alternate color every n (default 1) rows").setDefault(1),Zebra.class);
      addParameter(new ClassParameter(TABLEEVENTCLASS, "The class (subclass of ZebraStripes) that wil handle table events").setDefault(ZebraStripes.class),Zebra.class);
   }

   private PdfPTable style(PdfPTable t) throws VectorPrintException {
      ZebraStripes zs = null;
      try {
         zs = (ZebraStripes) getValue(TABLEEVENTCLASS, Class.class).newInstance();
         zs.setAlternate(getAlternate());
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(zs, getSettings());
      zs.setLayerManager(layerManager);

      if (getOdd() != null) {
         zs.setOddColor(getOdd());
      }
      if (getEven() != null) {
         zs.setEvenColor(getEven());
      }
      t.setTableEvent(zs);
      return t;
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      return (E) style((PdfPTable) text);
   }
   private static final Class<Object>[] classes = new Class[]{PdfPTable.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   public Color getOdd() {
      return getValue(ODDCOLOR, Color.class);
   }

   public void setOdd(Color odd) {
      setValue(ODDCOLOR, odd);
   }

   public Color getEven() {
      return getValue(EVENCOLOR, Color.class);
   }

   public void setEven(Color even) {
      setValue(EVENCOLOR, even);
   }

   public int getAlternate() {
      return getValue(ALTERNATE, Integer.class);
   }

   public void setAlternate(int alternate) {
      setValue(ALTERNATE, alternate);
   }

   @Override
   public void setLayerManager(LayerManager layerManager) {
      this.layerManager = layerManager;
   }

   @Override
   public String getHelp() {
      return "Define zebra striping for tables." + " " + super.getHelp();
   }
}
