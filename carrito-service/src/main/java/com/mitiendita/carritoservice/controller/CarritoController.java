package com.mitiendita.carritoservice.controller;

import com.mitiendita.carritoservice.dto.CarritoDTO;
import com.mitiendita.carritoservice.dto.CarritoItemDTO;
import com.mitiendita.carritoservice.security.JwtValidator;
import com.mitiendita.carritoservice.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final JwtValidator jwtValidator;

    public CarritoController(CarritoService carritoService, JwtValidator jwtValidator) {
        this.carritoService = carritoService;
        this.jwtValidator = jwtValidator;
    }

    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(@RequestHeader("Authorization") String token) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        CarritoDTO carrito = carritoService.obtenerCarrito(usuarioId);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping
    public ResponseEntity<CarritoDTO> agregarProducto(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CarritoItemDTO carritoItemDTO) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        CarritoDTO carrito = carritoService.agregarProducto(usuarioId, carritoItemDTO);
        return ResponseEntity.ok(carrito);
    }

    @PutMapping("/{productoId}")
    public ResponseEntity<CarritoDTO> actualizarCantidad(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productoId,
            @RequestBody Map<String, Integer> payload) {
        // Cambio aquí para aceptar cantidad en el body como {cantidad: X}
        Integer cantidad = payload.get("cantidad");
        if (cantidad == null) {
            cantidad = 1; // Valor por defecto
        }

        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        CarritoDTO carrito = carritoService.actualizarCantidad(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<CarritoDTO> eliminarProducto(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productoId) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        CarritoDTO carrito = carritoService.eliminarProducto(usuarioId, productoId);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(@RequestHeader("Authorization") String token) {
        Long usuarioId = obtenerUsuarioIdDesdeToken(token);
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
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