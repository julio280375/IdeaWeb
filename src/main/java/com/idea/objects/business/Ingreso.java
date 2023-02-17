package com.idea.objects.business;

import java.util.Date;

public class Ingreso {

	private Integer id;
	private Date fecha;
	private String concepto;
	private Double importe;
	private Long movimiento;
	private String detalle;
	private String factura;
	private String estatus;
	private String tipo_factura;
	
	private Obra obra;


	public Integer getId() {
		return id;
	}






	public void setId(Integer id) {
		this.id = id;
	}






	public Date getFecha() {
		return fecha;
	}






	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}






	public String getConcepto() {
		return concepto;
	}






	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}






	public Double getImporte() {
		return importe;
	}






	public void setImporte(Double importe) {
		this.importe = importe;
	}






	public Long getMovimiento() {
		return movimiento;
	}






	public void setMovimiento(Long movimiento) {
		this.movimiento = movimiento;
	}






	public String getDetalle() {
		return detalle;
	}






	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}






	public Obra getObra() {
		return obra;
	}






	public void setObra(Obra obra) {
		this.obra = obra;
	}






	public String getFactura() {
		return factura;
	}






	public void setFactura(String factura) {
		this.factura = factura;
	}






	public String getEstatus() {
		return estatus;
	}






	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}






	public String getTipo_factura() {
		return tipo_factura;
	}






	public void setTipo_factura(String tipo_factura) {
		this.tipo_factura = tipo_factura;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	}
