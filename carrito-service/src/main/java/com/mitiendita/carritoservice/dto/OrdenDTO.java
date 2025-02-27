package com.mitiendita.carritoservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDTO {

    private Long id;
    private Long usuarioId;
    private String estado;
    private LocalDateTime fechaCreacion;

    @NotEmpty(message = "La orden debe tener al menos un producto")
    @Valid
    private List<OrdenItemDTO> items = new ArrayList<>();

    private BigDecimal total;

    public BigDecimal getTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(OrdenItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}