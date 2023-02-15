package com.idea.controllers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.idea.Tools;
import com.idea.objects.business.Obra;
import com.idea.objects.business.Proveedor;
import com.idea.objects.system.Body;
import com.idea.objects.system.Header;
import com.idea.objects.system.Respuesta;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




@SuppressWarnings("deprecation")
@Component
@ManagedBean(name = "vwProveedores")
@SessionScoped
public class vwProveedores  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwProveedores.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private Header header;
	private List<Proveedor> listaPrincipal;
	//private LinkedHashMap<String, Integer> hashmapPerfiles;

	private Proveedor seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Proveedor registro_guardar;
	private Tools tools =new Tools();
	
	//Buscar
	private String nombre_b;
	
	//Editar
	private String nombre_e;


	

	public void iniciaVista() {
		LOG.info("**************** vwProveedores.iniciaVista() ****************");
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
		
		filtro_anterior="";
		
		descargaCatalogos();
		
		inicializaFiltros();
		

		
	}
	
	public void inicializaFiltros(){
		nombre_b="";			
		
		busquedaPrincipal();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		if (nombre_b.trim().length()>0) filtro="#NOMBRE#";		
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#NOMBRE#":
			body.setFilter("BY_NOMBRE_LIKE");
			body.setFilter1(nombre_b);
			break;
		}
			
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		
		listaPrincipal=tools.listadoProveedores("proveedor/filter", header, body, 30);
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
		}
		filtro_anterior=body.getFilter();
		
		seleccionarElemento();
		
		
	}
	
	
	public void seleccionarElemento(){
		if(seleccionado==null) {
			nombre_e="";
			return;
		}
		nombre_e=seleccionado.getNombre();
		editable=false;
		nuevo=false;
	}
	
	
	
	public void accionAgregar(){
		nombre_e="";
		editable=true;
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
		registro_guardar = new Proveedor();
		if(!nuevo) {
			registro_guardar=seleccionado;
		}
		 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setProveedor(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("proveedor/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getProveedor().getId();
				registro_guardar.setId(id);
				if(listaPrincipal==null) listaPrincipal=new ArrayList<>();
				listaPrincipal.add(registro_guardar);
				seleccionado=listaPrincipal.stream().filter(elem->elem.getId().equals(id)).findFirst().orElse(null);
			}
			addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);
			seleccionarElemento();
			LOG.info("**************** vwProveedores.accionGuardar() ****************");
		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}
		editable=false;
		nuevo=false;
	}
	
	
	
	private void asignaValoresRegistro() {
		registro_guardar.setNombre(nombre_e);

	}
	
	
	
	private String resultadoValidaGuardado() {
		String result="DESCRIPCION";
		if (nombre_e.equals("")) return result;
		return "";
	}
	
	
	private void descargaCatalogos() {
		
		
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

	public List<Proveedor> getListaPrincipal() {
		return listaPrincipal;
	}

	public void setListaPrincipal(List<Proveedor> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}

	public Proveedor getSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(Proveedor seleccionado) {
		this.seleccionado = seleccionado;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public Boolean getNuevo() {
		return nuevo;
	}

	public void setNuevo(Boolean nuevo) {
		this.nuevo = nuevo;
	}

	public String getFiltro_anterior() {
		return filtro_anterior;
	}

	public void setFiltro_anterior(String filtro_anterior) {
		this.filtro_anterior = filtro_anterior;
	}

	public String getDescripcion_b() {
		return nombre_b;
	}

	public void setDescripcion_b(String descripcion_b) {
		this.nombre_b = descripcion_b;
	}

	public String getDescripcion_e() {
		return nombre_e;
	}

	public void setDescripcion_e(String descripcion_e) {
		this.nombre_e = descripcion_e;
	}

	public String getNombre_e() {
		return nombre_e;
	}

	public void setNombre_e(String nombre_e) {
		this.nombre_e = nombre_e;
	}

	public String getNombre_b() {
		return nombre_b;
	}

	public void setNombre_b(String nombre_b) {
		this.nombre_b = nombre_b;
	}

	

	
	



}
