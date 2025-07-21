package tokoibuelin.storesystem.model;

import java.util.List;

public record Page<T>(long totalData, long totalPage, int page, int size, List<T> data) {
}