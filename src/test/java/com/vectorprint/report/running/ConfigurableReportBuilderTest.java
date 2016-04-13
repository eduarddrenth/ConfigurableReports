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
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.parameters.ParamBindingService;
import com.vectorprint.configuration.binding.parameters.ParameterizableParser;
import com.vectorprint.configuration.binding.settings.EnhancedMapParser;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.FindableProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.jaxb.SettingsFromJAXB;
import com.vectorprint.configuration.jaxb.SettingsXMLHelper;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.CharPasswordParameter;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.PasswordParameter;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.HELP;
import static com.vectorprint.report.ReportConstants.VERSION;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ConfigurableReportBuilderTest {

   public static final String TARGET = "target" + File.separator;

   static {
      Security.addProvider(new BouncyCastleProvider());
   }

   public ConfigurableReportBuilderTest() throws IOException {
   }

   private static ReportRunner init(boolean allowEmpties, boolean pdfa, boolean continueAfterErrors, boolean forceException, boolean produceError) throws IOException, VectorPrintException, JAXBException {
      FindableProperties.clearStaticReferences();
      ReportRunner runner;
      if (pdfa) {
         runner = new ReportRunner(new SettingsFromJAXB().fromJaxb(SettingsXMLHelper.fromXML(new FileReader("src/test/resources/settingspdfa.xml"))));
      } else {
         runner = allowEmpties
             ? new ReportRunner(new SettingsFromJAXB().fromJaxb(SettingsXMLHelper.fromXML(new FileReader("src/test/resources/settings.xml"))))
             : new ReportRunner(new SettingsFromJAXB().fromJaxb(SettingsXMLHelper.fromXML(new FileReader("src/test/resources/settingsNoEmpties.xml"))));
      }
      runner.getSettings().put("continueAfterErrors", String.valueOf(continueAfterErrors));
      runner.getSettings().put("forceException", String.valueOf(forceException));
      runner.getSettings().put("produceError", String.valueOf(produceError));
      return runner;
   }

   @BeforeClass
   public static void setupClass() {
      Logger.getLogger(Settings.class.getName()).setLevel(Level.SEVERE);
   }

   @Before
   public void setUp() throws IOException, VectorPrintException, JAXBException {
      System.clearProperty(ReportConstants.JSON);
   }

   private void checkPdf(String path) throws IOException {
      checkPdf(new FileInputStream(path));
   }

   private void checkPdf(InputStream in) throws IOException {
      new PdfReader(in);
   }

   @Test
   public void testLoadFont() throws IOException, VectorPrintException, JAXBException {
      ReportRunner runner = init(true, false, false, false, false);
      FontLoader.LOADSTATUS stat = FontLoader.getInstance().setLoadiText(false).loadFont(new URL("file:src/test/resources/config" + runner.getSettings().getProperty("fontsemibold")));
      assertEquals(FontLoader.LOADSTATUS.NOT_LOADED, stat);

      stat = FontLoader.getInstance().setLoadiText(true).loadFont(new URL("file:src/test/resources/config" + runner.getSettings().getProperty("fontsemibold")));
      assertEquals(FontLoader.LOADSTATUS.LOADED_ONLY_ITEXT, stat);

   }

   @Test
   public void testLoadFieldStyler() throws IOException, VectorPrintException, Exception {
      ReportRunner runner = init(true, false, false, false, false);
      BaseReportGenerator bg = (BaseReportGenerator) runner.getReportGenerator();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(bg, runner.getSettings());
      int i = 0;
      for (BaseStyler bs : (List<BaseStyler>) bg.getStylers("field")) {
         if (i == 0) {
            assertTrue(bs instanceof Font);
         } else if (bs instanceof FormFieldStyler) {
            assertEquals("italic", bs.getValue(Font.STYLE_PARAM, Font.STYLE.class).name());
            return;
         }
         i++;
      }
      fail("styler setup for field not as expected");
   }

   /**
    * Test of provideSettings method, of class ThreadSafeReportBuilder.
    */
   @Test
   public void testGetSettings() throws IOException, VectorPrintException, JAXBException {
      ReportRunner runner = init(true, false, false, false, false);
      EnhancedMap result = runner.getSettings();
      assertNotNull(result.getProperty(ReportConstants.REPORTCLASS));
      assertNotNull(result.getProperty(ReportConstants.DATACLASS));
   }

   @Test
   public void testBuild() throws Exception {
      ReportRunner runner = init(true, false, false, false, false);
      PrintStream orig = System.out;
      ByteArrayOutputStream bo = new ByteArrayOutputStream(50 * 1024);
      System.setOut(new PrintStream(bo));
      runner.buildReport(null);
      System.setOut(orig);
      System.out.println("Bytes written: " + bo.toByteArray().length);
      checkPdf(new ByteArrayInputStream(bo.toByteArray()));
   }

   @Test
   public void testImportPdf() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()), "src/test/resources/config/stylingImportPdf.properties"))
          .buildReport(new String[]{"output=" + TARGET + "importPdf.pdf\ndataclass=com.vectorprint.report.running.ImportingDataCollector\ndebug=false"});
      checkPdf(TARGET + "importPdf.pdf");
   }

   @Test
   public void testImportTiff() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()), "src/test/resources/config/stylingImportTiff.properties"))
          .buildReport(new String[]{"output=" + TARGET + "importTiff.pdf\ndataclass=com.vectorprint.report.running.ImportingDataCollector\ndebug=false"});
      checkPdf(TARGET + "importTiff.pdf");
   }

   @Test
   public void testDefaultStylerSettings() throws Exception {
      init(true, false, false, false, false)
          .buildReport(new String[]{"output=" + TARGET + "style.pdf\nFont.color.set_default=#eeeeee"});
      checkPdf(TARGET + "style.pdf");
   }

   @Test
   public void testToStream() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{}, new FileOutputStream(TARGET + "testToStream.pdf"));
      checkPdf(TARGET + "testToStream.pdf");
   }

   @Test
   public void testToXmlConfig() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()), "src/test/resources/config/styling.properties")).buildReport(
          new String[]{ReportConstants.DATAMAPPINGXML + "=file:src/test/resources/DataMapping.xml\ndataclass=com.vectorprint.report.running.NonQueueingTestableDataCollector"}, new FileOutputStream(TARGET + "testToXmlConfig.pdf"));
      checkPdf(TARGET + "testToXmlConfig.pdf");
   }

   @Test
   public void testToc() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testToc.pdf\nDocumentSettings.toc.set_value=true"});

   }

   @Test
   public void testNoConsumer() throws Exception {
      try {
         ReportRunner.main(new String[]{"src/test/resources/config/styling.properties", "output=" + TARGET + "testNoConsumer.pdf\nqueuetimeout=3\ndataclass=com.vectorprint.report.running.TestableDataCollector"});
      } catch (VectorPrintRuntimeException ex) {
         assertTrue(ex.getMessage().contains("Failed to queue"));
      }
   }

   @Test
   public void testAnnotations() throws Exception {
      new ReportRunner(new ParsingProperties(new CachingProperties(new Settings()), "src/test/resources/config/styling.properties")).buildReport(new String[]{"output=" + TARGET + "testAnnotations.pdf\ndataclass=com.vectorprint.report.running.TestableDataCollector\ndebug=true\nqueuetimeout=20000"});
      checkPdf(TARGET + "testAnnotations.pdf");
   }
   @Rule
   public final ExpectedSystemExit exit = ExpectedSystemExit.none();

   @Test
   public void testMain() throws Exception {
      exit.expectSystemExitWithStatus(ReportRunner.EXITNOSETTINGS);
      ReportRunner.main(null);

      exit.expectSystemExitWithStatus(0);
      ReportRunner.main(new String[]{"src/test/resources/config/styling.properties",
         "output" + TARGET + "testMain.pdf\ndataclass=com.vectorprint.report.running.TestableDataCollector"});
      checkPdf(TARGET + "testMain.pdf");
   }

   @Test
   public void testEncryption() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testEncryption.pdf\ndocumentsettings=DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,password=password,ownerpassword=password)"});

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
         init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testCertificate.pdf\ndocumentsettings=DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,certificate=file:src/test/resources/config/eduarddrenth-TECRA-S11.crt)"});
      } catch (VectorPrintRuntimeException ex) {
         if (ex.getCause().getMessage().contains("Illegal key size")) {
            System.err.println("\nyou may need to install 'unlimitted strength policy' from oracle\n");
            return;
         }
         if (ex.getCause().getMessage().contains("No installed provider supports this key")) {
            System.err.println("\nyou may need to install 'unlimitted strength policy' from oracle\n");
            return;
         }
      }

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
         init(false, false, false, false, false);
         fail("exception expected for empty value");
      } catch (VectorPrintRuntimeException ex) {
         // expected
      }
   }

   @Test
   public void testPrintVersion() throws Exception {
      int res = init(true, false, false, false, false).buildReport(new String[]{VERSION + "="});
      assertEquals(ReportRunner.EXITFROMPROPERTYCODE, res);
   }

   @Test
   public void testPrintHelp() throws Exception {
      int res = init(true, false, false, false, false).buildReport(new String[]{HELP + "="});
      assertEquals(ReportRunner.EXITFROMPROPERTYCODE, res);
   }

   @Test
   public void testStopAfterErrors() throws Exception {
      init(true, false, false, false, true).buildReport(new String[]{"output=" + TARGET + "testStopAfterErrors.pdf"});
      checkPdf(TARGET + "testStopAfterErrors.pdf");
   }

   @Test
   public void testContinueAfterErrors() throws Exception {
      init(true, false, true, false, true).buildReport(new String[]{"output=" + TARGET + "testContinueAfterErrors.pdf"});
      checkPdf(TARGET + "testContinueAfterErrors.pdf");
   }

   @Test
   public void testBuildDebug() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testBuildDebug.pdf\ndebug=true"});
      checkPdf(TARGET + "testBuildDebug.pdf");
   }

   @Test
   public void testBuildInChildThread() throws InterruptedException, IOException, VectorPrintException, JAXBException {
      final ReportRunner runner = init(true, false, false, false, false);
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            try {
               runner.buildReport(new String[]{"output=" + TARGET + "testBuildInChildThread.pdf"});
               checkPdf(TARGET + "testBuildInChildThread.pdf");
            } catch (Exception ex) {
               ex.printStackTrace();
               fail("failed to build report in thread: " + ex.toString());
            }
         }
      });
      assertNotSame(TARGET + "testBuildInChildThread.pdf", runner.getSettings().get("output"));

   }

   private static class ReportRunnerFromThread implements Runnable {

      ReportRunner runner = null;

      @Override
      public void run() {
         try {
            runner = init(true, false, false, false, false);
         } catch (IOException | VectorPrintException | JAXBException ex) {
            fail("failed to init");
         }
      }
   }

   @Test
   public void testBuildInSiblingThread() throws InterruptedException {
      final ReportRunnerFromThread myRunner = new ReportRunnerFromThread();
      ThreadTester.testInThread(myRunner);
      try {
         assertTrue(myRunner.runner.getSettings().entrySet().isEmpty()
         );
         fail("expected failure to build from sibling thread");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            try {
               myRunner.runner.buildReport(new String[]{"output=" + TARGET + "testBuildInSiblingThread.pdf"});
               fail("expected failure to build from sibling thread");
            } catch (Exception ex) {
               //expected
            }
         }
      });
   }

   @Test
   public void testExceptionStop() throws Exception {
      new File("testExceptionStop.pdf").delete();
      try {
         init(true, false, true, true, false).buildReport(new String[]{"output=" + TARGET + "testExceptionStop.pdf\nstoponerror=true"});
         fail("exception expected");
      } catch (VectorPrintRuntimeException ex) {
         // expected
      }

      try {
         PdfReader pdfReader = new PdfReader(TARGET + "testExceptionStop.pdf");
         fail("pdf should be invalid");
      } catch (InvalidPdfException invalidPdfException) {
      } catch (IOException invalidPdfException) {
      }
   }

   @Test
   public void testSign() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testSign.pdf\ndocumentsettings=DocumentSettings(margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,width=297,height=210,keystore=file:src/test/resources/config/eduarddrenth-TECRA-S11.pfx,keystorepassword=password)\ndebug=true"});
      checkPdf(TARGET + "testSign.pdf");
   }

   @Test
   public void testExceptionContinue() throws Exception {
      new File(TARGET + "testExceptionContinue.pdf").delete();
      try {
         init(true, false, false, true, false).buildReport(new String[]{"output=" + TARGET + "testExceptionContinue.pdf\nstoponerror=false"});
      } catch (VectorPrintException ex) {
         ex.printStackTrace();
         fail("exception not expected");
      }

      checkPdf(TARGET + "testExceptionContinue.pdf");
   }

   @Test
   public void testPdfA1A() throws Exception {
      init(true, true, false, false, false).buildReport(new String[]{"output=" + TARGET + "testPdfA1A.pdf\nfonts=src/test/resources/config"});
      checkPdf(TARGET + "testPdfA1A.pdf");
   }

   @Test
   public void testBoxes() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testBoxes.pdf\n"
         + "documentsettings=DocumentSettings("
         + "margin_top=5,margin_left=50,margin_right=5,margin_bottom=5,"
         + "width=297,height=210,bleedbox=-10|-10|317|230,cropbox=50|50|247|160)"});
      checkPdf(TARGET + "testBoxes.pdf");
   }

   @Test
   public void testLoadFonts() throws Exception {
      init(true, false, false, false, false).buildReport(new String[]{"output=" + TARGET + "testLoadFonts.pdf\n"
         + "fonts=src/test/resources/config"});
      Assert.assertEquals("myriad pro", FontFactory.getFont("myriad pro").getFamilyname().toLowerCase());
      Assert.assertNotNull(FontFactory.getFont("myriad pro").getBaseFont());
      checkPdf(TARGET + "testLoadFonts.pdf");
   }

   @Test
   public void testHelp() throws Exception {
      Set<Parameterizable> stylersAndConditions = Help.getStylersAndConditions();
      assertFalse(stylersAndConditions.isEmpty());
   }

   private void setVal(Parameter parameter, EnhancedMap settings) {
      ParameterizableParser parser = ParamBindingService.getInstance().getFactory().getParser(new StringReader(parameter.getKey() + "=" + settings.getProperty(parameter.getKey())));
      ParamBindingService.getInstance().getFactory().getBindingHelper().setValueOrDefault(parameter, parser.parseAsParameterValue(settings.getPropertyNoDefault(parameter.getClass().getSimpleName()), parameter), false);
   }

   @Test
   public void testParameters() throws Exception {
      String[] testStrings = new String[]{"lt", "ean8", "Helvetica", "overlay", "enc128", "bold", "combo", "pkcs12", "top", "rectangle"};
      new FloatArrayParameter("k", "h").setValue(new float[]{50f, 50f});
      Settings settings = new Settings();
      for (Class c : ClassHelper.fromPackage(AlignParameter.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers())) {
            if (ParameterImpl.class.isAssignableFrom(c)) {
               Constructor con = c.getConstructor(String.class, String.class);
               Parameter p = (Parameter) con.newInstance(c.getSimpleName(), "some help");
               Parameter cl = p.clone();
               assertEquals(p, cl);
               assertNotNull(cl.getValueClass());
               if (p instanceof BooleanParameter || Number.class.isAssignableFrom(p.getValueClass())) {
                  assertNotNull(p.getValue());
                  assertNotNull(p.getDefault());
                  assertNotNull(cl.getValue());
                  assertNotNull(cl.getDefault());
               }
               for (String init : testStrings) {
                  try {
                     settings.clear();
                     EnhancedMapParser parser = SettingsBindingService.getInstance().getFactory().getParser(new StringReader(c.getSimpleName() + "=" + init));
                     parser.parse(settings);
                     setVal(p, settings);
                     assertNotNull(p.toString(), p.getValue());
                     if (!(p instanceof BooleanParameter && !"true".equals(init)) && !(p instanceof CharPasswordParameter) && !(p instanceof PasswordParameter)) {
                        assertNotSame(p.getValue(), cl.getValue());
                     }
                     if (p instanceof PasswordParameter || p instanceof CharPasswordParameter) {
                        // password cleared by getValue
                        assertNull(p.getValue());
                        continue;
                     }
                     BindingHelper stringConversion = ParamBindingService.getInstance().getFactory().getBindingHelper();
                     String conf = stringConversion.serializeValue(p.getValue());

                     if (conf != null && !"".equals(conf)) {
                        ParamBindingService.getInstance().getFactory().getParser(new StringReader("")).parseAsParameterValue(stringConversion.serializeValue(p.getValue()), p);
                     }
                  } catch (NumberFormatException runtimeException) {
                     System.out.println(runtimeException.getMessage());
                  } catch (IllegalArgumentException runtimeException) {
                     if (runtimeException.getMessage().contains("No enum const")) {
                        System.out.println(runtimeException.getMessage());
                     } else {
                        throw runtimeException;
                     }
                  } catch (VectorPrintRuntimeException runtimeException) {
                     if (runtimeException.getCause() instanceof MalformedURLException
                         || runtimeException.getCause() instanceof NoSuchFieldException
                         || runtimeException.getCause() instanceof ClassNotFoundException
                         || runtimeException.getMessage().contains("No basefont for: ")) {
                        System.out.println(runtimeException.getMessage());
                     } else {
                        throw runtimeException;
                     }
                  }
               }
            }
         }
      }
   }
}
