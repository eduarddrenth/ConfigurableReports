/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.itextpdf.text.pdf;

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

import com.itextpdf.text.DocumentException;
import static com.itextpdf.text.pdf.BaseFont.fontCache;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import java.io.IOException;

/**
 * 
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class VectorPrintBaseFont extends BaseFont {
    /**
     * @see DocumentSettings#builtInFontHack() 
     * @param name
     * @param pfb
     * @throws DocumentException
     * @throws IOException
     * @throws VectorPrintException 
     */
    public static void cacheAndEmbedBuiltInFont(String name, byte[] pfb) throws DocumentException, IOException, VectorPrintException {
       if (!BuiltinFonts14.containsKey(name)) {
          throw new VectorPrintException(name + " is not a builtin font");
       }
       Type1Font bf = new Type1Font(name, BaseFont.WINANSI, true, new byte[0], pfb, false);
       bf.embedded = true;
       bf.pfb = pfb;
       String key = name + "\n" + BaseFont.WINANSI + "\n" + Boolean.TRUE;
       fontCache.put(key, bf);
       key = name + "\n" + BaseFont.WINANSI + "\n" + Boolean.FALSE;
       fontCache.put(key, bf);
    }
}
