package com.idea.controllers;

import java.io.IOException;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.idea.objects.system.Header;




@SuppressWarnings("deprecation")
@Component
@ManagedBean(name = "vwMain")
@SessionScoped
public class vwMain  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwMain.class);
	private Header header;
	
	

	public void iniciaVista() {		
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
	
		
	}
	
	
	
	
	
	
	
	

	public void cerrarSesion() {
		session.invalidate();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}









	public Header getHeader() {
		return header;
	}









	public void setHeader(Header header) {
		this.header = header;
	}
	
	





}
