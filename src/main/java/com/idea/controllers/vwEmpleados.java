package com.idea.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.idea.AESSFC;
import com.idea.Tools;
import com.idea.objects.system.Body;
import com.idea.objects.system.Header;
import com.idea.objects.system.Respuesta;
import com.idea.objects.system.Empleado;





@Component
@ManagedBean(name = "vwEmpleados")
@SessionScoped
public class vwEmpleados  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwEmpleados.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private Header header;
	private List<Empleado> listaPrincipal;
	//private LinkedHashMap<String, Integer> hashmapPerfiles;
	private List<String> listaDescripcionPerfiles;
	private List<String> listaDepartamentos;
	private List<String> listaPuestos;
	private Empleado seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Empleado registro_guardar;
	private StreamedContent descarga;
	private Tools tools=new Tools();
	private AESSFC crypt;
	private String mickeyIDEA = "1D3AL05C48051D34";
	
	//Buscar
	private String nombre_b;
	private String departamento_b;
	private String puesto_b;


	
	//Editar
	private String nombre_e;
	private String departamento_e;
	private String puesto_e;
	private String cuenta_e;
	private String password_e;
	private String confirmar_e;
	

	

	public void iniciaVista() {
		LOG.info("**************** vwEmpleados.iniciaVista() ****************");
		FacesContext context = FacesContext.getCurrentInstance();
		session = (HttpSession) context.getExternalContext().getSession(true);

		header = (Header) session.getAttribute("header");
		
		try {
			if (header==null || header.getEmpleado()==null) 	
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");				
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		crypt = new AESSFC();
		crypt.addKey(mickeyIDEA);
		
		filtro_anterior="";
		
		descargaCatalogos();
		
		inicializaFiltros();
		
		listaDepartamentos=listaPrincipal.stream().filter(elem->elem.getDepartamento()!=null && !elem.getDepartamento().equals("")).map(elem -> elem.getDepartamento()).distinct().collect(Collectors.toList()); 
		
		listaPuestos=listaPrincipal.stream().filter(elem->elem.getPuesto()!=null && !elem.getPuesto().equals("")).map(elem -> elem.getPuesto()).distinct().collect(Collectors.toList());

		
	}
	
	public void inicializaFiltros(){
		nombre_b="";
		departamento_b=null;
		puesto_b=null;
		
		busquedaPrincipal();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		if (nombre_b.trim().length()>0) filtro="#NOMBRE#";
		if (departamento_b!=null && departamento_b.trim().length()>0) filtro=filtro+"#DEPTO#";
		if (puesto_b!=null && puesto_b.trim().length()>0) filtro=filtro+"#PUESTO#";
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#NOMBRE#":
			body.setFilter("BY_NOMBRE_LIKE");
			body.setFilter1(nombre_b);
			break;
		case "#DEPTO#":
			body.setFilter("BY_DEPTO");
			body.setFilter1(departamento_b);
			break;
		case "#NOMBRE##DEPTO#":
			body.setFilter("BY_NOMBRE_DEPTO");
			body.setFilter1(nombre_b);
			body.setFilter2(departamento_b);
			break;
		}
	
		
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		
		listaPrincipal=tools.listadoEmpleados("empleado/filter", header, body, 30);
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
		}
		filtro_anterior=body.getFilter();
		
		seleccionarElemento();
		
		
	}
	
	
	public void seleccionarElemento(){
		if(seleccionado==null) {
			nombre_e="";
			departamento_e="";
			puesto_e="";
			cuenta_e="";
			return;
		}
		nombre_e=seleccionado.getNombre();
		departamento_e=seleccionado.getDepartamento();
		puesto_e=seleccionado.getPuesto();
		cuenta_e=seleccionado.getCuenta();
		password_e=crypt.desencriptar(seleccionado.getPassword());
		confirmar_e=crypt.desencriptar(seleccionado.getPassword());
		editable=false;
		nuevo=false;
	}
	
	
	
	public void accionDescargarPDF(String nombreReporte){
		Body body = new Body();
		body.setNombreArchivo("nombreReporte");
		descarga = tools.descargaStreamed("descargar/empleados", header, body, 30);
	}
	
	
	public void accionAgregar(){
		nombre_e="";
		departamento_e=null;
		puesto_e=null;
		cuenta_e="";
		editable=true;
		password_e="";
		confirmar_e="";
		nuevo=true;
	}
	
	
	public void accionModificar(){			
		editable=true;
		nuevo=false;
	}	
	
	
	public void accionCancelar(){
		if(nuevo) {
			if(listaPrincipal!=null && listaPrincipal.size()>0) {
				seleccionarElemento();
			}
		}
		editable=false;
		nuevo=false;
	}
	

	
	public void accionGuardar(){
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			return;
		}
		registro_guardar = new Empleado();
		if(!nuevo) {
			registro_guardar=seleccionado;
		}
		 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setEmpleado(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("empleado/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getEmpleado().getId();
				registro_guardar.setId(id);
				if(listaPrincipal==null) listaPrincipal=new ArrayList<>();
				listaPrincipal.add(registro_guardar);
				seleccionado=listaPrincipal.stream().filter(elem->elem.getId().equals(id)).findFirst().orElse(null);
			}
			addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);
			seleccionarElemento();
			LOG.info("**************** vwEmpleados.accionGuardar() ****************");
		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}
		editable=false;
		nuevo=false;
	}
	
	
	
	private void asignaValoresRegistro() {
		registro_guardar.setNombre(nombre_e);
		registro_guardar.setDepartamento(departamento_e);
		registro_guardar.setPuesto(puesto_e);
		registro_guardar.setCuenta(cuenta_e);
		registro_guardar.setPassword(crypt.encriptar(password_e));
	}
	
	
	
	private String resultadoValidaGuardado() {
		String result="NOMBRE";
		if (nombre_e.equals("")) return result;
		result="DEPARTAMENTO";
		if (departamento_e==null || departamento_e.equals("")) return result;
		result="PUESTO";
		if (puesto_e==null ||puesto_e.equals("")) return result;
		if(cuenta_e==null || cuenta_e.trim().equals("")) {
			cuenta_e="";
			password_e="";
			confirmar_e="";
		}else {
			result="PASSWORD";
			if (password_e==null || password_e.equals("") || confirmar_e==null ||confirmar_e.equals("") || !confirmar_e.equals(password_e)) return result;
		}
		return "";
	}
	
	
	private void descargaCatalogos() {
		/*
		  Body body = new Body();
		
		body.setFilter("ALL");
		listaPerfiles=listado_perfiles("perfil/filter", body, 30);
		hashmapPerfiles = new LinkedHashMap<>();
		
		for(Perfil elem : listaPerfiles){				
			hashmapPerfiles.put(elem.getDescripcion(), elem.getId());
		}	

		*/
		
	}
	
	
	
	

	public void redirectMenu() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("main.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void redirectLogin() {
		session.invalidate();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	

	
	
	public void addMessage(String msgPrimary, String msgSecundary, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();		
	    context.addMessage(null, new FacesMessage(severity, msgPrimary, msgSecundary));
	}

	
	
	
	public Header getHeader() {
		return header;
	}









	public void setHeader(Header header) {
		this.header = header;
	}



	public String getNombre_b() {
		return nombre_b;
	}



	public void setNombre_b(String nombre_b) {
		this.nombre_b = nombre_b;
	}



	public Boolean getEditable() {
		return editable;
	}



	public void setEditable(Boolean editable) {
		this.editable = editable;
	}




	public List<String> getListaDescripcionPerfiles() {
		return listaDescripcionPerfiles;
	}



	public void setListaDescripcionPerfiles(List<String> listaDescripcionPerfiles) {
		this.listaDescripcionPerfiles = listaDescripcionPerfiles;
	}



	public List<String> getListaDepartamentos() {
		return listaDepartamentos;
	}



	public void setListaDepartamentos(List<String> listaDepartamentos) {
		this.listaDepartamentos = listaDepartamentos;
	}



	public List<String> getListaPuestos() {
		return listaPuestos;
	}



	public void setListaPuestos(List<String> listaPuestos) {
		this.listaPuestos = listaPuestos;
	}



	public String getNombre_e() {
		return nombre_e;
	}



	public void setNombre_e(String nombre_e) {
		this.nombre_e = nombre_e;
	}



	public String getDepartamento_e() {
		return departamento_e;
	}



	public void setDepartamento_e(String departamento_e) {
		this.departamento_e = departamento_e;
	}



	public String getPuesto_e() {
		return puesto_e;
	}



	public void setPuesto_e(String puesto_e) {
		this.puesto_e = puesto_e;
	}














	public Empleado getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(Empleado seleccionado) {
		this.seleccionado = seleccionado;
	}



	public Boolean getNuevo() {
		return nuevo;
	}



	public void setNuevo(Boolean nuevo) {
		this.nuevo = nuevo;
	}



	public String getDepartamento_b() {
		return departamento_b;
	}



	public void setDepartamento_b(String departamento_b) {
		this.departamento_b = departamento_b;
	}



	public String getPuesto_b() {
		return puesto_b;
	}



	public void setPuesto_b(String puesto_b) {
		this.puesto_b = puesto_b;
	}



	






	public String getCuenta_e() {
		return cuenta_e;
	}


	public void setCuenta_e(String cuenta_e) {
		this.cuenta_e = cuenta_e;
	}


	public StreamedContent getDescarga() {
		return descarga;
	}


	public void setDescarga(StreamedContent descarga) {
		this.descarga = descarga;
	}


	public List<Empleado> getListaPrincipal() {
		return listaPrincipal;
	}


	public void setListaPrincipal(List<Empleado> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}


	public String getPassword_e() {
		return password_e;
	}

	public void setPassword_e(String password_e) {
		this.password_e = password_e;
	}

	public String getConfirmar_e() {
		return confirmar_e;
	}

	public void setConfirmar_e(String confirmar_e) {
		this.confirmar_e = confirmar_e;
	}

	
	
	





}
