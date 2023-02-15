package com.idea.controllers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.idea.AESSFC;
import com.idea.Tools;
import com.idea.objects.system.Body;
import com.idea.objects.system.Configuracion;
import com.idea.objects.system.Header;
import com.idea.objects.system.Respuesta;
import com.idea.objects.system.Empleado;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;






@SuppressWarnings("unused")
@Named(value = "vwLogin")
@ViewScoped
public class vwLogin{
	private HttpSession session;
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private static Logger LOG =  LoggerFactory.getLogger(vwLogin.class);
	private Header header;
	private List<Configuracion> listaConfiguracion; 
	private List<Empleado> listaEmpleados;
	private List<String> listaCuentas;
	private List<String> listaDepartamentos;
	private List<String> listaPuestos;
	private Tools tools =new Tools();
	private String cuenta_b;
	private String password_b;
	private AESSFC crypt;
	private String mickeyIDEA = "1D3AL05C48051D34";
	
	

	
	
	public void iniciaVista() {
		LOG.info("***************** vwLogin.iniciaVista() ****************");
		
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest reqHttp = (HttpServletRequest) externalContext.getRequest();
		
		
		session = (HttpSession) context.getExternalContext().getSession(true);
		session.setMaxInactiveInterval(60*60);
		header = new Header();
		header.setSesionID(session.getId());
		header.setUrl("http://sr-idea-ws.mx:8090/ideaWS/");
			
		iniciaConfiguracion();
		
		header.setConfiguracion(listaConfiguracion);
		
		session.setAttribute("header", header);
		
		crypt = new AESSFC();
		crypt.addKey(mickeyIDEA);
		
		iniciaLogin();
		
		inicializaEmpleado();
		
	}
		
	
	private void iniciaConfiguracion() {
		descargaConfiguracion();
	}
	
	
	private void iniciaLogin() {
		
		Boolean sesionValida=equipoValido();
		if(sesionValida) {
			descargaListadoEmpleados();
		}else {
			if(listaConfiguracion==null) {
				addMessage("Error al Iniciar Ejecución.", "No se obtuvo listado de configuración", FacesMessage.SEVERITY_WARN);
			}else {
				addMessage("Este Equipo NO Puede Iniciar Sesion", "Existen validaciones de inicio de sesión que no se cumplen.", FacesMessage.SEVERITY_WARN);
			}
		}
	}
	
	private void descargaListadoEmpleados() {
		Body body=new Body();
		body.setFilter("ALL");
		listaEmpleados=tools.listadoEmpleados("empleado/filter", header, body, 30);
		listaCuentas=new ArrayList<>();
		if(listaEmpleados==null) {
			Empleado empleado = new Empleado();
			empleado.setCuenta("idea.admin");
			empleado.setPassword(crypt.encriptar("admin"));
			body.setEmpleado(empleado);
			Respuesta resp=tools.ejecutaRespuesta("empleado/save", header, body, 30);
			if(resp.getCode()==200) {
				iniciaLogin();
			}
		}else {
			
			listaCuentas=listaEmpleados.stream().filter(elem-> elem.getCuenta()!=null && !elem.getCuenta().trim().equals("")).map(elem -> elem.getCuenta()).distinct().collect(Collectors.toList()); 
			
			listaDepartamentos=listaEmpleados.stream().filter(elem->elem.getDepartamento()!=null && !elem.getDepartamento().equals("")).map(elem -> elem.getDepartamento()).distinct().collect(Collectors.toList()); 
		
			listaPuestos=listaEmpleados.stream().filter(elem->elem.getPuesto()!=null && !elem.getPuesto().equals("")).map(elem -> elem.getPuesto()).distinct().collect(Collectors.toList());
			
		}
	}
	
	
	private void descargaConfiguracion() {
		Body body=new Body();
		body.setFilter("ALL");
		listaConfiguracion=tools.listadoConfiguracion("configuracion/filter", header, body, 30);
		if(listaConfiguracion==null || listaConfiguracion.size()==0) {
			//CUANDO LA BD ESTE EN BLANCO SE AGREGAN LOS REGISTRS DE CONFIGURACION BASICOS PARA INICIAR
			listaConfiguracion=new ArrayList<>();
			Configuracion configuracion = new Configuracion();
			configuracion.setConcepto("TIPO_VALIDACION_EQUIPO");
			configuracion.setValor("NONE");
			listaConfiguracion.add(configuracion);
			configuracion = new Configuracion();
			configuracion.setConcepto("TIPO_VALIDACION_PROCESO");
			configuracion.setValor("NONE");
			listaConfiguracion.add(configuracion);
			configuracion = new Configuracion();
			configuracion.setConcepto("FOLDER_TRABAJO");
			configuracion.setValor("/resources/");
			listaConfiguracion.add(configuracion);
			body.setListaConfiguracion(listaConfiguracion);
			configuracion = new Configuracion();
			configuracion.setConcepto("FOLDER_FACTURAS_GASTOS");
			configuracion.setValor("/resources/facturas/gastos/");
			configuracion = new Configuracion();
			configuracion.setConcepto("FOLDER_ORDENES");
			configuracion.setValor("/resources/ordenes/");
			listaConfiguracion.add(configuracion);
			Respuesta resp=tools.ejecutaRespuesta("configuracion/save", header, body, 30);
			if(resp.getCode()==200) {
				iniciaConfiguracion();
			}
		}
	}
	
	
	
