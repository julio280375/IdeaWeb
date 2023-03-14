package com.idea.objects.business;

import java.sql.Timestamp;
import java.util.Date;

import com.opencsv.bean.CsvBindByName;

public class Ingreso {
	@CsvBindByName(column = "id")
	private Integer id;
	
	private Date fecha;
	@CsvBindByName(column = "fecha")
	private String str_fecha;
	
	@CsvBindByName(column = "concepto")
	private String concepto;
	
	@CsvBindByName(column = "importe")
	private Double importe;
	
	@CsvBindByName(column = "movimiento")
	private Long movimiento;
	
	private String detalle;
	
	@CsvBindByName(column = "factura")
	private String factura;
	
	private String estatus;
	
	@CsvBindByName(column = "tipo_factura")
	private String tipo_factura;
	
	@CsvBindByName(column = "obra")
	private String str_obra;
	
	private Obra obra;
	
	private Timestamp created;
	private Timestamp deleted;
	
	


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






	public Timestamp getCreated() {
		return created;
	}






	public void setCreated(Timestamp created) {
		this.created = created;
	}






	public Timestamp getDeleted() {
		return deleted;
	}






	public void setDeleted(Timestamp deleted) {
		this.deleted = deleted;
	}






	public String getStr_obra() {
		return str_obra;
	}






	public void setStr_obra(String str_obra) {
		this.str_obra = str_obra;
	}






	public String getStr_fecha() {
		return str_fecha;
	}






	public void setStr_fecha(String str_fecha) {
		this.str_fecha = str_fecha;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	}
