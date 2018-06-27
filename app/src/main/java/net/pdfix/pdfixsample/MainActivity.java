package net.pdfix.pdfixsample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import net.pdfix.pdfixlib.PdfDoc;
import net.pdfix.pdfixlib.Pdfix;
import net.pdfix.pdftohtml.PdfHtmlDoc;
import net.pdfix.pdftohtml.PdfHtmlParams;
import net.pdfix.pdftohtml.PdfToHtml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

  private Pdfix pdfix = null;
  private PdfToHtml pdfToHtml = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // setup webView
    WebView webView = findViewById(R.id.pdfixwebview);
    setupWebView(webView);

    String email = "YOUR@EMAIL";
    String licenseKey = "LICENSE_KEY";

    try {
      initializePdfix(email, licenseKey);

	  // extract file from assets to disk
      String pdfPath = getExternalCacheDir().getAbsolutePath() + "example.pdf";
      saveAssetFile("example.pdf", pdfPath);

      // open PdfDoc
      PdfDoc pdfDoc = pdfix.OpenDoc(pdfPath, "");
      if (pdfDoc == null)
        throw new RuntimeException("Pdfix.OpenDoc failed");

      // create PdfHtmlDoc
      PdfHtmlDoc pdfHtmlDoc = pdfToHtml.OpenHtmlDoc(pdfDoc);
      if (pdfHtmlDoc == null)
        throw new RuntimeException("PdfToHtml.OpenHtmlDoc failed");

	  // save html
      String htmlPath = getExternalCacheDir().getAbsolutePath() + "index.html";
      PdfHtmlParams htmlParams  = new PdfHtmlParams();
      if (!pdfHtmlDoc.Save(htmlPath, htmlParams))
        throw new RuntimeException("PdfHtmlDoc.Save failed");

      String url = "file://" + htmlPath;
      webView.loadUrl(url);

      pdfDoc.Close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
      dlgAlert.setTitle("Pdfix Androis Sample");
      dlgAlert.setMessage(ex.getMessage());
      dlgAlert.setPositiveButton("Ok",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            //dismiss the dialog
          }
        });
      dlgAlert.show();
    }
  }

  @Override
  protected void onDestroy() {
    try {
      destroyPdfix();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    super.onDestroy();
  }

    protected void initializePdfix(String email, String licenseKey) {
    try {
      System.loadLibrary("pdfix");
      System.loadLibrary("pdf_to_html");
    } catch (UnsatisfiedLinkError | SecurityException | NullPointerException e) {
      e.printStackTrace();
      throw new RuntimeException("Loading PDFix libraries failed.");
    }

    if (pdfix == null)
      pdfix = new Pdfix();
    if (pdfix == null)
      throw new RuntimeException("Pdfix initialization fail");
    if (!pdfix.Authorize(email, licenseKey))
      throw new RuntimeException("PDFix Authorization fail.");

    if (pdfToHtml == null)
      pdfToHtml = new PdfToHtml();
    if (pdfToHtml == null)
      throw new RuntimeException("PdfToHtml initialization fail");
    if (!pdfToHtml.Initialize(pdfix))
      throw new RuntimeException("PdfToHtml Pdfix initialization fail " + pdfix.GetError());
  }

  protected void destroyPdfix() {
    if (pdfToHtml != null) {
      pdfToHtml.Destroy();
      pdfToHtml = null;
    }
    if (pdfix != null) {
      pdfix.Destroy();
      pdfix = null;
    }
  }

  protected void saveAssetFile(String name, String path) throws IOException {
    // save pdf from assets
    InputStream is = getAssets().open(name);
    byte[] data = new byte[is.available()];
    is.read(data);
    is.close();

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
    bos.write(data);
    bos.flush();
    bos.close();
  }

  protected void setupWebView(WebView webView) {
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setAllowContentAccess(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      webSettings.setAllowFileAccessFromFileURLs(true);
      webSettings.setAllowUniversalAccessFromFileURLs(true);
    }
    webSettings.setUseWideViewPort(false);
    webSettings.setSupportZoom(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDisplayZoomControls(false);
  }
}
