package com.example.studybuddy.utils;

import android.content.Context;
import android.net.Uri;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;

public class PdfExtractor {

    public static void init(Context context) {
        PDFBoxResourceLoader.init(context);
    }

    public static String extractText(Context context, Uri pdfUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}