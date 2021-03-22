package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class StripWrapper {

    private final PDFTextStripper stripper;
    private final PDDocument document;

    public StripWrapper(PDFTextStripper s, PDDocument d) {
        this.stripper = s;
        this.document = d;
    }

    public PDFTextStripper getStripper() {
        return  this.stripper;
    }

    public PDDocument getDocument() {
        return this.document;
    }

    public String getText() throws IOException {
        return this.stripper.getText(this.document);
    }
}
