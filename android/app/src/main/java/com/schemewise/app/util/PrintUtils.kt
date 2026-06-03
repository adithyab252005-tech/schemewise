package com.schemewise.app.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrintUtils {

    /**
     * Generates an HTML document and sends it to the Android PrintManager,
     * which prompts the user with a native dialog to "Save as PDF" or send to a printer.
     */
    fun printHtmlToPdf(
        context: Context,
        documentTitle: String,
        htmlContent: String
    ) {
        // WebView must be instantiated on the main thread, but it doesn't need to be attached to window
        val webView = WebView(context)
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                performPrint(context, webView, documentTitle)
            }
        }
        
        // Load the HTML string with a fake base URL and correct encoding
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    private fun performPrint(context: Context, webView: WebView, documentTitle: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        
        // PrintAdapter adapts the WebView's rendered content for printing
        val printAdapter = webView.createPrintDocumentAdapter(documentTitle)
        
        // Set up the default attributes (A4, etc)
        val builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        
        // Launch the print job (spawns the native Android Print/PDF modal)
        printManager.print(documentTitle, printAdapter, builder.build())
    }
}
