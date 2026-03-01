package com.gradle.demo.demo.gpt.handler;

import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

public class HeaderEventHandler implements IEventHandler {

    @Override
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        Rectangle pageSize = page.getPageSize();

        Canvas canvas = new Canvas(page, pageSize);

        Paragraph header = new Paragraph("Citi KYC - Corporation Outreach")
                .setBold()
                .setFontSize(14);

        canvas.showTextAligned(header,
                40,
                pageSize.getTop() - 30,
                TextAlignment.LEFT);

        canvas.close();
    }
}
