/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.running;

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
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.MultipleValueParser;
import com.vectorprint.report.data.DataCollectionMessages;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.DefaultElementProducer;
import com.vectorprint.report.itext.EventHelper;
import java.util.Date;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class TestableReportGenerator extends BaseReportGenerator<ReportDataHolder> {

   private static boolean continueAfterError = false;
   private static boolean didCreate = false;
   private static boolean forceException = false;
   private static boolean annotations = false;

   public TestableReportGenerator() throws VectorPrintException {
      super(new EventHelper<ReportDataHolder>(), new DefaultElementProducer());
      didCreate = false;
   }

   @Override
   protected void createReportBody(Document document, ReportDataHolder data, com.itextpdf.text.pdf.PdfWriter writer) throws DocumentException, VectorPrintException {
      didCreate = true;
      if (annotations) {
         processData(data);
         return;
      }
      try {
         getAndAddIndex("Eerste", 1, "chapter");
         getAndAddIndex("Nest 1", 2, "niveau1");
         // you can use any number of style classes
         createAndAddElement(getText("TestReportValue"), Paragraph.class, "bold", "empty");

         // you can add raw values to report elements, subclasses of ReportValue or format ReportValues yourself
         createAndAddElement("TestRaw", Paragraph.class, "bold", "empty");
         createAndAddElement(formatValue(getText("TestFormatted")), Paragraph.class, "bold", "empty");

         createAndAddElement(
             getText("dit is een hele lange \ntext met een grote\nregelhoogte"), Phrase.class, "wide");
         newLine();
         createColumns(getStylers("kols")).addContent(
             "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom "
             + "kolom ", "bold").write(false).addContent(createElement("9992345678906", Image.class, "bc"), null).write();

         newLine();
         createAndAddElement(
             getText("dit is een hele lange \ntext met een kleine\nregelhoogte"), Phrase.class, "narrow");
         document.newPage();
         getAndAddIndex("Nest 1", 2, "niveau1");
         createAndAddElement("Link", Anchor.class, "empty");
         newLine();
         getAndAddIndex("Nest 1", 3, "niveau2", "link");
         createAndAddElement("Item", ListItem.class, "bigbold");
         newLine();
         createAndAddElement(getNumber(10.23), Phrase.class, "header");
         newLine();
         createAndAddElement(getDate(new Date()), Phrase.class, "header");
         newLine();
         createAndAddElement(getMoney(25.95), Phrase.class, "header");
         newLine();
         createAndAddElement(getPercentage(99.5), Phrase.class, "header");
         newLine();
         createAndAddElement(getDuration(200000), Phrase.class, "header");
         newLine();
         long d = new Date().getTime();
         createAndAddElement(getPeriod(d, d + 1000 * 60 * 60 * 48), Phrase.class, "header");
         newLine();
         document.add(getIndex("Tweede", 1, "chapter"));
         com.vectorprint.report.itext.style.stylers.Image ims
             = new com.vectorprint.report.itext.style.stylers.Image(this, this, document, writer, getSettings());
         // the setters here could also be done from setup
         ims.setUrl(MultipleValueParser.URL_PARSER.parseString(getSettings().getProperty(ThreadSafeReportBuilder.CONFIG_URL)
             + "/" + "zon.pdf"));
         ims.setPdf(true);
         ims.setShifty(50);
         ims.setTransform(new Float[]{2f, 25f, 2f, 2f, 0f, 0f});
         ims.setShiftx(document.getPageSize().getLeft());
         if (writer.getPDFXConformance() != PdfWriter.PDFX1A2001) {
            ims.setOpacity(0.3f);
         }
         ims.draw(new Rectangle(10, 10, 100, 100), "");
         newLine();
         if (writer.getPDFXConformance() != PdfWriter.PDFX1A2001) {
            document.add(loadImage(MultipleValueParser.URL_PARSER.parseString(getSettings().getProperty(ThreadSafeReportBuilder.CONFIG_URL)
                + "/" + "pointer.png"), 1));
         }
         newLine();

         document.newPage();
         /**
          * here you add a barcode to the document using styling configuration
          *
          */
         createAndAddElement("8882345678906", Image.class, "bc");

         document.add(getIndex("Nest 2", 2, "niveau1"));

         PdfPTable t = createElement(null, PdfPTable.class, "table");
         t.addCell(createElement("c1", PdfPCell.class, "headerleft"));
         t.addCell(createElement("c2", PdfPCell.class, "headerleft"));
         t.addCell(createElement("c3", PdfPCell.class, "headerleft"));
         t.addCell(createElement("c4", PdfPCell.class, "headerleft"));
         t.addCell(createElement("c5", PdfPCell.class, "headerleft"));
         // form fields are annotations and always added to a cell for positioning and sizing
         t.addCell(createElement(null, PdfPCell.class, "field"));
         t.addCell(createElement("knoppie", PdfPCell.class, "headerleft"));
         t.addCell(createElement(null, PdfPCell.class, "field1"));
         t.addCell(createElement(null, PdfPCell.class, "field2"));
         t.addCell(createElement(null, PdfPCell.class, "field3"));
         t.addCell(createElement(null, PdfPCell.class, "field4"));
         t.addCell(createElement(null, PdfPCell.class, "field5"));
         t.addCell(createElement("c7", PdfPCell.class, "headerleft"));

         // nested
         PdfPTable t2 = createElement(null, PdfPTable.class, "tablesmall");
         t2.addCell(createElement("n1", PdfPCell.class, "headerleft"));
         t2.addCell(createElement("n2", PdfPCell.class, "headerleft"));
         t2.addCell(createElement("n3", PdfPCell.class, "headerleftc2"));
         t.addCell(createElement(t2, PdfPCell.class, "headerleft"));

         document.add(t);

      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
      if (forceException) {
         writer.getDirectContent().moveTo(10, 10);
         throw new VectorPrintException("forced exception");
      }
   }

   @Override
   public boolean continueOnDataCollectionMessages(DataCollectionMessages messages, com.itextpdf.text.Document document) throws VectorPrintException {
      if (!messages.getMessages(DataCollectionMessages.Level.ERROR).isEmpty()) {
         try {
            createAndAddElement(messages.getMessages(DataCollectionMessages.Level.ERROR), Phrase.class, "bigbold");
         } catch (InstantiationException ex) {
            throw new VectorPrintException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintException(ex);
         } catch (DocumentException ex) {
            throw new VectorPrintException(ex);
         }
         return !continueAfterError;
      }
      return true;
   }

   public static void setContinueAfterError(boolean continueAfterError) {
      TestableReportGenerator.continueAfterError = !continueAfterError;
   }

   public static boolean isDidCreate() {
      return didCreate;
   }

   public static void setDidCreate(boolean didCreate) {
      TestableReportGenerator.didCreate = didCreate;
   }

   public static void setForceException(boolean forceException) {
      TestableReportGenerator.forceException = forceException;
   }

   public static boolean isAnnotations() {
      return annotations;
   }

   public static void setAnnotations(boolean annotations) {
      TestableReportGenerator.annotations = annotations;
   }

}
