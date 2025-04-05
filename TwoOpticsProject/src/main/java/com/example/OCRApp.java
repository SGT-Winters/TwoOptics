package com.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class OCRApp extends JFrame {
    private JButton selectImageButton;
    private JTextArea resultArea;

    public OCRApp() {
        setTitle("TwoOptics");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        selectImageButton = new JButton("Select Image");
        selectImageButton.setBounds(210, 20, 150, 30);
        add(selectImageButton);

        resultArea = new JTextArea();
        resultArea.setBounds(50, 70, 490, 550);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        add(resultArea);

        selectImageButton.addActionListener(e -> chooseImage());

        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String name = file.getName().toLowerCase();
            String result;

            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                result = performOCR(file);
            } else if (name.endsWith(".pdf")) {
                result = performOCRFromPDF(file);
            } else {
                result = "Unsupported file type.";
            }

            resultArea.setText(result);
        }
    }


    private String performOCR(File imageFile) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // Update this path
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
