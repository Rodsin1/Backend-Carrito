package com.mitiendita.carritoservice.service;

import com.mitiendita.carritoservice.dto.OrdenDTO;

import java.util.List;

public interface OrdenService {

    OrdenDTO crearOrden(Long usuarioId);

    OrdenDTO obtenerOrdenPorId(Long id);

    List<OrdenDTO> obtenerOrdenesPorUsuario(Long usuarioId);
}