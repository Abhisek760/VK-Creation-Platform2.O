package com.vkcreation.platform.common;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PdfDocumentService {
    public byte[] fromHtml(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new PdfRendererBuilder().withHtmlContent(html, "https://vkcreation.invalid/").toStream(out).run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate PDF document.", e);
        }
    }
}