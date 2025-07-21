package tokoibuelin.storesystem.service;

import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;

import java.util.Optional;


abstract class AbstractService {

    Optional<Response<Object>> precondition(final Authentication authentication, User.Role... role) {
        String idUser = authentication.id();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(Response.unauthenticated());
        }
        if (authentication.id() == null || Integer.parseInt(idUser.substring(2)) < 0) {
            return Optional.of(Response.unauthorized());
        }
        for (User.Role r : role) {
            if (r == authentication.role()) {
                return Optional.empty();
            }
        }
        return Optional.of(Response.unauthorized());
    }

}