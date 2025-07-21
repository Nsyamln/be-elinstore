package tokoibuelin.storesystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.UpdateAddressReq;
import tokoibuelin.storesystem.model.request.addAddressesReq;
import tokoibuelin.storesystem.repository.AddressRepository;
import tokoibuelin.storesystem.service.AddressService;
import tokoibuelin.storesystem.util.SecurityContextHolder;


@RestController
@RequestMapping("/secured/address")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class AddressController {
    @Autowired
    AddressService addressService;
    @Autowired
    AddressRepository addressRepository;

    @PutMapping("/edit-address/{addressId}")
    public Response<Object> editAddress(@RequestBody UpdateAddressReq req, @PathVariable String addressId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return addressService.updateAddress(authentication, req, addressId);
    }
    @PostMapping("/add-address")
    public Response<Object> addAddress(@RequestBody addAddressesReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return addressService.addAddress(authentication, req);
    }
    @GetMapping("/findById/{userId}")
    public Response<Object> getAddressesByUserId(@PathVariable String userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
         if (userId == null || userId.trim().isEmpty()) {
            
            Response.create("06", "01", "ID Pengguna tidak boleh kosong.", null);
        
        }
        if (addresses.isEmpty()) {
            
                Response.create("06", "02", "Tidak ada alamat ditemukan untuk ID Pengguna: " + userId, List.of());
          
        }
        
            return Response.create("06", "00", "Daftar alamat untuk ID Pengguna " + userId + " berhasil diambil.", addresses);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Response<Object>> validateAddress(@RequestBody Address address) {
        Response<Object> response = addressService.validateAddress(address);
        if (response.code().endsWith("200")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/delete-address/{addressId}")
    public Response<Object> deleteAddress(@PathVariable String addressId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return addressService.deleteAddress(authentication, addressId);
    }

}
