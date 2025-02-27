package com.mitiendita.carritoservice.service;

import com.mitiendita.carritoservice.dto.CarritoDTO;
import com.mitiendita.carritoservice.dto.CarritoItemDTO;

public interface CarritoService {


    CarritoDTO obtenerCarrito(Long usuarioId);

    CarritoDTO agregarProducto(Long usuarioId, CarritoItemDTO carritoItemDTO);

    CarritoDTO actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad);

    CarritoDTO eliminarProducto(Long usuarioId, Long productoId);

    void vaciarCarrito(Long usuarioId);
}