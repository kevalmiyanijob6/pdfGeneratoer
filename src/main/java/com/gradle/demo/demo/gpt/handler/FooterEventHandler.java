package com.gradle.demo.demo.gpt.handler;


import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

public class FooterEventHandler implements IEventHandler {

    private static final float RIGHT_MARGIN = 40f;
    private static final float BOTTOM_MARGIN = 20f;

    @Override
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        int pageNumber = pdf.getPageNumber(page);

        Rectangle pageSize = page.getPageSize();

        Canvas canvas = new Canvas(page, pageSize);

        Paragraph footer = new Paragraph("Page " + pageNumber)
                .setFontSize(9);

        canvas.showTextAligned(
                footer,
                pageSize.getWidth() - RIGHT_MARGIN,
                BOTTOM_MARGIN,
                TextAlignment.RIGHT
        );

        canvas.close();
    }
}