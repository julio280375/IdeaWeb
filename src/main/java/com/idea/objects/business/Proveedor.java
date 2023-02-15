package com.idea.objects.business;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class Proveedor {

	private Integer id;
	private String nombre;
	private List<Gasto> gastos;
	private List<Orden> ordenes;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public List<Gasto> getGastos() {
		return gastos;
	}
	public void setGastos(List<Gasto> gastos) {
		this.gastos = gastos;
	}
	public List<Orden> getOrdenes() {
		return ordenes;
	}
	public void setOrdenes(List<Orden> ordenes) {
		this.ordenes = ordenes;
	}
	
	
}
