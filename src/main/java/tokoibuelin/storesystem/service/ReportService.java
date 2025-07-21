package tokoibuelin.storesystem.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.*;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.entity.SaleDetails;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.CashFlowDto;
import tokoibuelin.storesystem.model.response.SaleDto;
import tokoibuelin.storesystem.model.response.SalesDto;
import tokoibuelin.storesystem.repository.ConsignmentRepository;
import tokoibuelin.storesystem.repository.OrderRepository;
import tokoibuelin.storesystem.repository.SaleRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService extends AbstractService{

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConsignmentRepository consignmentRepository;


    public Response<Object> getSaldoAwal(final Authentication authentication, final String startDate) {
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                Long count = saleRepository.sumSalesBeforeDay(startDate)+orderRepository.sumOrdersBeforeDay(startDate);
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }


    public ByteArrayOutputStream createSaleReportByMethod(final String paymentMethod, String startDate, String endDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            List<SalesDto> sales = saleRepository.getSalesReportByPaymentMethod(paymentMethod,startDate, endDate);

            // Inisialisasi PDF dengan orientasi landscape
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Load font
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            Paragraph header = new Paragraph("Laporan Transaksi Penjualan")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(header);

            Paragraph storeInfo = new Paragraph("Toko Oleh Oleh Ibu Elin")
                    .setFont(regularFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(storeInfo);

            // Format periode
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedStartDate = startLocalDate.format(headerFormatter);
            String formattedEndDate = endLocalDate.format(headerFormatter);

            Paragraph dateRange = new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(dateRange);

            // Add space between header and table
            document.add(new Paragraph("\n"));

            // Create Table with landscape orientation
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2,  2, 4, 1, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Add Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("Tanggal").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nomor Transaksi").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Kode Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Harga Satuan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Harga").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Metode Pembayaran").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
                    
            for (SalesDto sale : sales) {
                // Data untuk setiap detail penjualan
                for (SaleDetails detail : sale.saleDetails()) {
                    BigDecimal price = BigDecimal.valueOf(detail.price().doubleValue());
                    BigDecimal quantity = BigDecimal.valueOf(detail.quantity());
                    BigDecimal totalPrice = price.multiply(quantity);
                    
                    LocalDateTime saleDateTime = LocalDateTime.parse(sale.saleDate(), formatter); // <--- Parse here

            // Now format the LocalDateTime object
            String formattedDate = formatter.format(saleDateTime); // This will now work!

                    table.addCell(new Cell().add(new Paragraph(formattedDate).setFont(smallFont).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(sale.saleId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(detail.productName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Nama Produk
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.quantity())).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Jumlah
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", price)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Harga Satuan
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", totalPrice)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Total Harga
                    table.addCell(new Cell().add(new Paragraph(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "").setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Metode Pembayaran
                }
            }

            document.add(table);
            document.close();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ByteArrayOutputStream createSaleReportByMethodAll(final String startDate, final String endDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            List<SalesDto> sales = saleRepository.getSalesReportByPaymentMethodAll(startDate, endDate);

            System.out.println("GET ISI LAPORAN ->>>>>"+sales);

            // Inisialisasi PDF dengan orientasi landscape
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Load font
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            Paragraph header = new Paragraph("Laporan Transaksi Penjualan")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(header);

            Paragraph storeInfo = new Paragraph("Toko Oleh Oleh Ibu Elin")
                    .setFont(regularFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(storeInfo);

            // Format periode
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedStartDate = startLocalDate.format(headerFormatter);
            String formattedEndDate = endLocalDate.format(headerFormatter);

            Paragraph dateRange = new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(dateRange);

            // Add space between header and table
            document.add(new Paragraph("\n"));

            // Create Table with landscape orientation
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 4, 1, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Add Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("Tanggal").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nomor Transaksi").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Kode Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Harga Satuan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Harga").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Metode Pembayaran").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss").withZone(ZoneId.systemDefault());
            DateTimeFormatter inputDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); 

            for (SalesDto sale : sales) {
                if(sale.saleDetails() !=null){
                        for (SaleDetails detail : sale.saleDetails()) {
                        BigDecimal price = BigDecimal.valueOf(detail.price().doubleValue());
                        BigDecimal quantity = BigDecimal.valueOf(detail.quantity());
                        BigDecimal totalPrice = price.multiply(quantity);
                        LocalDateTime saleDateTime = LocalDateTime.parse(sale.saleDate(), inputDateFormatter); // <--- Parse here


                        String formattedDate = formatter.format(saleDateTime);

                        table.addCell(new Cell().add(new Paragraph(formattedDate).setFont(smallFont).setFontSize(10)));
                        table.addCell(new Cell().add(new Paragraph(sale.saleId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                        table.addCell(new Cell().add(new Paragraph(detail.productId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Kode Produk
                        table.addCell(new Cell().add(new Paragraph(detail.productName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Nama Produk
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.quantity())).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Jumlah
                        table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", price)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Harga Satuan
                        table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", totalPrice)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Total Harga
                        table.addCell(new Cell().add(new Paragraph(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "").setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Metode Pembayaran
                        }

                }else{
                        System.out.println("Tidak ada detail penjualan untuk saleId: " + sale.saleId());
                }
               
            }

            document.add(table);
            document.close();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response getCashFlow(final String startDate, final String endDate) {

        Long totalBRIFromSales = saleRepository.sumByPaymentMethod(startDate, endDate, "bri");

        // Panggil metode dari orderRepository
        Long totalBRIFromOrders = orderRepository.sumByPaymentMethod(startDate, endDate, "bri");

        // Jumlahkan kedua hasil tersebut
        Long  grandTotalBRI= totalBRIFromSales + totalBRIFromOrders;
        Long totalFromSales = saleRepository.sumSales(startDate, endDate);
        Long totalFromOrders = orderRepository.sumSales(startDate, endDate);
        System.out.println("CEK LAGI "+totalFromOrders);
        Long grandTotalPendapatan = totalFromOrders+totalFromSales;
        
        try {
            // Mengambil data dari repository
            Long totalPenjualan = grandTotalPendapatan;
            Long totalBRI = grandTotalBRI;
            Long totalCash = saleRepository.sumByPaymentMethod(startDate, endDate, "cash");
            Long totalShopeepay = saleRepository.sumByPaymentMethod(startDate, endDate, "SHOPEEPAY");
            Long totalSharing = consignmentRepository.sumConsignment(startDate, endDate);
            Long kasMasuk = totalPenjualan;
            Long kasKeluar = totalSharing;
            Long kasBersih = kasMasuk - kasKeluar;

            // Membuat DTO dengan data yang diambil
            CashFlowDto cashFlowDto = new CashFlowDto(
                    totalPenjualan,
                    totalBRI,
                    totalCash,
                    totalShopeepay,
                    totalSharing,
                    kasMasuk,
                    kasKeluar,
                    kasBersih
            );
            return Response.create("09", "00", "Sukses", cashFlowDto);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.create("01", "00", "Database error", null);
        }
    }


    public ByteArrayOutputStream generateCashFlow(String startDate, String endDate) throws DocumentException, IOException {
        Long totalBRIFromSales = saleRepository.sumByPaymentMethod(startDate, endDate, "bri");
        Long totalBRIFromOrders = orderRepository.sumByPaymentMethod(startDate, endDate, "bri");
        Long  grandTotalBRI= totalBRIFromSales + totalBRIFromOrders;

        Long totalFromSales = saleRepository.sumSales(startDate, endDate);
        Long totalFromOrders = orderRepository.sumSales(startDate, endDate);
        System.out.println("CEK LAGI "+totalFromOrders);
        Long grandTotalPendapatan = totalFromOrders+totalFromSales;

        Long totalPenjualan = grandTotalPendapatan;
        Long totalBRI = grandTotalBRI;
        Long totalCash = saleRepository.sumByPaymentMethod(startDate, endDate, "CASH");
        Long totalShopeepay = saleRepository.sumByPaymentMethod(startDate, endDate, "shopeepay");
        Long totalPenerimaan = totalPenjualan;
        Long totalSharing = consignmentRepository.sumConsignment(startDate, endDate);
        Long kasMasuk = totalPenjualan;
        Long kasKeluar = totalSharing;
        Long kasBersih = kasMasuk - kasKeluar;

        CashFlowDto cashFlowDto = new CashFlowDto(
                totalPenjualan,
                totalBRI,
                totalCash,
                totalShopeepay,
                totalSharing,
                kasMasuk,
                kasKeluar,
                kasBersih
        );

        // Generate PDF
        return generateCashFlowPdf(cashFlowDto,startDate,endDate);
    }


    public ByteArrayOutputStream generateCashFlowPdf(CashFlowDto cashFlowDto, String startDate, String endDate) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Initialize PdfWriter and PdfDocument
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDoc);

        // Font setup
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Title
        document.add(new Paragraph("Laporan Arus Kas")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold());

        // Add store information
        document.add(new Paragraph("Toko Oleh Oleh Ibu Elin")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));

        // Format tanggal
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        String formattedStartDate = startLocalDate.format(formatter);
        String formattedEndDate = endLocalDate.format(formatter);

        // Add report details
        document.add(new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // Add line separator
        LineSeparator lineSeparator = new LineSeparator(new SolidLine());
        lineSeparator.setWidth(UnitValue.createPercentValue(100));
        document.add(lineSeparator);

        // Add section header
        document.add(new Paragraph("Arus Kas dari Aktivitas Operasional")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.LEFT));
        document.add(new Paragraph(" "));

        // Add table for "Pendapatan dari Penjualan"
        document.add(new Paragraph("Pendapatan dari Penjualan")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table1 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table1.addCell(createCell("Total Penjualan:", TextAlignment.LEFT));
        table1.addCell(createCell(String.format("Rp %,d", cashFlowDto.totalPenjualan()), TextAlignment.RIGHT));
        document.add(table1);

        // Add table for "Metode Pembayaran"
        document.add(new Paragraph("Metode Pembayaran")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table2.addCell(createCell("Bank Transfer:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.bankBRI()), TextAlignment.RIGHT));
        table2.addCell(createCell("Tunai:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.cash()), TextAlignment.RIGHT));
        table2.addCell(createCell("ShopeePay:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.shopeepay()), TextAlignment.RIGHT));
        document.add(table2);

        // Add table for "Penerimaan dari Pelanggan"
        // document.add(new Paragraph("Penerimaan dari Pelanggan")
        //         .setFont(boldFont)
        //         .setFontSize(12)
        //         .setTextAlignment(TextAlignment.LEFT));
        // Table table3 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
        //         .setWidth(UnitValue.createPercentValue(100));
        // table3.addCell(createCell("Jumlah Penerimaan:", TextAlignment.LEFT));
        // table3.addCell(createCell(String.format("Rp %,d", cashFlowDto.jumlahPenerimaan()), TextAlignment.RIGHT));
        // document.add(table3);

        // Add table for "Pembayaran kepada Pemasok"
        document.add(new Paragraph("Pembayaran kepada Pemasok")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table4 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table4.addCell(createCell("Total Pembayaran:", TextAlignment.LEFT));
        table4.addCell(createCell(String.format("Rp %,d", cashFlowDto.bayarPemasok()), TextAlignment.RIGHT));
        document.add(table4);

        // Add table for "Arus Kas Bersih dari Aktivitas Operasional"
        document.add(new Paragraph("Arus Kas Bersih dari Aktivitas Operasional")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table5 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table5.addCell(createCell("Total Kas Masuk:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasMasuk()), TextAlignment.RIGHT));
        table5.addCell(createCell("Total Kas Keluar:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasKeluar()), TextAlignment.RIGHT));
        table5.addCell(createCell("Arus Kas Bersih:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasBersih()), TextAlignment.RIGHT));
        document.add(table5);

        // Close document
        document.close();

        return baos;
    }

    private Cell createCell(String content, TextAlignment alignment) {
        return new Cell().add(new Paragraph(content).setTextAlignment(alignment));
    }



}
