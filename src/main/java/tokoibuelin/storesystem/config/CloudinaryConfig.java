package tokoibuelin.storesystem.config;
import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

  

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary("cloudinary://761262871289896:Koy7dTFACGUxigiOk0lZUlRJgsc@dulwovkvt");
    }
}