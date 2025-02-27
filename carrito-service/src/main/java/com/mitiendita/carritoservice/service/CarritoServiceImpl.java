package com.mitiendita.carritoservice.service;

import com.mitiendita.carritoservice.dto.CarritoDTO;
import com.mitiendita.carritoservice.dto.CarritoItemDTO;
import com.mitiendita.carritoservice.entity.Carrito;
import com.mitiendita.carritoservice.exception.CarritoException;
import com.mitiendita.carritoservice.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final RestTemplate restTemplate;


    private static final String PRODUCTOS_API_URL = "http://localhost:8082/api/productos/";

    public CarritoServiceImpl(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public CarritoDTO obtenerCarrito(Long usuarioId) {
        List<Carrito> carritoItems = carritoRepository.findByUsuarioId(usuarioId);

        List<CarritoItemDTO> items = carritoItems.stream()
                .map(this::enriquecerCarritoItem)
                .collect(Collectors.toList());

        return CarritoDTO.builder()
                .usuarioId(usuarioId)
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public CarritoDTO agregarProducto(Long usuarioId, CarritoItemDTO carritoItemDTO) {
        try {

            Map<String, Object> producto = obtenerProducto(carritoItemDTO.getProductoId());


            carritoRepository.findByUsuarioIdAndProductoId(usuarioId, carritoItemDTO.getProductoId())
                    .ifPresentOrElse(

                            carrito -> {
                                carrito.setCantidad(carrito.getCantidad() + carritoItemDTO.getCantidad());
                                carritoRepository.save(carrito);
                            },

                            () -> {
                                Carrito carrito = new Carrito();
                                carrito.setUsuarioId(usuarioId);
                                carrito.setProductoId(carritoItemDTO.getProductoId());
                                carrito.setCantidad(carritoItemDTO.getCantidad());
                                carritoRepository.save(carrito);
                            }
                    );


            return obtenerCarrito(usuarioId);

        } catch (Exception e) {
            throw new CarritoException("Error al agregar producto al carrito: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CarritoDTO actualizarCantidad(Long usuarioId, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new CarritoException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId)
                .orElseThrow(() -> new CarritoException("Producto no encontrado en el carrito"));

        carrito.setCantidad(cantidad);
        carritoRepository.save(carrito);

        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public CarritoDTO eliminarProducto(Long usuarioId, Long productoId) {
        Carrito carrito = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId)
                .orElseThrow(() -> new CarritoException("Producto no encontrado en el carrito"));

        carritoRepository.delete(carrito);

        return obtenerCarrito(usuarioId);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        List<Carrito> carritoItems = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(carritoItems);
    }


    private Map<String, Object> obtenerProducto(Long productoId) {
        try {
            return restTemplate.getForObject(PRODUCTOS_API_URL + productoId, HashMap.class);
        } catch (Exception e) {
            throw new CarritoException("Error al obtener información del producto: " + e.getMessage());
        }
    }


    private CarritoItemDTO enriquecerCarritoItem(Carrito carrito) {
        try {
            Map<String, Object> producto = obtenerProducto(carrito.getProductoId());

            CarritoItemDTO dto = new CarritoItemDTO();
            dto.setId(carrito.getId());
            dto.setProductoId(carrito.getProductoId());
            dto.setCantidad(carrito.getCantidad());
            dto.setNombreProducto((String) producto.get("nombre"));
            dto.setUrlImagen((String) producto.get("urlImagen"));
            String precioStr = producto.get("precio").toString();
            BigDecimal precio = new BigDecimal(precioStr);
            dto.setPrecioUnitario(precio);


            dto.setSubtotal(precio.multiply(BigDecimal.valueOf(carrito.getCantidad())));

            return dto;
        } catch (Exception e) {
            // Si hay error, devolvemos DTO con datos básicos
            return CarritoItemDTO.builder()
                    .id(carrito.getId())
                    .productoId(carrito.getProductoId())
                    .cantidad(carrito.getCantidad())
                    .build();
        }
    }
}