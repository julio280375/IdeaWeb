package com.idea.objects.business;

import java.util.Date;
import com.idea.objects.system.Empleado;

public class Orden  {

	private Integer id;
	private Date fecha;
	private String tipo;
	private String concepto;
	private Double importe;
	private String archivo;
	
	private Proveedor proveedor;
	private Obra obra;
	private Empleado solicito;
	private Empleado autorizo;
	private Gasto gasto;
	
	
	
	
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
	public String getArchivo() {
		return archivo;
	}
	public void setArchivo(String archivo) {
		this.archivo = archivo;
	}
	public Obra getObra() {
		return obra;
	}
	public void setObra(Obra obra) {
		this.obra = obra;
	}
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	public Empleado getSolicito() {
		return solicito;
	}
	public void setSolicito(Empleado solicito) {
		this.solicito = solicito;
	}
	public Empleado getAutorizo() {
		return autorizo;
	}
	public void setAutorizo(Empleado autorizo) {
		this.autorizo = autorizo;
	}
	public Gasto getGasto() {
		return gasto;
	}
	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	}
