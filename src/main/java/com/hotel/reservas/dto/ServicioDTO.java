package com.hotel.reservas.dto;

import com.hotel.reservas.model.TipoServicio;
import jakarta.validation.constraints.NotNull;

public record ServicioDTO(
        @NotNull(message = "El tipo de servicio es obligatorio") TipoServicio tipoServicio
) {
}
