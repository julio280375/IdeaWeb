package com.idea.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.ChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.donut.DonutChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.idea.Tools;
import com.idea.objects.business.Archivo;
import com.idea.objects.business.Gasto;
import com.idea.objects.business.Ingreso;
import com.idea.objects.business.Obra;
import com.idea.objects.business.Orden;
import com.idea.objects.business.Proveedor;
import com.idea.objects.system.Body;
import com.idea.objects.system.Configuracion;
import com.idea.objects.system.Empleado;
import com.idea.objects.system.Header;
import com.idea.objects.system.Respuesta;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;


@SuppressWarnings("deprecation")
@Component
@ManagedBean(name = "vwIngresos")
@SessionScoped
@RequestScoped
public class vwIngresos  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwIngresos.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
	private DateFormat df_ddMMMyyyy= new SimpleDateFormat("dd-MMM-yyyy");
	private DateFormat df_yyyyMMdd= new SimpleDateFormat("yyyy-MM-dd");	
	private Header header;
	private List<Ingreso> listaPrincipal;
	private Ingreso seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Ingreso registro_guardar;
	private String carpeta_ingresos;
	private String carpeta_trabajo;
	private List<String>listaCatalogoIngresos=new ArrayList<>();
	private List<Obra>listaObras;
	private List<String>listaNombreObras=new ArrayList<>();
	private List<Archivo>listaArchivos=new ArrayList<>();
	private Double totalIngreso;
	private Tools tools =new Tools();

	private String archivo_e;
	private List<String> listaArchivosPDF;
	private String pdf_to_preview;
	private String pdf_to_show;
	private Archivo archivoSeleccionado;

	//Buscar	
	private String obra_b;
	private String concepto_b;
	
	
	//Editar
	private Date fecha_e;
	private String concepto_e;
	private Double importe_e;
	private String factura_e;
	private String detalle_e;
	private String obra_e;


	
	

	public void iniciaVista() {
		LOG.info("***************** vwIngresos.iniciaVista() ****************");
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
		
		leeConfiguracion();
		
		filtro_anterior="";			
				
		descargaCatalogos();
		
		inicializaFiltros(true);
		
	}
	

	
	private void leeConfiguracion() {
		
		Configuracion configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_TRABAJO")).findFirst().orElse(null);
		
		carpeta_trabajo=configuracion.getValor();
		
		configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_FACTURAS_INGRESOS")).findFirst().orElse(null);
		
		carpeta_ingresos=configuracion.getValor();
		
		
	}

	
	private void descargaCatalogos() {
		
		Body body = new Body();
		body.setFilter("INGRESOS");		
		listaCatalogoIngresos=tools.listadoString("tools/stringList", header, body, 30);
		
		body.setFilter("ALL");
		listaObras=tools.listadoObras("obra/filter", header, body, 30);
		if(listaObras!=null && listaObras.size()>0) {
			listaNombreObras=listaObras.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
		}
		
		descargaListaPDF();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		
		if (obra_b!=null && obra_b.trim().length()>0) filtro="#OBRA#";
		if (concepto_b!=null && concepto_b.trim().length()>0) filtro="#CONCEPTO#";
		
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#CONCEPTO#":
			body.setFilter("BY_CONCEPTO_LIKE");
			body.setFilter1(concepto_b);
			break;
		case "#OBRA#":
			body.setFilter("BY_OBRA");
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_b.toUpperCase())).findFirst().orElse(null);
			body.setFilter1(obra.getId().toString());
			break;
		
		}
		
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		listaPrincipal=tools.listadoIngresos("ingreso/filter", header, body, 30);
		totalIngreso = 0d;
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
			totalIngreso = listaPrincipal.stream().mapToDouble(elem->elem.getImporte()).sum();
		}

		filtro_anterior=body.getFilter();
		
		seleccionarElemento();
	}
	
	
	
	
public void seleccionarElemento(){
		
		editable=false;
		nuevo=false;
		
		if(seleccionado==null) {
			inicializaCapturar();			
			return;
		}
		
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
					
			fecha_e=seleccionado.getFecha();
			importe_e=seleccionado.getImporte();
			factura_e=seleccionado.getFactura();
			concepto_e=seleccionado.getConcepto();
			detalle_e=seleccionado.getDetalle();
			obra_e=null;
			if(seleccionado.getObra()!=null) {
				obra_e=seleccionado.getObra().getNombre();
			}
			
			
			descargaListaArchivos();		
			
		}		
	}
	
	
	

public void inicializaFiltros(Boolean buscar){

	obra_b=null;
	concepto_b=null;
	if(buscar) {
		busquedaPrincipal();
	}
}


