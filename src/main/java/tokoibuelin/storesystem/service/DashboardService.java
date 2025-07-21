package tokoibuelin.storesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.repository.OrderRepository;
import tokoibuelin.storesystem.repository.SaleRepository;

@Service
public class DashboardService extends AbstractService{

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SaleRepository saleRepository;

    @Autowired
    OrderRepository orderRepository;

    public Response<Object> countAll(final Authentication authentication, final String table) {
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                String sql = "SELECT COUNT(*) FROM " + table + " WHERE deleted_by IS NULL OR deleted_at IS NULL";
                Long count = jdbcTemplate.queryForObject(sql, Long.class);
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }
    public Response<Object> countUserByRole(final Authentication authentication, final String role) {
        // Validate role and authentication
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND (deleted_by IS NULL OR deleted_at IS NULL)";
                Long count = jdbcTemplate.queryForObject(sql, new Object[]{role}, Long.class);
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }
    public Response<Object> countConfirmOrder(final Authentication authentication) {
        // Validate role and authentication
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                String sql = "SELECT COUNT(*) FROM orders WHERE status = 'PAID' ";
                Long count = jdbcTemplate.queryForObject(sql, Long.class);
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }

    public Response<Object> countIncome(final Authentication authentication) {
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                Long count = saleRepository.sumSalesDay()+orderRepository.sumOrdersDay();
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }

 


    public Response<Object> countSales(final Authentication authentication) {
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            try {
                String sql = "SELECT COUNT(*) FROM  sales  ";
                Long count = jdbcTemplate.queryForObject(sql, Long.class);
                return Response.create("09", "00", "Sukses", count != null ? count : 0L);
            } catch (Exception e) {
                e.printStackTrace();
                return Response.create("01", "00", "Database error", null);
            }
        });
    }




}
