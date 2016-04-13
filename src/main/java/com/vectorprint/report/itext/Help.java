/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext;

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
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.annotation.ParamAnnotationProcessor;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.DataCollectorImpl;
import com.vectorprint.report.itext.annotations.ContainerStart;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.conditions.AbstractCondition;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.report.running.ReportRunner;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Help {

   private static final Logger log = Logger.getLogger(Help.class.getName());

   /**
    * calls {@link #printHelp(java.io.PrintStream)}.
    *
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      printHelp(System.out);
   }

   public static void printHelpHeader(PrintStream out) {
      out.println("Getting started.");
      out.println("----------------\n");
      out.println("  0 optionally create a settings configuration file (xsd available), examples in junit test");
      out.println("  1 create a settings file with stylinginformation (using current syntax, configure in step 0), examples in junit test.");
      out.println("  2 create an xml config file for the translation of java objects to report parts (xsd in .jar and in GUI) and/or");
      out.println("  3 annotate your dataclasses (" + ContainerStart.class.getPackage().getName() + "), examples in junit tests.");
      out.println("  4 extend " + DataCollectorImpl.class.getName() + ", example in junit test or");
      out.println("  5 extend " + BaseReportGenerator.class.getName() + " and override createReportBody, example in junit test.");
      out.println("  6 setup your classpath to include your classes, reporting jars and dependent jars (see lib folder in binary distribution)");

      out.println("  7 java -cp <cp> " + ReportRunner.class.getName() + "<settings configuration file> or <settings file> <settings in corrrect syntax>\n");
      out.println("  Settings must contain \"" + ReportConstants.DATACLASS + "\" or \"" + ReportConstants.REPORTCLASS + "\", look at " + ReportConstants.class.getName() + " for more available settings\n");
      out.println("  You can look at the junit tests to see working examples\n");
      out.println("  Javadoc is recommended as a source for further details\n");
      out.println("Available stylers that can be configured in a settings file, together with parameters that may be used.");
      out.println("---------------------------------------------------------------------------------------------------------\n");
      out.println("  All parameters mentioned below can take defaults through properties in the current syntax\n"
          + "by default <(Parent)Class.getSimpleName().parameterName.(set_default|set_value)>=<value>\n");
   }

   public static void printStylerHelp(Document document, com.itextpdf.text.Font f) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, InvocationTargetException {
      for (BaseStyler s : getParameterizables(AbstractStyler.class.getPackage(), BaseStyler.class)) {
         document.add(Chunk.NEWLINE);
         document.add(new Chunk(s.getClass().getSimpleName(), f).setLocalDestination(s.getClass().getSimpleName()));
         document.add(new Phrase(":  " + s.getHelp() + "\n  parameters available:", f));

         ByteArrayOutputStream o = new ByteArrayOutputStream(400);
         PrintStream out = new PrintStream(o);
         printParamInfo(s, out);
         out.println("  able to style: " + s.getSupportedClasses());
         addStylerForClass(s);
         if (s.creates()) {
            out.println("  creates supported Class");
         }
         out.flush();
         document.add(new Paragraph(o.toString(), f));
      }
      printStylersPerClass(document, f);
   }

   public static void printConditionrHelp(Document document, com.itextpdf.text.Font f) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, InvocationTargetException {
      document.add(new Paragraph("\nAvailable conditions for stylers that can be configured in a properties file, together with parameters that may be used."
          + "---------------------------------------------------------------------------------------------------------\n"
          + "  All parameters mentioned below can take defaults through properties in the form <(Parent)Class.getSimpleName().parameterName>=<value>\n", f));
      for (StylingCondition s : getParameterizables(AbstractCondition.class.getPackage(), StylingCondition.class)) {
         document.add(Chunk.NEWLINE);
         document.add(new Chunk(s.getClass().getSimpleName(), f).setLocalDestination(s.getClass().getSimpleName()));
         document.add(new Phrase(":  " + s.getHelp() + "\n  parameters available:", f));

         ByteArrayOutputStream o = new ByteArrayOutputStream(400);
         PrintStream out = new PrintStream(o);
         printParamInfo(s, out);
         out.flush();
         document.add(new Paragraph(o.toString(), f));
      }
   }
   private static Map<Class, Set<Class<? extends BaseStyler>>> stylersForClass = new HashMap<>(30);

   private static void addStylerForClass(BaseStyler bs) {
      for (Class cc : bs.getSupportedClasses()) {
         if (!stylersForClass.containsKey(cc)) {
            stylersForClass.put((Class) cc, new HashSet<>(30));
         }
         stylersForClass.get(cc).add(bs.getClass());
         if (Element.class.equals(cc)) {
            break;
         }
         for (Class su : stylersForClass.keySet()) {
            if (Element.class.equals(su)) {
               break;
            }
            if (su.isAssignableFrom((Class) cc) && !su.equals(cc)) {
               stylersForClass.get(cc).addAll(stylersForClass.get(su));
            }
         }
      }
   }

   /**
    * Calls {@link #getParameterizables(java.lang.Package, java.lang.Class) } with packages of {@link AbstractCondition}
    * and {@link AbstractStyler}.
    *
    * @return
    * @throws IOException
    * @throws FileNotFoundException
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws NoSuchMethodException
    * @throws InvocationTargetException
    */
   public static Set<Parameterizable> getStylersAndConditions() throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Set<Parameterizable> parameterizables = getParameterizables(AbstractCondition.class.getPackage(), Parameterizable.class);
      parameterizables.addAll(getParameterizables(AbstractStyler.class.getPackage(), Parameterizable.class));
      return parameterizables;
   }

   /**
    * Use this generic method in for example a gui that supports building a styling file.
    *
    * @param <P>
    * @param javaPackage
    * @param clazz
    * @return
    * @throws IOException
    * @throws FileNotFoundException
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws NoSuchMethodException
    * @throws InvocationTargetException
    */
   public static <P extends Parameterizable> Set<P> getParameterizables(Package javaPackage, Class<P> clazz) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Set<P> parameterizables = new HashSet<>(50);
      for (Class<?> c : ClassHelper.fromPackage(javaPackage)) {
         if (clazz.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            P p = (P) c.newInstance();
            ParamAnnotationProcessor.PAP.initParameters(p);
            parameterizables.add(p);
         }
      }
      return parameterizables;
   }

   public static void printStylerHelp(PrintStream out) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      for (BaseStyler s : getParameterizables(AbstractStyler.class.getPackage(), BaseStyler.class)) {
         out.println("\n" + s.getClass().getSimpleName() + ":  " + s.getHelp() + "\n  parameters available:");
         printParamInfo(s, out);
         out.println("  able to style: " + s.getSupportedClasses());
         addStylerForClass(s);
         if (s.creates()) {
            out.println("  creates supported Class");
         }
      }
      printStylersPerClass(out);
   }

   public static void printConditionHelp(PrintStream out) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      out.println("\nAvailable conditions for stylers that can be configured in a properties file, together with parameters that may be used.");
      out.println("---------------------------------------------------------------------------------------------------------\n");
      out.println("  All parameters mentioned below can take defaults through properties in the form <(Parent)Class.getSimpleName().parameterName>=<value>\n");
      for (StylingCondition s : getParameterizables(AbstractCondition.class.getPackage(), StylingCondition.class)) {
         out.println("\n" + s.getClass().getSimpleName() + ":  " + s.getHelp() + "\n  parameters available:");
         printParamInfo(s, out);
      }
   }

   public static void printStylersPerClass(PrintStream out) {
      out.println("\nOverview of stylers available per iText Class (and its subclasses)\n");
      for (Map.Entry<Class, Set<Class<? extends BaseStyler>>> e : stylersForClass.entrySet()) {
         out.print("\nstylers for " + e.getKey().getSimpleName() + ": ");
         for (Class bs : e.getValue()) {
            out.print(bs.getSimpleName() + ", ");
         }
      }
      out.println();
   }

   public static void printStylersPerClass(Document document, com.itextpdf.text.Font f) throws DocumentException {
      document.add(Chunk.NEWLINE);
      document.add(new Phrase("Overview of stylers available per iText Class (and its subclasses)", f));
      document.add(Chunk.NEWLINE);
      for (Map.Entry<Class, Set<Class<? extends BaseStyler>>> e : stylersForClass.entrySet()) {
         document.add(Chunk.NEWLINE);
         document.add(new Chunk("stylers for " + e.getKey().getSimpleName() + ": ", f));
         for (Class bs : e.getValue()) {
            document.add(new Chunk(bs.getSimpleName(), f).setLocalGoto(bs.getSimpleName()).setUnderline(0.3f, -1f));
            document.add(new Chunk(", ", f));
         }
      }
   }

   public static void printHelp(PrintStream out) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      printHelpHeader(out);
      printStylerHelp(out);
      printConditionHelp(out);
   }

   public static void printParamInfo(Parameterizable p, PrintStream out) {
      for (Parameter e : p.getParameters().values()) {
         out.println("    " + e);
         if (p instanceof BaseStyler) {
            out.println("    css equivalent(s): " + Arrays.toString(((BaseStyler) p).getCssEquivalent(e)));
         }
      }
   }

   public static String getClassInfo(String key, Class c) {
      if (c != null) {
         return (c.isArray()) ? "array of " + c.getComponentType().getName() : c.getName();
      } else {
         log.warning("class information for key: " + key + " is missing");
         return "unknown";
      }
   }

}
