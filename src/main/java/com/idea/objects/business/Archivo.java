package com.idea.objects.business;



public class Archivo {

	private Integer id;
	private String tipo;
	private String nombre;
	private Gasto gasto;
	private Ingreso ingreso;
	private CuentaPagar cuentaPagar;
	private CuentaCobrar cuentaCobrar;
	
	
	

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


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public Gasto getGasto() {
		return gasto;
	}


	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}


	public Ingreso getIngreso() {
		return ingreso;
	}


	public void setIngreso(Ingreso ingreso) {
		this.ingreso = ingreso;
	}


	public CuentaPagar getCuentaPagar() {
		return cuentaPagar;
	}


	public void setCuentaPagar(CuentaPagar cuentaPagar) {
		this.cuentaPagar = cuentaPagar;
	}


	public CuentaCobrar getCuentaCobrar() {
		return cuentaCobrar;
	}


	public void setCuentaCobrar(CuentaCobrar cuentaCobrar) {
		this.cuentaCobrar = cuentaCobrar;
	}




	
	
	
	
	
	
	
	
	}
