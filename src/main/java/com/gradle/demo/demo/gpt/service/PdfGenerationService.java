package com.gradle.demo.demo.gpt.service;

import com.gradle.demo.demo.gpt.model.FieldRequest;
import com.gradle.demo.demo.gpt.model.FieldType;
import com.gradle.demo.demo.gpt.model.SectionRequest;
import com.gradle.demo.demo.gpt.handler.HeaderEventHandler;
import com.gradle.demo.demo.gpt.handler.FooterEventHandler;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.borders.Border;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PdfGenerationService {

    private static final float QUESTION_WIDTH = 60f;
    private static final float FIELD_WIDTH = 40f;

    public byte[] generatePdf(List<SectionRequest> sections) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);

        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderEventHandler());
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler());

        Document document = new Document(pdfDoc);
        document.setMargins(100, 40, 50, 40);

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        form.setGenerateAppearance(true);   // CRITICAL for Chrome rendering

        document.add(new Paragraph(
                "List of information required and supporting documents for KYC journey")
                .setFontSize(11)
                .setMarginBottom(20));

        AtomicInteger counter = new AtomicInteger(1);
        int sectionIndex = 1;

        for (SectionRequest section : sections) {

            document.add(new Paragraph(sectionIndex++ + ". " + section.getTag())
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(15)
                    .setMarginBottom(10));

            int questionIndex = 0;

            for (FieldRequest field : section.getFields()) {

                char letter = (char) ('a' + questionIndex++);
                String label = letter + ". " + field.getFieldName();

                if (field.isMandatory()) {
                    label += " *";
                }

                addRow(document, pdfDoc, form,
                        field, label, "field_" + counter.getAndIncrement());
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addRow(Document document,
                        PdfDocument pdfDoc,
                        PdfAcroForm form,
                        FieldRequest field,
                        String questionText,
                        String fieldName) {

        Table table = new Table(UnitValue.createPercentArray(
                new float[]{QUESTION_WIDTH, FIELD_WIDTH}))
                .useAllAvailableWidth()
                .setMarginBottom(8);

        Cell left = new Cell()
                .add(new Paragraph(questionText).setFontSize(10))
                .setBorder(Border.NO_BORDER);

        Cell right = new Cell()
                .setBorder(Border.NO_BORDER)
                .setMinHeight(26);

        right.setNextRenderer(
                new FieldRenderer(right, pdfDoc, form, field, fieldName));

        table.addCell(left);
        table.addCell(right);

        document.add(table);
    }

    private static class FieldRenderer extends CellRenderer {

        private final PdfDocument pdfDoc;
        private final PdfAcroForm form;
        private final FieldRequest field;
        private final String fieldName;

        public FieldRenderer(Cell cell,
                             PdfDocument pdfDoc,
                             PdfAcroForm form,
                             FieldRequest field,
                             String fieldName) {
            super(cell);
            this.pdfDoc = pdfDoc;
            this.form = form;
            this.field = field;
            this.fieldName = fieldName;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);

            Rectangle rect = getOccupiedAreaBBox();

            switch (field.getFieldType()) {

                case TEXT:
                case DATE:

                    PdfTextFormField text =
                            PdfTextFormField.createText(pdfDoc, rect, fieldName, "");

                    text.setBorderColor(ColorConstants.BLACK);
                    text.setBorderWidth(2);
                    text.setBackgroundColor(ColorConstants.WHITE);

                    form.addField(text);
                    text.regenerateField();   // IMPORTANT

                    break;

                case SELECTION:

                    PdfChoiceFormField combo =
                            PdfChoiceFormField.createComboBox(
                                    pdfDoc,
                                    rect,
                                    fieldName,
                                    "",
                                    field.getOptions() != null
                                            ? field.getOptions().toArray(new String[0])
                                            : new String[]{"Option 1"});

                    combo.setBorderColor(ColorConstants.BLACK);
                    combo.setBorderWidth(2);
                    combo.setBackgroundColor(ColorConstants.WHITE);

                    form.addField(combo);
                    combo.regenerateField();   // IMPORTANT

                    break;

                case CHECKBOX:  // Yes / No Radio Pair

                    float size = 16f;
                    float gap = 70f;

                    PdfButtonFormField group =
                            PdfButtonFormField.createRadioGroup(pdfDoc, fieldName, "Off");

                    Rectangle yesRect =
                            new Rectangle(rect.getX(), rect.getY(), size, size);

                    PdfFormField yes =
                            PdfFormField.createRadioButton(pdfDoc, yesRect, group, "Yes");

                    yes.setBorderColor(ColorConstants.BLACK);
                    yes.setBorderWidth(2);
                    yes.setBackgroundColor(ColorConstants.WHITE);

                    Rectangle noRect =
                            new Rectangle(rect.getX() + gap, rect.getY(), size, size);

                    PdfFormField no =
                            PdfFormField.createRadioButton(pdfDoc, noRect, group, "No");

                    no.setBorderColor(ColorConstants.BLACK);
                    no.setBorderWidth(2);
                    no.setBackgroundColor(ColorConstants.WHITE);

                    form.addField(group);
                    group.regenerateField();   // CRITICAL

                    break;
            }
        }
    }
}