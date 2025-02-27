package com.mitiendita.carritoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO {

    private Long usuarioId;
    private List<CarritoItemDTO> items = new ArrayList<>();
    private BigDecimal total;

    public BigDecimal getTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> {
                    if (item.getSubtotal() != null) {
                        return item.getSubtotal();
                    } else if (item.getPrecioUnitario() != null && item.getCantidad() != null) {
                        return item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
                    } else {
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}