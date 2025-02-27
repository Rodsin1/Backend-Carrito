package com.mitiendita.carritoservice.repository;

import com.mitiendita.carritoservice.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}