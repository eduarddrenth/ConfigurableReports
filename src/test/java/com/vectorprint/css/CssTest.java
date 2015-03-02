package com.vectorprint.css;

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

import com.steadystate.css.parser.SACParser;
import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.report.itext.style.css.CssTransformer;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.stylers.Background;
import com.vectorprint.report.itext.style.stylers.Border;
import com.vectorprint.report.itext.style.stylers.Font;
import com.vectorprint.report.running.ConfigurableReportBuilderTest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class CssTest extends TestCase {

   @Test
   public void testCss() throws Exception {
      System.setProperty("org.w3c.css.sac.parser", SACParser.class.getName());

      try {
         CssTransformer.main(new String[]{"src/test/resources/test.css", ConfigurableReportBuilderTest.TARGET + "stylesheetFromCss.properties"});
         fail("exception expected, css property cursor not supported");
      } catch (IllegalArgumentException illegalArgumentException) {
      }
      try {
         CssTransformer.main(new String[]{"src/test/resources/test.css", ConfigurableReportBuilderTest.TARGET + "stylesheetFromCss.properties", CssTransformer.NOVALIDATE});
      } catch (IllegalArgumentException illegalArgumentException) {
         fail("exception not expected, css property cursor not supported, but validate is false");
      }
   }

   @Test
   public void testCssParams() throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      for (Class<?> c : ClassHelper.fromPackage(Font.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers()) && BaseStyler.class.isAssignableFrom(c)) {
            BaseStyler bs = (BaseStyler) c.newInstance();
            for (Parameter p : bs.getParameters().values()) {
               // nothing to assert here, but good to run in test 
               bs.getCssEquivalent(p);
            }
         }
      }
      assertEquals(Background.class, StylerFactoryHelper.findForCssName("background-color").iterator().next().getClass());
      Collection<BaseStyler> bs = StylerFactoryHelper.findForCssName("border");
      assertEquals(Border.class, bs.iterator().next().getClass());
      assertEquals(3, bs.iterator().next().findForCssProperty("border").size());
   }
}
