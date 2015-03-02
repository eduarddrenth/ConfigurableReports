package com.vectorprint.report.itext.style.css;

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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.helpers.ParserFactory;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class CssTransformer {

   public static final String DEFAULTDOCUMENTHANDLER = CssDocumentHandler.class.getName();
   public static final String DOCUMENTHANDLERPROPERTY = CssToBaseStylers.class.getName().toLowerCase();
   public static final String NOVALIDATE = "-novalidate";
   public static final String USAGE = "java -jar "+CssTransformer.class.getName()+" <INPUTFILE> <OUTPUTFILE> " + NOVALIDATE + 
       System.getProperty("line.separator") + " (with "+NOVALIDATE+" unsupported css input will not cause an error)";

   /**
    * Calls {@link #transform(java.io.InputStream, java.io.OutputStream, boolean) }.
    * 
    * @param args {@link #NOVALIDATE -novalidate} indicates no css input validation, the first optional other argument is the css input file,
    * the second optional argument is the output file.
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      BufferedInputStream in = null;
      BufferedOutputStream out = null;
      boolean validate = true;
      if (args == null || args.length == 0) {
         // assume stream
         in = new BufferedInputStream(System.in);
         out = new BufferedOutputStream(System.out);
         PrintStream ps = new PrintStream(new File("system.out"));
         System.setOut(ps);

      } else {
         String input=null, output=null;
         for (String s : args) {
            if (NOVALIDATE.equals(s)) {
               validate = false;
            } else if (input==null) {
               input = s;
            } else {
               output = s;
            }
         }
         in = (input==null)?new BufferedInputStream(System.in):new BufferedInputStream(new FileInputStream(input));
         out = (output==null) ? new BufferedOutputStream(System.out) : new BufferedOutputStream(new FileOutputStream(output));
         if (output==null) {
            PrintStream ps = new PrintStream(new File("system.out"));
            System.setOut(ps);
         }
      }

      System.out.println(USAGE);

      transform(in, out, validate);

      out.close();

   }

   /**
    * Set the system property "org.w3c.css.sac.parser" to point to your {@link Parser} and optionally {@link #DOCUMENTHANDLERPROPERTY} to
    * point to your document handler.
    *
    * @param css
    * @param stylerSetup
    * @param validate when true validate css input (see {@link CssDocumentHandler#setMustFindStylersForCssNames(java.lang.Boolean) })
    * @throws ClassNotFoundException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws CSSException
    * @throws IOException
    */
   public static void transform(InputStream css, OutputStream stylerSetup, boolean validate) throws ClassNotFoundException, IllegalAccessException, InstantiationException, CSSException, IOException {
      // find and use a SAC parser and document handler
      String handler = System.getProperty(DOCUMENTHANDLERPROPERTY);
      CssToBaseStylers ctbs = (CssToBaseStylers) Class.forName((handler==null)?DEFAULTDOCUMENTHANDLER:handler).newInstance();
      if (ctbs instanceof CssDocumentHandler) {
         ((CssDocumentHandler)ctbs).setMustFindStylersForCssNames(validate);
      }
      Parser cssParser = new ParserFactory().makeParser();
      cssParser.setDocumentHandler(ctbs);
      cssParser.parseStyleSheet(new InputSource(new InputStreamReader(css)));

      ctbs.printStylers(stylerSetup);
   }

}
