package com.micro.productos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.micro.productos.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}