package tokoibuelin.storesystem.service;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tokoibuelin.storesystem.entity.Consignment;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.entity.Sale;
import tokoibuelin.storesystem.entity.SaleDetails;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.OfflineSaleReq;
import tokoibuelin.storesystem.model.response.SalesDto;
import tokoibuelin.storesystem.repository.ConsignmentRepository;
import tokoibuelin.storesystem.repository.ProductRepository;
import tokoibuelin.storesystem.repository.SaleRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleService extends AbstractService{
    private final SaleRepository saleRepository;
    private final ConsignmentRepository consignmentRepository;
    private final ProductRepository productRepository;
    @Autowired AddressService addressService;

    public SaleService(final SaleRepository saleRepository, final ConsignmentRepository consignmentRepository, final  ProductRepository productRepository){
        this.saleRepository = saleRepository;
        this.consignmentRepository = consignmentRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Response<Object> createOfflineSale(final Authentication authentication, final OfflineSaleReq req) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            if (req == null) {
                return Response.badRequest();
            }
            final Sale sale = new Sale(
                    null,
                    OffsetDateTime.now(),
                    req.totalPrice(),
                    req.amountPaid(),
                    req.paymentMethod(),
                    authentication.id()  
            );
            final String savedSale = saleRepository.saveSale(sale);
            if (null == savedSale ) {
                return Response.create("06", "01", "Gagal menambahkan Penjualan", null);
            }
            List<SaleDetails> saleDetails = req.saleDetails().stream().map(detailReq -> new SaleDetails(
                    null,
                    savedSale,
                    detailReq.productId(),
                    detailReq.productName(),
                    detailReq.quantity(),
                    detailReq.price()
            )).collect(Collectors.toList());
            final Long savedSaleDetails = saleRepository.saveSaleDetails(saleDetails);
            List<Consignment> consignmentsToSave = new ArrayList<>();

            for (SaleDetails detail : saleDetails) {
                Optional<Product> productOpt = productRepository.findById(detail.productId());

                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    if (product.isConsignmentProduct()) {
                        Long consignmentTotalPrice = (product.purchasePrice() != null) ?
                                                    product.purchasePrice() * detail.quantity() :
                                                    0L; 
                        Consignment consignmentRecord = new Consignment(
                            null, 
                            savedSale,
                            detail.productId(),
                            product.supplierId(), 
                            detail.quantity(),
                            consignmentTotalPrice, 
                            "UNPAID", 
                            null 
                        );
                            consignmentsToSave.add(consignmentRecord);
                    } else {
                        System.out.println("Produk reguler: " + detail.productName() + " (ID: " + detail.productId() + ")");
                    }
                } else {
                    System.out.println("Peringatan: Produk dengan ID " + detail.productId() + " tidak ditemukan di repository.");
                }
            }

            if (!consignmentsToSave.isEmpty()) {
                System.out.println("Ditemukan " + consignmentsToSave.size() + " produk titipan yang perlu dicatat konsinyasinya.");
                consignmentRepository.saveConsignment(consignmentsToSave);
            }
            if (0L == savedSaleDetails ) {
                return Response.create("05", "01", "Gagal menambahkan Detail Penjualan", null);
            }
            return Response.create("05", "00", "Sukses", savedSale);
        });
    }

    public ByteArrayOutputStream generateReceipt(String saleId) throws IOException {
        // Fetch sales and sale details
        List<SalesDto> saleDtos = saleRepository.getSalesById(saleId);

        // Ensure there's at least one sale entry
        if (saleDtos.isEmpty()) {
            throw new IllegalArgumentException("No sale found with ID: " + saleId);
        }

        // Define date formatter for local time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        // Create a ByteArrayOutputStream to hold the PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Initialize PDF writer and document with custom page size
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PageSize pageSize = new PageSize(75 * 2.83465f, 100 * 2.83465f); // 65mm x 100mm
        pdfDoc.setDefaultPageSize(pageSize);
        Document document = new Document(pdfDoc);

        // Load fonts
        PdfFont font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

        // Add store information
        document.add(new Paragraph("Toko Oleh Oleh Ibu Elin").setFont(boldFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setFontSize(12));
        document.add(new Paragraph("Jl Budiasih No 25, Sindangkasih - Ciamis").setFont(font).setFontSize(8).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // Add horizontal line
        LineSeparator lineSeparator = new LineSeparator(new SolidLine());
        lineSeparator.setWidth(UnitValue.createPercentValue(100));
        document.add(lineSeparator);

        // Iterate through each SaleDto
        for (SalesDto saleDto : saleDtos) {
            // String formattedDate = formatter.format(saleDto.saleDate());

            // Add receipt number and date
            document.add(new Paragraph(String.format("ID : %s             %s", saleDto.saleId(), saleDto.saleDate()))
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.JUSTIFIED));

            // Add horizontal line
            document.add(lineSeparator);

            // Add sale details
            for (SaleDetails detail : saleDto.getSaleDetails()) {
                document.add(new Paragraph(String.format("  %s       %d        Rp %s",
                         detail.productName(), detail.quantity(), detail.price().toString()))
                        .setFont(font).setFontSize(8)
                        .setTextAlignment(TextAlignment.JUSTIFIED));
            }

            // Calculate totals
            BigDecimal total = saleDto.totalPrice();
            BigDecimal paid = saleDto.amountPaid();
            BigDecimal change = paid.subtract(total);

            // Mengonversi BigDecimal ke integer
            int totalAmount = total.intValue();
            int paidAmount = paid.intValue();
            int changeAmount = change.intValue();


            // Add total price, amount paid, and change
            document.add(lineSeparator);
            document.add(new Paragraph(String.format("Total                                    Rp %d", totalAmount))
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.JUSTIFIED));
            document.add(new Paragraph(String.format("Bayar                                   Rp %d", paidAmount))
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.JUSTIFIED));
            document.add(new Paragraph(String.format("Kembalian                           Rp %d", changeAmount))
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.JUSTIFIED));

            // Add footer
            document.add(lineSeparator);
            document.add(new Paragraph("TERIMAKASIH").setFont(boldFont).setFontSize(8).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Atas kunjungan anda!").setFontSize(8).setFont(font).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" ").setFont(font).setTextAlignment(TextAlignment.CENTER));
        }

        // Close document
        document.close();
        return baos;
    }
}
