package com.idea.objects.system;

import java.util.List;

import com.idea.objects.business.Archivo;
import com.idea.objects.business.CuentaCobrar;
import com.idea.objects.business.CuentaPagar;
import com.idea.objects.business.Gasto;
import com.idea.objects.business.Ingreso;
import com.idea.objects.business.Obra;
import com.idea.objects.business.Orden;
import com.idea.objects.business.Proveedor;



public class Body {
	private String filter;
	private String Filter1;
	private String Filter2;
	private Empleado empleado;
	private Gasto gasto;
	private Ingreso ingreso;
	private Obra obra;
	private Orden orden;
	private Proveedor proveedor; 
	private String nombreArchivo;
	private Archivo archivo;
	private CuentaPagar cuentaPagar;
	private CuentaCobrar cuentaCobrar;
	private List<Archivo> listaArchivos;
	private List<Integer> listaInteger;
	private List<Configuracion> listaConfiguracion;
	private List<Gasto> listaGastos;
	private List<Ingreso> listaIngresos;
	private List<CuentaPagar> listaCuentasPagar;
	private List<CuentaCobrar> listaCuentasCobrar;
	
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	

	public String getFilter2() {
		return Filter2;
	}
	public void setFilter2(String Filter2) {
		this.Filter2 = Filter2;
	}
	public String getFilter1() {
		return Filter1;
	}
	public void setFilter1(String Filter1) {
		this.Filter1 = Filter1;
	}
	public List<Integer> getListaInteger() {
		return listaInteger;
	}
	public void setListaInteger(List<Integer> listaInteger) {
		this.listaInteger = listaInteger;
	}
	public List<Configuracion> getListaConfiguracion() {
		return listaConfiguracion;
	}
	public void setListaConfiguracion(List<Configuracion> listaConfiguracion) {
		this.listaConfiguracion = listaConfiguracion;
	}
	public String getNombreArchivo() {
		return nombreArchivo;
	}
	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}
	public Gasto getGasto() {
		return gasto;
	}
	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}
	public Obra getObra() {
		return obra;
	}
	public void setObra(Obra obra) {
		this.obra = obra;
	}
	public Empleado getEmpleado() {
		return empleado;
	}
	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	public Orden getOrden() {
		return orden;
	}
	public void setOrden(Orden orden) {
		this.orden = orden;
	}
	public List<Gasto> getListaGastos() {
		return listaGastos;
	}
	public void setListaGastos(List<Gasto> listaGastos) {
		this.listaGastos = listaGastos;
	}
	public List<Ingreso> getListaIngresos() {
		return listaIngresos;
	}
	public void setListaIngresos(List<Ingreso> listaIngresos) {
		this.listaIngresos = listaIngresos;
	}
	public Ingreso getIngreso() {
		return ingreso;
	}
	public void setIngreso(Ingreso ingreso) {
		this.ingreso = ingreso;
	}
	public Archivo getArchivo() {
		return archivo;
	}
	public void setArchivo(Archivo archivo) {
		this.archivo = archivo;
	}
	public List<Archivo> getListaArchivos() {
		return listaArchivos;
	}
	public void setListaArchivos(List<Archivo> listaArchivos) {
		this.listaArchivos = listaArchivos;
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
	public List<CuentaPagar> getListaCuentasPagar() {
		return listaCuentasPagar;
	}
	public void setListaCuentasPagar(List<CuentaPagar> listaCuentasPagar) {
		this.listaCuentasPagar = listaCuentasPagar;
	}
	public List<CuentaCobrar> getListaCuentasCobrar() {
		return listaCuentasCobrar;
	}
	public void setListaCuentasCobrar(List<CuentaCobrar> listaCuentasCobrar) {
		this.listaCuentasCobrar = listaCuentasCobrar;
	}



	





}
