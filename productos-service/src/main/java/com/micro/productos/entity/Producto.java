package com.micro.productos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;
  
  @Column(nullable = false)
  private Double precio;

  public Long getId() {
	return id;
  }
	
  public void setId(Long id) {
	this.id = id;
  }
	
  public String getNombre() {
	return nombre;
  }
	
  public void setNombre(String nombre) {
	this.nombre = nombre;
  }
	
  public Double getPrecio() {
	return precio;
  }
	
  public void setPrecio(Double precio) {
	this.precio = precio;
  }
  
}