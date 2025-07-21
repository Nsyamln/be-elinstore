package tokoibuelin.storesystem.model;

import tokoibuelin.storesystem.entity.User;

public record Authentication(String id, User.Role role, boolean isAuthenticated) {
}
