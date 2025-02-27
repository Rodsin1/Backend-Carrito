package com.mitiendita.carritoservice.controller;

import com.mitiendita.carritoservice.dto.OrdenDTO;
import com.mitiendita.carritoservice.security.JwtValidator;
import com.mitiendita.carritoservice.service.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;
    private final JwtValidator jwtValidator;

    public OrdenController(OrdenService ordenService, JwtValidator jwtValidator) {
        this.ordenService = ordenService;
        this.jwtValidator = jwtValidator;
    }

    @PostMapping
    public ResponseEntity<OrdenDTO> crearOrden(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) Object orderData) {
        // Ignoramos orderData, ya que creamos la orden del carrito existente
        // Pero aceptamos el parámetro para ser compatible con la API del frontend

        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        OrdenDTO orden = ordenService.crearOrden(usuarioId);
        return new ResponseEntity<>(orden, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenDTO> obtenerOrdenPorId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        OrdenDTO orden = ordenService.obtenerOrdenPorId(id);

        // Verificar que la orden pertenece al usuario autenticado
        if (!orden.getUsuarioId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orden);
    }

    @GetMapping
    public ResponseEntity<List<OrdenDTO>> obtenerOrdenesPorUsuario(@RequestHeader("Authorization") String token) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        List<OrdenDTO> ordenes = ordenService.obtenerOrdenesPorUsuario(usuarioId);
        return ResponseEntity.ok(ordenes);
    }

    /**
     * Método auxiliar para obtener el ID del usuario desde el token JWT
     */
    private Long obtenerUsuarioIdDesdeToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtValidator.getUserId(token);
    }
}