	private Boolean equipoValido() {

		if(listaConfiguracion==null) {
			return false;
		}
		Configuracion configuracion=listaConfiguracion.stream().filter(elem->elem.getConcepto().equals("TIPO_VALIDACION_EQUIPO")).findFirst().orElse(null);
		if(configuracion==null) {
			return false;
		}else {
			switch(configuracion.getValor().toUpperCase()) {
			case "NONE":
				break;
			}
		}
		return true;
	}
	
	
	
	
	
	private void inicializaEmpleado() {
		cuenta_b=null;
		password_b="";
	}
	
	
	public Boolean validaBuscaEmpleado() {
		Boolean result=false;
		if(cuenta_b!=null || !password_b.equals("")) {
			result=true;
		}
		return result;
	}
	
	
	
	public void loginEmpleado() {
		if (!validaBuscaEmpleado()) {
			addMessage("Datos de Empleado No Ingresados", "Ingrese una cuenta y password validos.", FacesMessage.SEVERITY_WARN);
		}else {
			
			Empleado empleado = listaEmpleados.stream().filter(elem -> Objects.equals(elem.getCuenta(), cuenta_b)).findFirst().orElse(null);
			
			String passwDecript=crypt.desencriptar(empleado.getPassword());
			
			if(!passwDecript.equals(password_b)) {
				addMessage("Empleado/Password Incorrecto", "Verifique la cuenta o password ingresados.", FacesMessage.SEVERITY_WARN);
			}else {

				header.setEmpleado(empleado);
				session.setAttribute("header", header);
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect("main.xhtml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	

	
	
	public void addMessage(String msgPrimary, String msgSecundary, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();		
	    context.addMessage(null, new FacesMessage(severity, msgPrimary, msgSecundary));
	}
	
	
	

	

	public List<Empleado> getListaEmpleados() {
		return listaEmpleados;
	}




	public void setListaEmpleados(List<Empleado> listaEmpleados) {
		this.listaEmpleados = listaEmpleados;
	}





	public String getCuenta_b() {
		return cuenta_b;
	}



	public void setCuenta_b(String cuenta_b) {
		this.cuenta_b = cuenta_b;
	}



	public String getPassword_b() {
		return password_b;
	}



	public void setPassword_b(String password_b) {
		this.password_b = password_b;
	}



	public List<String> getListaCuentas() {
		return listaCuentas;
	}



	public void setListaCuentas(List<String> listaCuentas) {
		this.listaCuentas = listaCuentas;
	}










	
}
