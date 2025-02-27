package com.mitiendita.carritoservice.repository;

import com.mitiendita.carritoservice.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);

    Optional<Carrito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioId(Long usuarioId);
}