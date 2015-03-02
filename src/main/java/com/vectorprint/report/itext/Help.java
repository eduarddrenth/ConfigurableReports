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
import com.vectorprint.report.data.DataCollectorImpl;
import com.vectorprint.report.itext.annotations.ContainerStart;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.FormFieldStyler;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.conditions.AbstractCondition;
import com.vectorprint.report.itext.style.conditions.RegexCondition;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.report.itext.style.stylers.Font;
import com.vectorprint.report.running.ReportRunner;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
    * calls {@link #printHelp(java.io.PrintStream) or prints available css parameters (see {@link #CSS_PARAM_ARGS})
    * @param args
    * @throws Exception 
    */
   public static void main(String[] args) throws Exception {
      if (args != null && args.length > 0 && args[0].equals(CSS_PARAM_ARGS)) {
         for (Class<?> c : ClassHelper.fromPackage(Font.class.getPackage())) {
            if (!Modifier.isAbstract(c.getModifiers())&&BaseStyler.class.isAssignableFrom(c)) {
               BaseStyler bs = (BaseStyler) c.newInstance();
               for (Parameter p : bs.getParameters().values()) {
                  System.out.println("# " + p.getHelp());
                  System.out.println(c.getSimpleName() + '.' + p.getKey() + '=');
               }
            }
         }
      } else {
         printHelp(System.out);
      }
   }
   /**
    * when the first argument to {@link #main(java.lang.String[]) } equals this constant css parameters for stylers are printed
    */
   public static final String CSS_PARAM_ARGS = "cssParams";

   public static void printHelpHeader(PrintStream out) {
      out.println("Getting started.");
      out.println("----------------\n");
      out.println("  1 create a properties file with stylinginformation, example in junit test");
      out.println("  2 create an xml config file for the translation of java objects to report parts (xsd in .jar and in GUI); and/or");
      out.println("  3 annotate your dataclasses ("+ContainerStart.class.getPackage().getName() +"), examples in junit tests");
      out.println("  4 extend " + DataCollectorImpl.class.getName() + ", example in junit test");
      out.println("  5 setup your classpath to include your classes, VectorPrintReport*.jar and dependent jars (see lib folder in binary distribution)");

      out.println("  6 java -cp <cp> " + ReportRunner.class.getName() + " <properties file> -dataclass <data class from step 3>\n");
      out.println("  You can look at the junit tests to see a working example\n");
      out.println("  Javadoc is recommended as a source for further details\n");
      out.println("Available stylers that can be configured in a properties file, together with parameters that may be used.");
      out.println("---------------------------------------------------------------------------------------------------------\n");
      out.println("  All parameters mentioned below can take defaults through properties in the form <(Parent)Class.getSimpleName().parameterName>=<value>\n");
   }

   public static void printHelpFooter(Document document, com.itextpdf.text.Font f) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, InvocationTargetException {
      document.add(new Paragraph("\nAvailable conditions for stylers that can be configured in a properties file, together with parameters that may be used."
          + "---------------------------------------------------------------------------------------------------------\n"
          + "  All parameters mentioned below can take defaults through properties in the form <(Parent)Class.getSimpleName().parameterName>=<value>\n", f));
      for (Class<?> c : ClassHelper.fromPackage(RegexCondition.class.getPackage())) {
         if (StylingCondition.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            StylingCondition s = (StylingCondition) c.newInstance();
            document.add(Chunk.NEWLINE);
            document.add(new Chunk(c.getSimpleName(), f).setLocalDestination(c.getSimpleName()));
            document.add(new Phrase(":  " + s.getHelp() + " \n  parameters available:\n", f));
            ByteArrayOutputStream o = new ByteArrayOutputStream(400);
            PrintStream out = new PrintStream(o);
            printParamInfo(s, out);
            out.flush();
            document.add(new Paragraph(o.toString(), f));
         }
      }
   }

   public static void printHelpFooter(PrintStream out) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      out.println("\nAvailable conditions for stylers that can be configured in a properties file, together with parameters that may be used.");
      out.println("---------------------------------------------------------------------------------------------------------\n");
      out.println("  All parameters mentioned below can take defaults through properties in the form <(Parent)Class.getSimpleName().parameterName>=<value>\n");
      for (Class<?> c : ClassHelper.fromPackage(RegexCondition.class.getPackage())) {
         if (StylingCondition.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            StylingCondition s = (StylingCondition) c.newInstance();
            out.println("\n" + c.getSimpleName() + ":  " + s.getHelp() + "\n  parameters available:\n");
            printParamInfo(s, out);
         }
      }
   }

   public static void printStylerHelp(Document document, com.itextpdf.text.Font f) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, DocumentException, NoSuchMethodException, InvocationTargetException {
      for (Class<?> c : ClassHelper.fromPackage(Font.class.getPackage())) {
         if (BaseStyler.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            BaseStyler s = (BaseStyler) c.newInstance();
            document.add(Chunk.NEWLINE);
            document.add(new Chunk(c.getSimpleName(), f).setLocalDestination(c.getSimpleName()));
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
      }
      printStylersPerClass(document, f);
   }
   private static Map<Class, Set<Class<? extends BaseStyler>>> stylersForClass = new HashMap<Class, Set<Class<? extends BaseStyler>>>(30);

   private static void addStylerForClass(BaseStyler bs) {
      for (Class cc : bs.getSupportedClasses()) {
         if (!stylersForClass.containsKey(cc)) {
            stylersForClass.put((Class) cc, new HashSet<Class<? extends BaseStyler>>(30));
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
    * Calls {@link #getParameterizables(java.lang.Package, java.lang.Class) } to collect {@link ElementStyler}s and
    * {@link FormFieldStyler}s.
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
   public static Set<BaseStyler> getStylers() throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      Set<BaseStyler> stylers = getParameterizables(AbstractStyler.class.getPackage(), BaseStyler.class);
      return stylers;
   }

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
      Set<P> parameterizables = new HashSet<P>(50);
      for (Class<?> c : ClassHelper.fromPackage(javaPackage)) {
         if (clazz.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            parameterizables.add((P) c.newInstance());
         }
      }
      return parameterizables;
   }

   public static void printStylerHelp(PrintStream out) throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      for (Class<?> c : ClassHelper.fromPackage(Font.class.getPackage())) {
         if (BaseStyler.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
            BaseStyler s = (BaseStyler) c.newInstance();
            out.println("\n" + c.getSimpleName() + ":  " + s.getHelp() + "\n  parameters available:");
            printParamInfo(s, out);
            out.println("  able to style: " + s.getSupportedClasses());
            addStylerForClass(s);
            if (s.creates()) {
               out.println("  creates supported Class");
            }
         }
      }
      printStylersPerClass(out);
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
      printHelpFooter(out);
   }

   public static void printParamInfo(Parameterizable p, PrintStream out) {
      for (Parameter e : p.getParameters().values()) {
         out.println("    " + e);
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

   public static String getParamInfo(Parameterizable p) {
      StringBuilder sb = new StringBuilder(p.getParameters().size() * 20).append("\n");
      for (Parameter e : p.getParameters().values()) {
         sb.append(e.toString()).append("\n");
      }
      return sb.toString();
   }
}
