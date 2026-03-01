package com.gradle.demo.demo.gpt.service;

import com.gradle.demo.demo.gpt.model.FieldRequest;
import com.gradle.demo.demo.gpt.model.FieldType;
import com.gradle.demo.demo.gpt.model.SectionRequest;
import com.gradle.demo.demo.gpt.handler.HeaderEventHandler;
import com.gradle.demo.demo.gpt.handler.FooterEventHandler;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.*;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
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
        document.setMargins(80, 40, 50, 40);

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        form.setNeedAppearances(true);   // Important for Chrome rendering

        document.add(new Paragraph(
                "List of information required and supporting documents for KYC journey")
                .setFontSize(11)
                .setMarginBottom(20));

        AtomicInteger fieldCounter = new AtomicInteger(1);
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

                addQuestionRow(document, pdfDoc, form,
                        field, label, "field_" + fieldCounter.getAndIncrement());
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addQuestionRow(Document document,
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
                .setMinHeight(22);

        right.setNextRenderer(new FieldCellRenderer(right, pdfDoc, form, field, fieldName));

        table.addCell(left);
        table.addCell(right);

        document.add(table);
    }

    // ===========================
    // Custom Cell Renderer
    // ===========================

    private static class FieldCellRenderer extends CellRenderer {

        private final PdfDocument pdfDoc;
        private final PdfAcroForm form;
        private final FieldRequest field;
        private final String fieldName;

        public FieldCellRenderer(Cell modelElement,
                                 PdfDocument pdfDoc,
                                 PdfAcroForm form,
                                 FieldRequest field,
                                 String fieldName) {
            super(modelElement);
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

                    PdfTextFormField textField =
                            new TextFormFieldBuilder(pdfDoc, fieldName)
                                    .setWidgetRectangle(rect)
                                    .createText();

                    applyBorder(textField);
                    form.addField(textField);
                    break;

                case SELECTION:

                    PdfChoiceFormField combo =
                            new ChoiceFormFieldBuilder(pdfDoc, fieldName)
                                    .setWidgetRectangle(rect)
                                    .setOptions(
                                            field.getOptions() != null
                                                    ? field.getOptions().toArray(new String[0])
                                                    : new String[]{"Option 1"})
                                    .createComboBox();

                    applyBorder(combo);
                    form.addField(combo);
                    break;

                case CHECKBOX:

                    Rectangle small =
                            new Rectangle(rect.getX(), rect.getY(), 15, 15);

                    PdfButtonFormField checkbox =
                            new CheckBoxFormFieldBuilder(pdfDoc, fieldName)
                                    .setWidgetRectangle(small)
                                    .createCheckBox();

                    applyBorder(checkbox);
                    form.addField(checkbox);
                    break;
            }
        }

        private void applyBorder(PdfFormField field) {

            PdfWidgetAnnotation widget = field.getWidgets().get(0);

            // Border Style dictionary
            PdfDictionary bs = new PdfDictionary();
            bs.put(PdfName.W, new PdfNumber(1)); // width
            bs.put(PdfName.S, PdfName.S);        // solid
            widget.getPdfObject().put(PdfName.BS, bs);

            // Border color (black)
            PdfArray color = new PdfArray();
            color.add(new PdfNumber(0));
            color.add(new PdfNumber(0));
            color.add(new PdfNumber(0));
            widget.getPdfObject().put(PdfName.C, color);

            // Legacy border array [0 0 1]
            PdfArray border = new PdfArray();
            border.add(new PdfNumber(0));
            border.add(new PdfNumber(0));
            border.add(new PdfNumber(1));
            widget.getPdfObject().put(PdfName.Border, border);
        }
    }
}