package com.gradle.demo.demo.gpt.handler;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class HeaderEventHandler implements IEventHandler {

    private Image logo;

    public HeaderEventHandler() {
        try {
            InputStream is =
                    new ClassPathResource("images/citi.png").getInputStream();

            byte[] bytes = is.readAllBytes();
            ImageData imageData = ImageDataFactory.create(bytes);

            logo = new Image(imageData);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        Rectangle pageSize = page.getPageSize();

        Canvas canvas = new Canvas(page, pageSize);

        if (logo != null) {

            // Fit full width minus margins
            float availableWidth = pageSize.getWidth() - 80; // 40 left + 40 right
            logo.scaleToFit(availableWidth, 60);

            logo.setFixedPosition(
                    40,
                    pageSize.getTop() - 70
            );

            canvas.add(logo);
        }

        canvas.close();
    }
}