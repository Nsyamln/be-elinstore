package tokoibuelin.storesystem.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.entity.Consignment;
import tokoibuelin.storesystem.entity.Order;
import tokoibuelin.storesystem.entity.OrderDetails;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.response.OrderDto;
import tokoibuelin.storesystem.repository.ConsignmentRepository;
import tokoibuelin.storesystem.repository.OrderRepository;
import tokoibuelin.storesystem.repository.ProductRepository;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.OnlineSaleReq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
@Service
public class OrderService extends AbstractService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ConsignmentRepository consignmentRepository;

    public String getLatestOrderId() {
        return orderRepository.getLatestOrderId();
    }

    public String generateNewOrderId() {
        return orderRepository.generateNewOrderId();
    }

    public OrderService(final OrderRepository orderRepository, final ProductRepository productRepository, final ConsignmentRepository consignmentRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.consignmentRepository = consignmentRepository;
    }

    public Response<Object> approveOrder(Authentication authentication, String id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Order> auctionOpt = orderRepository.findById(id);
            if (auctionOpt.isEmpty()) {
                return Response.create("07", "02", "Order tidak ditemukan", null);
            }

            Long updated = orderRepository.updateOrderStatus(authentication.id(), id, Order.Status.PROCESS);

            if (updated == 1L) {

                return Response.create("07", "00", "Sukses", updated);
            } else {
                return Response.create("07", "01", "Gagal memproses Order", null);
            }
        });
    }

    @Transactional
    public Response<Object> createOnlineSale(final Authentication authentication, final OnlineSaleReq req) {
//        return precondition(authentication, User.Role.PELANGGAN).orElseGet(() -> {
            if (req == null) {
                return Response.badRequest();
            }
            System.out.println("Cek req -> "+req);
            final Order order = new Order(
                    null,
                    null,
                    req.customerId(),
                    req.deliveryAddress(),
                    req.phone(),
                    Order.Status.PENDING,
                    req.customerId(),
                    OffsetDateTime.now(),
                    req.shippingCost(),
                    req.courier(),
                    req.shippingMethod(),
                    req.estimatedDelivery(),
                    req.paymentMethod(),
                    null

            );
            final String savedOrder = orderRepository.saveOrder(order);
            System.out.println("savedOrder : "+savedOrder);
            if (null == savedOrder  ) {
                return Response.create("05", "01", "Gagal menambahkan Order", null);
            }

            List<OrderDetails> orderDetails = req.orderDetail().stream().map(detailReq -> new OrderDetails(
                    null,
                    savedOrder,
                    detailReq.productId(),
                    detailReq.productName(),
                    detailReq.quantity(),
                    detailReq.price(),
                    detailReq.unit()
            )).collect(Collectors.toList());
            final Long savedOrderDetails = orderRepository.saveOrderDetails(orderDetails);

            List<Consignment> consignmentsToSave = new ArrayList<>();

            for (OrderDetails detail : orderDetails) {
                Optional<Product> productOpt = productRepository.findById(detail.productId());

                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    if (product.isConsignmentProduct()) {
                        System.out.println("Produk titipan terdeteksi: " + detail.productName() + " (ID: " + detail.productId() + ")");

                        Long consignmentTotalPrice = (product.purchasePrice() != null) ?
                                                    product.purchasePrice() * detail.quantity() :
                                                    0L; 
                        Consignment consignmentRecord = new Consignment(
                            null,
                            savedOrder, 
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
        //
            if (0L == savedOrderDetails ) {
                return Response.create("05", "01", "Gagal menambahkan Detail Penjualan", null);
            }


            return Response.create("05", "00", "Sukses", savedOrder);
//        });
    }

    public Response<Object> paidOrder(final String orderId, final String paymentMethod, final OffsetDateTime paymentTime){
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()){
            return Response.create("07", "01", "order  tidak ditemukan", null);
        }
        Order order = orderOpt.get();
            Order updatedOrder = new Order(
                    order.orderId(),
                    order.orderDate(),
                    order.customerId(),
                    order.deliveryAddress(),
                    order.phone(),
                    Order.Status.PAID,
                    order.createdBy(),
                    OffsetDateTime.now(),
                    order.shippingCost(),
                    order.courier(),
                    order.shippingMethod(),
                    order.estimatedDeliveryDate(),
                    paymentMethod,
                    paymentTime
                    );

            if (orderRepository.paid(updatedOrder)) {
                return Response.create("07", "00", "Status order berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate status order", null);
            }
    }

    public ByteArrayOutputStream generateShippingLabel(String orderId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Retrieve order data
            Optional<OrderDto> optionalOrderDto = orderRepository.findWithDetailByOrderId(orderId);
            System.out.println("cek ------> "+optionalOrderDto);

            
            if (optionalOrderDto.isEmpty()) {
                throw new RuntimeException("Order not found");
            }
            OrderDto orderDto = optionalOrderDto.get();

            // Initialize PDF
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PageSize pageSize = new PageSize(90 * 2.83465f, 120 * 2.83465f);
            pdf.setDefaultPageSize(pageSize);

            document.setMargins(5, 10, 10, 10);

            // Load fonts
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Add logo
            ImageData logoData = ImageDataFactory.create("D:\\College\\TA\\store-system\\src\\main\\resources\\static\\LOGO.png");
            Image logo = new Image(logoData).scaleToFit(100, 50);  // Adjust logo size
            document.add(logo);
            LineSeparator lineSeparator = new LineSeparator(new SolidLine());
            lineSeparator.setWidth(UnitValue.createPercentValue(100));
            document.add(lineSeparator);

            // Add invoice and order details
            String invoiceHeader = String.format("INV/%s/%s                                  %s-%s", orderDto.orderDate(), orderDto.orderId(),orderDto.courier(),orderDto.shippingMethod());
            document.add(new Paragraph(invoiceHeader)
                    .setFont(font).setFontSize(8).setBold().setTextAlignment(TextAlignment.JUSTIFIED));

            // Add courier and shipping cost information
//            document.add(new Paragraph(String.format("JNE                                      Ongkir:"))
//                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
//            document.add(new Paragraph(String.format("REG                                     Rp %,d", orderDto.shippingCost()))
//                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));

            document.add(new Paragraph(String.format("Dikirim Dari : "))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph(String.format("Toko Oleh Oleh Ibu Elin"))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT).setBold());
            document.add(new Paragraph(String.format("Jl. Budiasih, Budiasih, Kec. Sindangkasih, Kabupaten Ciamis, Jawa Barat 46268"))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));

            document.add(new Paragraph(String.format("Kepada: "))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph(String.format("%s ", orderDto.customerName()))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT).setBold());
            document.add(new Paragraph(String.format("%s ", orderDto.phone()))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
            document.add(new Paragraph(String.format("%s ", orderDto.deliveryAddress()))
                    .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));

            // Add separator line
            document.add(lineSeparator);

            // // Add product details
            // document.add(new Paragraph("Produk                                                                            Jumlah")
            //         .setFont(font).setFontSize(8).setBold().setTextAlignment(TextAlignment.LEFT));
            // document.add(new Paragraph(String.format("%s                                                                  %d", orderDto.orderDetails().productName(), orderDto.orderDetails().quantity()))
            //         .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));

            document.add(new Paragraph("Produk                                                 Jumlah")
                .setFont(font).setFontSize(8).setBold().setTextAlignment(TextAlignment.LEFT));

            // Loop melalui setiap OrderDetails di dalam list
            for (OrderDetails detail : orderDto.orderDetails()) {
                document.add(new Paragraph(String.format("%s                                                 %d", detail.productName(), detail.quantity()))
                        .setFont(font).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
            }

            // Close the document
            document.close();
            return outputStream;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




}