public void inicializaCapturar(){
	concepto_e="";
	fecha_e = Calendar.getInstance().getTime();
	importe_e=0d;
	factura_e="";
	detalle_e="";
	archivo_e="";
	pdf_to_show=carpeta_trabajo+"error.pdf";
	obra_e = "";
	
	listaArchivos= new ArrayList<Archivo>();
}



	private void asignaValoresRegistro() {
		
		registro_guardar.setFecha(fecha_e);
		registro_guardar.setImporte(importe_e);
		registro_guardar.setConcepto(concepto_e);
		registro_guardar.setDetalle(detalle_e);
		registro_guardar.setFactura(factura_e);
		registro_guardar.setObra(null);
		if(obra_e!=null && !obra_e.equals("")) {
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setObra(obra);
		}
		
	}

	
	
	public void seleccionarElementoArchivo(){
		pdf_to_show=carpeta_trabajo+"error.pdf";
		if(archivoSeleccionado!=null ){
			pdf_to_show=carpeta_ingresos+archivoSeleccionado.getNombre();
		}
	}

	
	public void descargaListaPDF() {
		Body body = new Body();
		body.setFilter("INGRESOS_PDF_DISPONIBLES");	
		String pathArchivos=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp";
		body.setFilter1(pathArchivos+carpeta_ingresos);
		listaArchivosPDF=tools.listadoString("tools/stringList", header, body, 30);
	}
	
	
	
	
	
	
	
	public void eliminaArchivo() {
		Body body = new Body();
		body.setArchivo(archivoSeleccionado);
		Respuesta resp = tools.ejecutaRespuesta("archivo/delete", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			listaArchivos.remove(archivoSeleccionado);
			
			listaArchivosPDF.add(archivoSeleccionado.getNombre());
		}
	}
	
	
	
	private void descargaListaArchivos() {
		Body body = new Body();
		body.setFilter("BY_INGRESO");	
		body.setFilter1(seleccionado.getId().toString());
		listaArchivos=tools.listadoArchivos("archivo/filter", header, body, 30);
		if(listaArchivos!=null && listaArchivos.size()>0) {
			archivoSeleccionado=listaArchivos.get(0);
		}
		
		seleccionarElementoArchivo();
	}
	
	
	public void agregaArchivo() {
		if(archivo_e!=null && !archivo_e.equals("")) {
			Archivo archivo=new Archivo();
			archivo.setNombre(archivo_e);
			archivo.setTipo("INGRESO");
			if(listaArchivos==null) {
				listaArchivos=new ArrayList<>();
			}
			listaArchivos.add(archivo);
			listaArchivosPDF.remove(archivo.getNombre());	
			archivoSeleccionado=archivo;
			
			seleccionarElementoArchivo();
		}
	}
	
	
	
	public void accionAgregar(){
		
		inicializaCapturar();
		
		editable=true;
		
		nuevo=true;
	}
	
	
	public void accionModificar(){			
		
		if( listaArchivosPDF==null ) {
			listaArchivosPDF= new ArrayList<String>();
		}
		
		editable=true;		
		nuevo=false;
	}	
	
	
	public void accionCancelar(){
			
		seleccionarElemento();
			
		editable=false;
		nuevo=false;
	}
	

	
	public void accionGuardar(){
		LOG.info("***************** vwIngresos.accionGuardar() ****************");
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			PrimeFaces.current().ajax().update(":formaID:mensajeID");
			return;
		}
		registro_guardar = new Ingreso();//SI ES NUEVO SE CREA UN NUEVO REGISTRO 
		if(!nuevo) {			
			registro_guardar=seleccionado;//SI NO ES NUEVO SE ACTUALIZA EL REGISTRO CON EL ID SELECCIONADO
		}
		//SE ASIGNAN VALORES AL REGISTRO YA SEA NUEVO O NO 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setIngreso(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("ingreso/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getIngreso().getId();
				registro_guardar.setId(id);
				if(listaPrincipal==null) listaPrincipal=new ArrayList<>();
				listaPrincipal.add(registro_guardar);
				seleccionado=listaPrincipal.stream().filter(elem->elem.getId().equals(id)).findFirst().orElse(null);				
			}
			
			actualizaArchivos(registro_guardar);
			
			descargaListaPDF();
			
			seleccionarElemento();	
			
			addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);

		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}
		editable=false;
		nuevo=false;
		PrimeFaces.current().ajax().update(":formaID");
	}
	

	
	
	private void actualizaArchivos(Ingreso param) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
			listaArchivos.stream().forEach(elem-> elem.setIngreso(param));		
			Body body = new Body();
			body.setListaArchivos(listaArchivos);
			Respuesta resp = tools.ejecutaRespuesta("archivo/saveAll", header, body, 30);
			if(resp!=null && resp.getCode()==200) {	
				LOG.info("OK! Archivos guardados correctamente!");			
			}else {
				LOG.error("ERROR! Los archivos no se guardaron correctamente!");
			}
		}
	}
	
	
	
	
	
	
	
	private String resultadoValidaGuardado() {
		//String result="TIPO";
		//if (tipo_e==null || tipo_e.equals("")) return result;
		//result="PROVEEDOR";
		//if (proveedor_e==null || proveedor_e.equals("")) return result;
		//result="SOLICITO";
		//if (solicito_e==null || solicito_e.equals("")) return result;
		//result="IMPORTE";
		//if (importe_e==null || importe_e<=0d) return result;
		return "";
	}
	
	
	
    
	public void redirectOrdenes() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("ordenes.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public Boolean getEditable() {
		return editable;
	}
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}






	public List<Ingreso> getListaPrincipal() {
		return listaPrincipal;
	}



	public void setListaPrincipal(List<Ingreso> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}



	public Ingreso getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(Ingreso seleccionado) {
		this.seleccionado = seleccionado;
	}



	public Date getFecha_e() {
		return fecha_e;
	}



	public void setFecha_e(Date fecha_e) {
		this.fecha_e = fecha_e;
	}



	public void setImporte_e(Double importe_e) {
		this.importe_e = importe_e;
	}



	public String getConcepto_e() {
		return concepto_e;
	}



	public void setConcepto_e(String concepto_e) {
		this.concepto_e = concepto_e;
	}


	public String getPdf_to_show() {
		return pdf_to_show;
	}



	public void setPdf_to_show(String pdf_to_show) {
		this.pdf_to_show = pdf_to_show;
	}




	public String getObra_e() {
		return obra_e;
	}


	public void setObra_e(String obra_e) {
		this.obra_e = obra_e;
	}


	public String getObra_b() {
		return obra_b;
	}


	public void setObra_b(String obra_b) {
		this.obra_b = obra_b;
	}


	
	public List<String> getListaCatalogoIngresos() {
		return listaCatalogoIngresos;
	}



	public void setListaCatalogoIngresos(List<String> listaCatalogoIngresos) {
		this.listaCatalogoIngresos = listaCatalogoIngresos;
	}



	public List<Obra> getListaObras() {
		return listaObras;
	}



	public void setListaObras(List<Obra> listaObras) {
		this.listaObras = listaObras;
	}



	public List<String> getListaNombreObras() {
		return listaNombreObras;
	}



	public void setListaNombreObras(List<String> listaNombreObras) {
		this.listaNombreObras = listaNombreObras;
	}


	public List<String> getListaArchivosPDF() {
		return listaArchivosPDF;
	}







	public void setListaArchivosPDF(List<String> listaArchivosPDF) {
		this.listaArchivosPDF = listaArchivosPDF;
	}



	public String getPdf_to_preview() {
		return pdf_to_preview;
	}



	public void setPdf_to_preview(String pdf_to_preview) {
		this.pdf_to_preview = pdf_to_preview;
	}



	public Double getImporte_e() {
		return importe_e;
	}



	public Double getTotalGasto() {
		return totalIngreso;
	}



	public void setTotalGasto(Double totalGasto) {
		this.totalIngreso = totalGasto;
	}




	public String getConcepto_b() {
		return concepto_b;
	}



	public void setConcepto_b(String concepto_b) {
		this.concepto_b = concepto_b;
	}



	public String getDetalle_e() {
		return detalle_e;
	}



	public void setDetalle_e(String detalle_e) {
		this.detalle_e = detalle_e;
	}



	public List<Archivo> getListaArchivos() {
		return listaArchivos;
	}



	public void setListaArchivos(List<Archivo> listaArchivos) {
		this.listaArchivos = listaArchivos;
	}



	public Archivo getArchivoSeleccionado() {
		return archivoSeleccionado;
	}



	public void setArchivoSeleccionado(Archivo archivoSeleccionado) {
		this.archivoSeleccionado = archivoSeleccionado;
	}



	public String getArchivo_e() {
		return archivo_e;
	}



	public void setArchivo_e(String archivo_e) {
		this.archivo_e = archivo_e;
	}



	public Double getTotalIngreso() {
		return totalIngreso;
	}



	public void setTotalIngreso(Double totalIngreso) {
		this.totalIngreso = totalIngreso;
	}



	public String getFactura_e() {
		return factura_e;
	}



	public void setFactura_e(String factura_e) {
		this.factura_e = factura_e;
	}



}
