package com.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class OCRApp extends JFrame {
    private JButton selectImageButton;
    private JTextPane resultArea;
    private JComboBox<String> fontTypes;

    public OCRApp() {
        setTitle("TwoOptics");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        selectImageButton = new JButton("Select Image");
        selectImageButton.setBounds(210, 140, 130, 30);
        add(selectImageButton);

        resultArea = new JTextPane();
        resultArea.setBounds(15, 180, 550, 450);
        add(resultArea);
        resultArea.setEditable(false);

        String[] fonts = {"Arial", "Helvetica", "Times New Roman", "Courier New", "Century", "Serif"};
        fontTypes = new JComboBox(fonts);
        fontTypes.setBounds(30,70,130,30);
        add(fontTypes);

        selectImageButton.addActionListener(e -> chooseImage());
        fontTypes.addActionListener(e -> changeFont(fontTypes.getSelectedItem().toString()));


        setVisible(true);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String name = file.getName().toLowerCase();
            resultArea.setEditable(false);
            String result;

            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                result = performOCR(file);
                resultArea.setEditable(true);
            } else if (name.endsWith(".pdf")) {
                result = performOCRFromPDF(file);
                resultArea.setEditable(true);
            } else {
                result = "Unsupported file type.";
            }

            resultArea.setText(result);
        }
    }

    private void changeFont(String fontName) {
        int start = resultArea.getSelectionStart();
        int end = resultArea.getSelectionEnd();

        if (start == end) {
            // No text selected
            return;
        }

        javax.swing.text.StyledDocument doc = resultArea.getStyledDocument();
        javax.swing.text.Style style = resultArea.addStyle("NewStyle", null);
        javax.swing.text.StyleConstants.setFontFamily(style, fontName);

        doc.setCharacterAttributes(start, end - start, style, false);
    }



    private String performOCR(File imageFile) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error performing OCR";
        }
    }

    private String performOCRFromPDF(File pdfFile) {
        StringBuilder resultText = new StringBuilder();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/Program Files/Tesseract-OCR");
            tesseract.setLanguage("eng");

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);
                File tempImage = File.createTempFile("page_" + page, ".png");
                ImageIO.write(image, "png", tempImage);
                resultText.append("Page ").append(page + 1).append(":\n");
                resultText.append(tesseract.doOCR(tempImage)).append("\n\n");
                tempImage.delete(); // Clean up
            }
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "Error processing PDF: " + e.getMessage();
        }
        return resultText.toString();
    }

}
