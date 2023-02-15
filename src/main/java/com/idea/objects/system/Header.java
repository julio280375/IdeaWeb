package com.idea.objects.system;

import java.util.List;


public class Header {
	private String sesionID;
	private Empleado empleado;
	private String url;
	private List<Configuracion> configuracion;

	

	public String getSesionID() {
		return sesionID;
	}
	public void setSesionID(String sesionID) {
		this.sesionID = sesionID;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<Configuracion> getConfiguracion() {
		return configuracion;
	}
	public void setConfiguracion(List<Configuracion> configuracion) {
		this.configuracion = configuracion;
	}
	public Empleado getEmpleado() {
		return empleado;
	}
	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}
	

}
