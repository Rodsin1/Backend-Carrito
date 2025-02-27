package com.mitiendita.carritoservice.repository;

import com.mitiendita.carritoservice.entity.OrdenItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenItemRepository extends JpaRepository<OrdenItem, Long> {

    List<OrdenItem> findByOrdenId(Long ordenId);
}