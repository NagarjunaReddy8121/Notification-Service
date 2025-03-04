package in.arjun.service;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import in.arjun.request.NotificationRequest;
import in.arjun.request.OrderItemsRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFGenarationService {

    public ByteArrayInputStream generateOrderInvoicePdf(NotificationRequest notificationRequest) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Initialize PDF writer
            PdfWriter writer = new PdfWriter(out);
            // Initialize PDF document
            PdfDocument pdfDoc = new PdfDocument(writer);
            // Add event handler for header and footer
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterHandler());
            // Initialize document
            Document document = new Document(pdfDoc, PageSize.A4);

            // Add title
            document.add(new Paragraph("Order Invoice")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20));

            // Add message
            document.add(new Paragraph("Your order is delivering today, please take the order.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold());

            // Add Customer Info
            document.add(new Paragraph("Order Tracking ID: " + (notificationRequest.getOrderData().getOrderTrackingNumber() != null
                    ? notificationRequest.getOrderData().getOrderTrackingNumber() : "N/A"))
                    .setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph("Total Amount: $" + String.format("%.2f", notificationRequest.getOrderData().getTotalPrice()))
                    .setTextAlignment(TextAlignment.LEFT));

            // Add some space
            document.add(new Paragraph("\n"));

            // Add table with product details
            Table table = new Table(new float[]{1, 4, 2, 2});
            table.setWidth(100);

            table.addCell(new Cell().add(new Paragraph("No")));
            table.addCell(new Cell().add(new Paragraph("Product Name")));
            table.addCell(new Cell().add(new Paragraph("Quantity")));
            table.addCell(new Cell().add(new Paragraph("Price")));

            // Sample product details from the order
            List<OrderItemsRequest> orderItems = notificationRequest.getOrderItems();
            if (orderItems != null) {
                int index = 1;
                for (OrderItemsRequest orderItem : orderItems) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(index++))));
                    table.addCell(new Cell().add(new Paragraph(orderItem.getName() != null ? orderItem.getName() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(orderItem.getQuantity()))));
                    table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", orderItem.getPrice()))));
                }
            }

            document.add(table);

            // Add total amount
            document.add(new Paragraph("\nTotal Amount: $" + String.format("%.2f", notificationRequest.getOrderData().getTotalPrice()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold());

            // Close document
            document.close();
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Custom header and footer event handler
    private class HeaderFooterHandler implements IEventHandler {

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            PdfCanvas canvas = new PdfCanvas(page);

            int pageNumber = pdfDoc.getPageNumber(page);

            // Add header
            try {
                canvas.beginText()
                        .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 12)
                        .moveText(36, page.getPageSize().getTop() - 30) // Position for header
                        .showText("Invoice - Page " + pageNumber)
                        .endText();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Add footer
            canvas.beginText()
                    .moveText(36, 30) // Position for footer
                    .showText("Thank you for your order - Page " + pageNumber)
                    .endText();
        }
    }
}
