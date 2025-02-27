package com.mitiendita.carritoservice.service;

import com.mitiendita.carritoservice.dto.CarritoDTO;
import com.mitiendita.carritoservice.dto.CarritoItemDTO;
import com.mitiendita.carritoservice.dto.OrdenDTO;
import com.mitiendita.carritoservice.dto.OrdenItemDTO;
import com.mitiendita.carritoservice.entity.Orden;
import com.mitiendita.carritoservice.entity.OrdenItem;
import com.mitiendita.carritoservice.exception.CarritoException;
import com.mitiendita.carritoservice.exception.OrdenNotFoundException;
import com.mitiendita.carritoservice.repository.OrdenItemRepository;
import com.mitiendita.carritoservice.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final OrdenItemRepository ordenItemRepository;
    private final CarritoService carritoService;
    private final RestTemplate restTemplate;


    private static final String PRODUCTOS_API_URL = "http://localhost:8082/api/productos/";

    public OrdenServiceImpl(OrdenRepository ordenRepository,
                            OrdenItemRepository ordenItemRepository,
                            CarritoService carritoService) {
        this.ordenRepository = ordenRepository;
        this.ordenItemRepository = ordenItemRepository;
        this.carritoService = carritoService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Transactional
    public OrdenDTO crearOrden(Long usuarioId) {

        CarritoDTO carritoDTO = carritoService.obtenerCarrito(usuarioId);

        if (carritoDTO.getItems().isEmpty()) {
            throw new CarritoException("No se puede crear una orden con carrito vacío");
        }


        Orden orden = new Orden();
        orden.setUsuarioId(usuarioId);
        orden.setEstado("PENDIENTE");


        orden = ordenRepository.save(orden);


        for (CarritoItemDTO carritoItem : carritoDTO.getItems()) {
            OrdenItem ordenItem = new OrdenItem();
            ordenItem.setOrden(orden);
            ordenItem.setProductoId(carritoItem.getProductoId());
            ordenItem.setCantidad(carritoItem.getCantidad());
            ordenItem.setPrecioUnitario(carritoItem.getPrecioUnitario());
            ordenItem.calcularSubtotal();

            orden.addItem(ordenItem);
        }


        orden = ordenRepository.save(orden);


        carritoService.vaciarCarrito(usuarioId);


        return convertirADTO(orden);
    }

    @Override
    public OrdenDTO obtenerOrdenPorId(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new OrdenNotFoundException("Orden no encontrada con ID: " + id));

        return convertirADTO(orden);
    }

    @Override
    public List<OrdenDTO> obtenerOrdenesPorUsuario(Long usuarioId) {
        List<Orden> ordenes = ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        return ordenes.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private OrdenDTO convertirADTO(Orden orden) {
        List<OrdenItemDTO> itemsDTO = orden.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());

        return OrdenDTO.builder()
                .id(orden.getId())
                .usuarioId(orden.getUsuarioId())
                .estado(orden.getEstado())
                .fechaCreacion(orden.getFechaCreacion())
                .items(itemsDTO)
                .build();
    }

    private OrdenItemDTO convertirItemADTO(OrdenItem item) {
        OrdenItemDTO dto = OrdenItemDTO.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();

        try {
            Map<String, Object> producto = restTemplate.getForObject(PRODUCTOS_API_URL + item.getProductoId(), HashMap.class);

            if (producto != null) {
                dto.setNombreProducto((String) producto.get("nombre"));
                dto.setUrlImagen((String) producto.get("urlImagen"));
            }
        } catch (Exception e) {
            // Ignorar error y continuar con datos básicos
        }

        return dto;
    }
}