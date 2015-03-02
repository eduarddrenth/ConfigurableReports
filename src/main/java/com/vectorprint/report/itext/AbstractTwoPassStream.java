package com.vectorprint.report.itext;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * subclass supporting writing to an outputstream in two phases.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
abstract class AbstractTwoPassStream extends OutputStream implements TwoPassOutputStream {

   private OutputStream out = null, orig = null;
   private int bufferSize;
   private File tempFile;

   public AbstractTwoPassStream(OutputStream out, int bufferSize) throws IOException {
      this.bufferSize = bufferSize;
      orig = out;
      tempFile = File.createTempFile(getClass().getSimpleName(), "pdf");
      tempFile.deleteOnExit();
      this.out = new BufferedOutputStream(new FileOutputStream(tempFile), bufferSize);
   }

   /**
    * closes the outputstream and calls {@link #secondPass(java.io.OutputStream) } with the original outputstream. Note
    * that the original outputstream may be null when a subclass did not call {@link #setOut(java.io.OutputStream) }
    *
    * @throws IOException
    */
   @Override
   public final void close() throws IOException {
      out.close();
      if (orig != null) {
         secondPass(new BufferedInputStream(new FileInputStream(tempFile), bufferSize), orig);
         tempFile.delete();
      }
   }

   /**
    * writes to the wrapped outputstream.
    *
    * @param b
    * @throws IOException
    */
   @Override
   public final void write(int b) throws IOException {
      out.write(b);
   }


}
