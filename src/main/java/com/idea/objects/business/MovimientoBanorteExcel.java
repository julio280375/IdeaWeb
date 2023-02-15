package com.idea.objects.business;



import java.util.Date;



public class MovimientoBanorteExcel  {
	private Long movimiento;
	private String fecha;
	private String descripcion;
	private String detalle;
	private String deposito;
	private String retiro;
	private String aux;
	
	public Long getMovimiento() {
		return movimiento;
	}
	public void setMovimiento(Long movimiento) {
		this.movimiento = movimiento;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getDetalle() {
		return detalle;
	}
	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
	public String getDeposito() {
		return deposito;
	}
	public void setDeposito(String deposito) {
		this.deposito = deposito;
	}
	public String getRetiro() {
		return retiro;
	}
	public void setRetiro(String retiro) {
		this.retiro = retiro;
	}
	public String getAux() {
		return aux;
	}
	public void setAux(String aux) {
		this.aux = aux;
	}
	
	
}
