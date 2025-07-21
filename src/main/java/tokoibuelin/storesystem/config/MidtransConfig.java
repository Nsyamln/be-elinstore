package tokoibuelin.storesystem.config;

import com.midtrans.Midtrans;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {
    static {
        // Set global configuration for Midtrans
        Midtrans.serverKey = "SB-Mid-server-ithaY3pbvFd3X2yyCQkQ4e5j";
        Midtrans.clientKey = "SB-Mid-client-rIQqHY1znAFozWEl";
        Midtrans.isProduction = false; // Set to true if using production environment
    }
}
