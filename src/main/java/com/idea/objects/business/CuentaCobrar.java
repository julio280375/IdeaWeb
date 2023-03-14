package com.idea.objects.business;

import java.sql.Timestamp;
import java.util.Date;

import com.opencsv.bean.CsvBindByName;

public class CuentaCobrar  {
	
	@CsvBindByName(column = "id")
	private Integer id;
	
	private Date fecha;
	@CsvBindByName(column = "fecha")
	private String str_fecha;
	
	@CsvBindByName(column = "factura")
	private String factura;
	
	@CsvBindByName(column = "tipo")
	private String tipo;
	
	@CsvBindByName(column = "concepto")
	private String concepto;
	
	@CsvBindByName(column = "importe")
	private Double importe;
	
	@CsvBindByName(column = "tipo_factura")
	private String tipo_factura;
	
	@CsvBindByName(column = "estatus")
	private String estatus;
	
	private Obra obra;
	@CsvBindByName(column = "obra")
	private String str_obra;
	
	private String detalle;	
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
	public String getFactura() {
		return factura;
	}
	public void setFactura(String factura) {
		this.factura = factura;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
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
	public Obra getObra() {
		return obra;
	}
	public void setObra(Obra obra) {
		this.obra = obra;
	}
	public String getDetalle() {
		return detalle;
	}
	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
	public String getTipo_factura() {
		return tipo_factura;
	}
	public void setTipo_factura(String tipo_factura) {
		this.tipo_factura = tipo_factura;
	}
	public String getEstatus() {
		return estatus;
	}
	public void setEstatus(String estatus) {
		this.estatus = estatus;
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
	public String getStr_fecha() {
		return str_fecha;
	}
	public void setStr_fecha(String str_fecha) {
		this.str_fecha = str_fecha;
	}
	public String getStr_obra() {
		return str_obra;
	}
	public void setStr_obra(String str_obra) {
		this.str_obra = str_obra;
	}
	
	
}
