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
@ManagedBean(name = "vwObras")
@SessionScoped
public class vwObras  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwObras.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private Header header;
	private List<Obra> listaPrincipal;
	private Obra seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Obra registro_guardar;
	private Tools tools =new Tools();
	
	//Buscar
	private String descripcion_b;

	
	//Editar
	private String descripcion_e;


	

	public void iniciaVista() {
		LOG.info("**************** vwObras.iniciaVista() ****************");
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
		descripcion_b="";
		
		busquedaPrincipal();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		if (descripcion_b.trim().length()>0) filtro="#DESCRIPCION#";
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#DESCRIPCION#":
			body.setFilter("BY_DESCRIPCION_LIKE");
			body.setFilter1(descripcion_b);
			break;

		}
	
		
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		
		listaPrincipal=tools.listadoObras("obra/filter", header, body, 30);
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
		}
		filtro_anterior=body.getFilter();
		
		seleccionarElemento();
		
		
	}
	
	
	public void seleccionarElemento(){
		if(seleccionado==null) {
			descripcion_e="";
			return;
		}
		descripcion_e=seleccionado.getNombre();
		editable=false;
		nuevo=false;
	}
	
	
	
	public void accionAgregar(){
		descripcion_e="";	
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
		registro_guardar = new Obra();
		if(!nuevo) {
			registro_guardar=seleccionado;
		}
		 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setObra(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("obra/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getObra().getId();
				registro_guardar.setId(id);
				if(listaPrincipal==null) listaPrincipal=new ArrayList<>();
				listaPrincipal.add(registro_guardar);
				seleccionado=listaPrincipal.stream().filter(elem->elem.getId().equals(id)).findFirst().orElse(null);
			}
			addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);
			seleccionarElemento();
			LOG.info("**************** vwObras.accionGuardar() ****************");
		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}
		editable=false;
		nuevo=false;
	}
	
	
	
	private void asignaValoresRegistro() {
		registro_guardar.setNombre(descripcion_e);
	}
	
	
	
	private String resultadoValidaGuardado() {
		String result="DESCRIPCION";
		if (descripcion_e.equals("")) return result;
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

	public List<Obra> getListaPrincipal() {
		return listaPrincipal;
	}

	public void setListaPrincipal(List<Obra> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}

	public Obra getSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(Obra seleccionado) {
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
		return descripcion_b;
	}

	public void setDescripcion_b(String descripcion_b) {
		this.descripcion_b = descripcion_b;
	}


	public String getDescripcion_e() {
		return descripcion_e;
	}

	public void setDescripcion_e(String descripcion_e) {
		this.descripcion_e = descripcion_e;
	}


	
	



}
