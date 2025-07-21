package tokoibuelin.storesystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.util.JwtUtils;
import tokoibuelin.storesystem.util.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;


public class AuthenticationFilter extends OncePerRequestFilter {

    private final byte[] jwtKey;

    public AuthenticationFilter(byte[] jwtKey) {
        this.jwtKey = jwtKey;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                final String token = authorization.substring(7);
                JwtUtils.Jwt jwt = JwtUtils.hs256Parse(token, jwtKey);
                ObjectMapper mapper = new ObjectMapper();
                Map map = mapper.readValue(jwt.payload(), Map.class);
                String id = (String) map.get("sub");
                User.Role role = User.Role.fromString((String) map.get("role"));
                Number iat = (Long) map.get("iat");
                Number exp = (Number) map.get("exp");
                long expiration = iat.longValue() + exp.longValue();
                if (System.currentTimeMillis() < expiration) {
                    final Authentication authentication = new Authentication(id, role, true);
                    SecurityContextHolder.setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clear();
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
        SecurityContextHolder.clear();
    }
}

