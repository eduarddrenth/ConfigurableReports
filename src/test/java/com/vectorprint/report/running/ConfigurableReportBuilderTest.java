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
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.vectorprint.ClassHelper;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.certificates.CertificateHelper;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.FindableProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.parameters.CharPasswordParameter;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterHelper;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.PasswordParameter;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.HELP;
import static com.vectorprint.report.ReportConstants.VERSION;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.FontLoader;
import com.vectorprint.report.itext.style.FormFieldStyler;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.parameters.AlignParameter;
import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import com.vectorprint.report.itext.style.stylers.Font;
import com.vectorprint.testing.ThreadTester;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Set;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ConfigurableReportBuilderTest {

   private static ThreadSafeReportBuilder<ReportDataHolder> instance;
   public static final String TARGET = "target" + File.separator;

   static {
      Security.addProvider(new BouncyCastleProvider());
   }

   public ConfigurableReportBuilderTest() throws IOException {
   }

   private static void init(boolean allowEmpties) throws IOException, VectorPrintException, ParseException {
      FindableProperties.clearStaticReferences();
      instance = new ThreadSafeReportBuilder("src/test/resources/config",
          ThreadSafeReportBuilder.DEFAULTPROPERTYURLS.toArray(new String[ThreadSafeReportBuilder.DEFAULTPROPERTYURLS.size()]), allowEmpties, true);
   }

   @AfterClass
   public static void tearDownClass() {
   }

   @Before
   public void setUp() throws IOException, VectorPrintException, ParseException {
      init(true);
      TestableReportGenerator.setDidCreate(false);
      TestableReportGenerator.setForceException(false);

   }

   @After
   public void tearDown() {
   }

   @Test
   public void testLoadFont() throws IOException, VectorPrintException {
      FontLoader.LOADSTATUS stat = FontLoader.getInstance().setLoadiText(false).loadFont(new URL("file:src/test/resources/config" + instance.getSettings().getProperty("fontsemibold")));
      assertEquals(FontLoader.LOADSTATUS.NOT_LOADED, stat);

      stat = FontLoader.getInstance().setLoadiText(true).loadFont(new URL("file:src/test/resources/config" + instance.getSettings().getProperty("fontsemibold")));
      assertEquals(FontLoader.LOADSTATUS.LOADED_ONLY_ITEXT, stat);

   }

   /**
    * Test of getConfigBaseUrl method, of class ThreadSafeReportBuilder.
    */
   @Test
   public void testGetConfigDir() throws IOException {
      String expResult = "src/test/resources/config";
      String result = instance.getSettings().getProperty(ThreadSafeReportBuilder.CONFIG_URL);
      assertEquals(expResult, result);
   }

   @Test
   public void testLoadFieldStyler() throws IOException, VectorPrintException, Exception {
      BaseReportGenerator bg = (BaseReportGenerator) instance.getReportGenerator();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(bg, instance.getSettings());
      int i = 0;
      for (BaseStyler bs : (List<BaseStyler>) bg.getStylers("field")) {
         if (i == 0) {
            assertTrue(bs instanceof Font);
         } else {
            if (bs instanceof FormFieldStyler) {
               assertEquals("italic", bs.getValue(Font.STYLE_PARAM, Font.STYLE.class).name());
               return;
            }
         }
         i++;
      }
      fail("styler setup for field not as expected");
   }

   /**
    * Test of provideSettings method, of class ThreadSafeReportBuilder.
    */
   @Test
   public void testGetSettings() throws IOException {
      EnhancedMap result = instance.getSettings();
      assertNotNull(result.getProperty(ReportConstants.REPORTCLASS));
      assertNotNull(result.getProperty(ReportConstants.DATACLASS));
   }

   @Test
   public void testBuild() throws Exception {
      PrintStream orig = System.out;
      ByteArrayOutputStream bo = new ByteArrayOutputStream(50 * 1024);
      System.setOut(new PrintStream(bo));
      instance.buildReport(new String[]{"-output", ReportConstants.STREAM});
      System.setOut(orig);
      assertTrue(TestableReportGenerator.isDidCreate());
      System.out.println("Bytes written: " + bo.toByteArray().length);
      assertTrue(bo.toByteArray().length > 0);
   }

   @Test
   public void testImportPdf() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()),"src/test/resources/config/stylingImportPdf.properties"))
          .buildReport(new String[]{"-output", TARGET + "importPdf.pdf", "-dataclass", "com.vectorprint.report.running.ImportingDataCollector", "-debug", "false"});
   }

   @Test
   public void testImportTiff() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()),"src/test/resources/config/stylingImportTiff.properties"))
          .buildReport(new String[]{"-output", TARGET + "importTiff.pdf", "-dataclass", "com.vectorprint.report.running.ImportingDataCollector", "-debug", "false"});
   }

   @Test
   public void testDefaultStylerSettings() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "style.pdf", "-Font.color", "#eeeeee"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testToStream() throws Exception {
      instance.buildReport(new String[]{}, new FileOutputStream(TARGET + "testToStream.pdf"));
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testToXmlConfig() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()),"src/test/resources/config/styling.properties")).buildReport(new String[]{'-' + ReportConstants.DATAMAPPINGXML, "file:src/test/resources/DataMapping.xml", "-dataclass", "com.vectorprint.report.running.NonQueueingTestableDataCollector"}, new FileOutputStream(TARGET + "testToXmlConfig.pdf"));
   }

   @Test
   public void testToc() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "testToc.pdf", "-DocumentSettings.toc", "true", "-debug", "false"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testNoConsumer() throws Exception {
      try {
      ReportRunner.main(new String[]{"src/test/resources/config/styling.properties","-output", TARGET + "testNoConsumer.pdf", "-queuetimeout", "3", "-dataclass", "com.vectorprint.report.running.TestableDataCollector"});
      } catch (VectorPrintRuntimeException ex) {
         assertTrue(ex.getMessage().contains("Failed to queue"));
      }
   }

   @Test
   public void testAnnotations() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()),"src/test/resources/config/styling.properties")).buildReport(new String[]{"-output", TARGET + "testAnnotations.pdf", "-dataclass", "com.vectorprint.report.running.TestableDataCollector", "-debug", "true", "-queuetimeout", "20000"});
   }
   @Rule
   public final ExpectedSystemExit exit = ExpectedSystemExit.none();

   @Test
   public void testMain() throws Exception {
      exit.expectSystemExitWithStatus(ReportRunner.EXITNOSETTINGS);
      ReportRunner.main(null);

      exit.expectSystemExitWithStatus(0);
      ReportRunner.main(new String[]{"src/test/resources/config/styling.properties",
         "-output", TARGET + "testMain.pdf",
         "-dataclass", "com.vectorprint.report.running.TestableDataCollector"});

   }

   @Test
   public void testEncryption() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "testEncryption.pdf",
         "-documentsettings", "DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,password=password,ownerpassword=password)"});
      assertTrue(TestableReportGenerator.isDidCreate());
      try {
         PdfReader pdfReader = new PdfReader(TARGET + "testEncryption.pdf");
         fail("setting password and encryption failed");
      } catch (BadPasswordException badPasswordException) {
         // expected
      }
      PdfReader pdfReader = new PdfReader(TARGET + "testEncryption.pdf", "password".getBytes());
   }

   @Test
   public void testCertificate() throws Exception {
      try {
         instance.buildReport(new String[]{"-output", TARGET + "testCertificate.pdf",
            "-documentsettings", "DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,certificate=file:src/test/resources/config/eduarddrenth-TECRA-S11.crt)"});
      } catch (VectorPrintRuntimeException ex) {
         if (ex.getCause().getMessage().contains("Illegal key size")) {
            System.err.println("\nyou may need to install 'unlimitted strength policy' from oracle\n");
            return;
         }
      }
      assertTrue(TestableReportGenerator.isDidCreate());

      try {
         PdfReader pdfReader = new PdfReader(TARGET + "testCertificate.pdf");
      } catch (InvalidPdfException ex) {
         // expected
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048000);
      IOHelper.load(new URL("file:src/test/resources/config/eduarddrenth-TECRA-S11.crt").openStream(), out);
      Certificate cert = CertificateFactory.getInstance("X.509")
          .generateCertificate(new ByteArrayInputStream(out.toByteArray()));
      PdfReader pdfReader = new PdfReader(TARGET + "testCertificate.pdf", cert, getPrivateKey(), "BC");
   }

   private Key getPrivateKey() throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
      return CertificateHelper.getKey(CertificateHelper.loadKeyStore(new FileInputStream("src/test/resources/config/keystore"), "jks", "password".toCharArray()), "password".toCharArray());
   }

   @Test
   public void testBuildNoEmptyValues() throws Exception {
      try {
         init(false);
         fail("exception expected for empty value");
         instance.buildReport(new String[]{"-output", TARGET + "testBuildNoEmptyValues.pdf"});
         assertTrue(TestableReportGenerator.isDidCreate());
      } catch (VectorPrintRuntimeException ex) {
         // expected
      }
   }

   @Test
   public void testPrintVersion() throws Exception {
      int res = instance.buildReport(new String[]{"-" + VERSION});
      assertEquals(ReportRunner.EXITFROMPROPERTYCODE, res);
      assertFalse(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testPrintHelp() throws Exception {
      int res = instance.buildReport(new String[]{"-" + HELP});
      assertEquals(ReportRunner.EXITFROMPROPERTYCODE, res);
      assertFalse(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testStopAfterErrors() throws Exception {
      TestableReportGenerator.setContinueAfterError(false);
      NonQueueingTestableDataCollector.setProduceError(true);
      instance.buildReport(new String[]{"-output", TARGET + "testStopAfterErrors.pdf"});
      assertFalse(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testContinueAfterErrors() throws Exception {
      TestableReportGenerator.setContinueAfterError(true);
      NonQueueingTestableDataCollector.setProduceError(true);
      instance.buildReport(new String[]{"-output", TARGET + "testContinueAfterErrors.pdf"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testBuildDebug() throws Exception {
      NonQueueingTestableDataCollector.setProduceError(false);
      instance.buildReport(new String[]{"-output", TARGET + "testBuildDebug.pdf", "-debug", "true"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testBuildInChildThread() throws InterruptedException {
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            try {
               instance.buildReport(new String[]{"-output", TARGET + "testBuildInChildThread.pdf"});
            } catch (Exception ex) {
               ex.printStackTrace();
               fail("failed to build report in thread");
            }
         }
      });
      assertNotSame(TARGET + "testBuildInChildThread.pdf", instance.getSettings().get("output"));

      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testBuildInSiblingThread() throws InterruptedException {
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            try {
               init(true);
            } catch (Exception ex) {
               fail("failed to init");
            }
         }
      });
      try {
         assertTrue(instance.getSettings().entrySet().isEmpty());
         fail("expected failure to build from sibling thread");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            try {
               instance.buildReport(new String[]{"-output", TARGET + "testBuildInSiblingThread.pdf"});
               fail("expected failure to build from sibling thread");
            } catch (Exception ex) {
               //expected
            }
         }
      });
   }

   @Test
   public void testExceptionStop() throws Exception {
      TestableReportGenerator.setForceException(true);
      new File("testExStop.pdf").delete();
      try {
         instance.buildReport(new String[]{"-output", TARGET + "testExceptionStop.pdf", "-stoponerror", "true"});
         fail("exception expected");
      } catch (VectorPrintRuntimeException ex) {
         // expected
      }
      assertTrue(TestableReportGenerator.isDidCreate());
      try {
         PdfReader pdfReader = new PdfReader(TARGET + "testExceptionStop.pdf");
         fail("pdf should be invalid");
      } catch (InvalidPdfException invalidPdfException) {
      } catch (IOException invalidPdfException) {
      }
   }

   @Test
   public void testSign() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "testSign.pdf", "-documentsettings", "DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,keystore=file:src/test/resources/config/eduarddrenth-TECRA-S11.pfx,keystorepassword=password)", "-debug", "true"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testExceptionContinue() throws Exception {
      TestableReportGenerator.setForceException(true);
      new File(TARGET + "testExContinue.pdf").delete();
      try {
         instance.buildReport(new String[]{"-output", TARGET + "testExceptionContinue.pdf", "-stoponerror", "false"});
      } catch (VectorPrintException ex) {
         ex.printStackTrace();
         fail("exception not expected");
      }
      assertTrue(TestableReportGenerator.isDidCreate());
      assertTrue(new File(TARGET + "testExceptionContinue.pdf").exists());
      assertTrue(new File(TARGET + "testExceptionContinue.pdf").length() > 0);
   }

   @Test
   public void testPdfA1A() throws Exception {

      String[] props = ThreadSafeReportBuilder.DEFAULTPROPERTYURLS.toArray(new String[ThreadSafeReportBuilder.DEFAULTPROPERTYURLS.size()]);
      props[1] = "styling_pdfa1a.properties";
      instance = new ThreadSafeReportBuilder("src/test/resources/config", props, true, true);
      instance.buildReport(new String[]{"-output", TARGET + "testPdfA1A.pdf", "-fonts", "src/test/resources/config"});
      assertTrue(TestableReportGenerator.isDidCreate());
   }

   @Test
   public void testBoxes() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "testBoxes.pdf",
         "-documentsettings", "DocumentSettings("
         + "margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,"
         + "width=297,height=210,bleed=-10|-10|317|230,crop=50|50|247|160)"});
   }

   @Test
   public void testLoadFonts() throws Exception {
      instance.buildReport(new String[]{"-output", TARGET + "testLoadFonts.pdf",
         "-fonts", "src/test/resources/config"});
      Assert.assertEquals("myriad pro", FontFactory.getFont("myriad pro").getFamilyname().toLowerCase());
      Assert.assertNotNull(FontFactory.getFont("myriad pro").getBaseFont());
   }

   @Test
   public void testParameters() throws Exception {
      String[] testStrings = new String[]{"lt", "ean8", "Helvetica", "overlay", "enc128", "bold", "combo", "pkcs12", "top", "rectangle"};
      new FloatArrayParameter("k", "h").setValue(new Float[]{50f, 50f});
      for (Class c : ClassHelper.fromPackage(AlignParameter.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers())) {
            if (ParameterImpl.class.isAssignableFrom(c)) {
               Constructor con = c.getConstructor(String.class, String.class);
               Parameter p = (Parameter) con.newInstance(c.getSimpleName(), "some help");
               for (String init : testStrings) {
                  try {
                     p.setValue(p.convert(init));
                     if (p.getValue() == null) {
                        fail(p.toString());
                     }
                     assertNotNull(p.getValue());
                     if (PasswordParameter.class.isAssignableFrom(c) || CharPasswordParameter.class.isAssignableFrom(c)) {
                        assertNull(p.getValue());
                        continue;
                     }
                     String conf = ParameterHelper.toConfig(p, true).toString();
                     if (null != conf && !"".equals(conf)) {
                        if (p.getValue().getClass().isArray()) {
                           Object[] orig = (Object[]) p.getValue();
                           Object[] neww = (Object[]) p.convert(conf.substring(conf.indexOf('=') + 1));
                           Assert.assertArrayEquals(orig, neww);
                        } else {
                           assertEquals(p.serializeValue(p.getValue()), conf.substring(conf.indexOf('=') + 1));
                        }
                     }
                  } catch (NumberFormatException runtimeException) {
                     runtimeException.printStackTrace();
                  } catch (IllegalArgumentException runtimeException) {
                     if (runtimeException.getMessage().contains("No enum const")) {
                        runtimeException.printStackTrace();
                     } else {
                        throw runtimeException;
                     }
                  } catch (VectorPrintRuntimeException runtimeException) {
                     if (runtimeException.getMessage().contains("No basefont for")) {
                        runtimeException.printStackTrace();
                     } else {
                        throw runtimeException;
                     }
                  }
               }
            }
         }
      }
   }
   
   @Test
   public void testHelp() throws Exception {
      Set<Parameterizable> stylersAndConditions = Help.getStylersAndConditions();
      assertFalse(stylersAndConditions.isEmpty());
   }
}
