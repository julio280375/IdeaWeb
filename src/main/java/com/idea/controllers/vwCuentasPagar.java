package com.idea.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
import com.idea.objects.business.CuentaPagar;
import com.idea.objects.business.Gasto;
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
@ManagedBean(name = "vwCuentasPagar")
@SessionScoped
@RequestScoped
public class vwCuentasPagar  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwCuentasPagar.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
	private DateFormat df_ddMMMyyyy= new SimpleDateFormat("dd-MMM-yyyy");
	private DateFormat df_yyyyMMdd= new SimpleDateFormat("yyyy-MM-dd");	
	private List<String>listaEstatus= new ArrayList<String>(Arrays.asList("PENDIENTE", "ANTICIPO", "PAGADO", "CANCELADO"));
	private Header header;
	private List<CuentaPagar> listaPrincipal;
	private CuentaPagar seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private CuentaPagar registro_guardar;
	private String carpeta_gastos;
	private String carpeta_cpagar;
	private String carpeta_trabajo;
	private List<String>listaCatalogoGastos=new ArrayList<>();
	private List<Obra>listaObras;
	private List<String>listaNombreObras=new ArrayList<>();
	private List<Proveedor>listaProveedores;
	private List<String>listaNombreProveedores=new ArrayList<>();
	private List<Empleado>listaEmpleados;
	private List<String>listaNombreEmpleados=new ArrayList<>();
	private List<Archivo>listaArchivos=new ArrayList<>();
	private Double totalCuentasPagar;
	private Gasto gasto;
	private Tools tools =new Tools();
	
	private String archivo_e;
	private List<String> listaArchivosPDF;
	private String pdf_to_preview;
	private String pdf_to_show;
	private Archivo archivoSeleccionado;

	//Buscar	
	private String tipo_b;
	private Date vencimiento_inicio_b;
	private Date vencimiento_final_b;
	private String obra_b;
	private String proveedor_b;
	private String concepto_b;
	
	
	//Editar
	private Date fecha_e;
	private String tipo_e;
	private String concepto_e;
	private Double importe_e;
	private Date vencimiento_e;	
	private String factura_e;
	private String tipo_factura_e;
	private String estatus_e;
	private String detalle_e;

	private String proveedor_e;
	private String obra_e;
	private String solicito_e;
	private String autorizo_e;

	
	

	public void iniciaVista() {
		LOG.info("***************** vwCuentasPagar.iniciaVista() ****************");
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
		
		configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_CUENTAS_PAGAR")).findFirst().orElse(null);
		
		carpeta_cpagar=configuracion.getValor();
		
		configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_FACTURAS_GASTOS")).findFirst().orElse(null);
		
		carpeta_gastos=configuracion.getValor();
		
		
	}

	
	private void descargaCatalogos() {
		
		Body body = new Body();
		body.setFilter("GASTOS");		
		listaCatalogoGastos=tools.listadoString("tools/stringList", header, body, 30);
		
		body.setFilter("ALL");
		listaObras=tools.listadoObras("obra/filter", header, body, 30);
		if(listaObras!=null && listaObras.size()>0) {
			listaNombreObras=listaObras.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
		}
		
		listaProveedores=tools.listadoProveedores("proveedor/filter", header, body, 30);
		if(listaProveedores!=null && listaProveedores.size()>0) {
			listaNombreProveedores=listaProveedores.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
		}
		
		listaEmpleados=tools.listadoEmpleados("empleado/filter", header, body, 30);
		if(listaEmpleados!=null && listaEmpleados.size()>0) {
		listaNombreEmpleados=listaEmpleados.stream().map(elem -> elem.getNombre()).collect(Collectors.toList());
	
		}
		
		descargaListaPDF();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		
		if (proveedor_b!=null && proveedor_b.trim().length()>0) filtro="#PROVEEDOR#";
		if (obra_b!=null && obra_b.trim().length()>0) filtro="#OBRA#";
		if (vencimiento_inicio_b!=null) {
			filtro=filtro+"#FECHA#";
			if(vencimiento_final_b!=null) filtro=filtro.replace("#FECHA#","#PERIODO#");
		}
		if (tipo_b!=null && tipo_b.trim().length()>0) filtro="#TIPO#";
		if (concepto_b!=null && concepto_b.trim().length()>0) filtro="#CONCEPTO#";
		
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#TIPO#":
			body.setFilter("BY_TIPO");
			body.setFilter1(tipo_b);
			break;
		case "#CONCEPTO#":
			body.setFilter("BY_CONCEPTO_LIKE");
			body.setFilter1(concepto_b);
			break;
		case "#PROVEEDOR#":
			body.setFilter("BY_PROVEEDOR");
			Proveedor proveedor = listaProveedores.stream().filter(elem->elem.getNombre().toUpperCase().equals(proveedor_b.toUpperCase())).findFirst().orElse(null);
			body.setFilter1(proveedor.getId().toString());
			break;
		case "#OBRA#":
			body.setFilter("BY_OBRA");
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_b.toUpperCase())).findFirst().orElse(null);
			body.setFilter1(obra.getId().toString());
			break;
		case "#FECHA#":	
			body.setFilter("BY_FECHA");
			body.setFilter1(df_yyyyMMdd.format(vencimiento_inicio_b));
			break;
		case "#PERIODO#":
			body.setFilter("BY_PERIODO");
			body.setFilter1(df_yyyyMMdd.format(vencimiento_inicio_b));
			body.setFilter2(df_yyyyMMdd.format(vencimiento_final_b));
			break;
		
		}
		
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		listaPrincipal=tools.listadoCuentasPagar("cuentaPagar/filter", header, body, 30);
		totalCuentasPagar = 0d;
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
			totalCuentasPagar = listaPrincipal.stream().mapToDouble(elem->elem.getImporte()).sum();
			listaPrincipal.stream().forEach(elem-> elem.setDiasVencimiento(calculaDiasVencimiento(elem.getVencimiento().getTime())));
			listaPrincipal.stream().filter(elem -> elem.getDiasVencimiento().equals(0l)).forEach(elem->elem.setEstatus("VENCIDO"));
		}
	    filtro_anterior=body.getFilter();
		
		seleccionarElemento();
	}
	
	private Long calculaDiasVencimiento(Long fecha) {
		Date now = Calendar.getInstance().getTime();
		Long result = TimeUnit.DAYS.convert(Math.abs(now.getTime() - fecha), TimeUnit.MILLISECONDS);
		if(now.getTime() > fecha) return 0l;
		return result;
	}
	
	
	public void seleccionarElemento(){
		
		editable=false;
		nuevo=false;
		
		if(seleccionado==null) {
			inicializaCapturar();			
			return;
		}
		
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			
			tipo_e=seleccionado.getTipo();			
			fecha_e=seleccionado.getFecha();
			importe_e=seleccionado.getImporte();
			concepto_e=seleccionado.getConcepto();
			vencimiento_e= seleccionado.getVencimiento();
			factura_e=seleccionado.getFactura();
			tipo_factura_e=seleccionado.getTipo_factura();
			estatus_e=seleccionado.getEstatus();
			detalle_e=seleccionado.getDetalle();
			
			obra_e=null;
			if(seleccionado.getObra()!=null) {
				obra_e=seleccionado.getObra().getNombre();
			}
			proveedor_e=null;
			if(seleccionado.getProveedor()!=null) {
				proveedor_e=seleccionado.getProveedor().getNombre();
			}
			solicito_e=null;
			if(seleccionado.getSolicito()!=null) {
				solicito_e=seleccionado.getSolicito().getNombre();
			}
			autorizo_e=null;
			if(seleccionado.getAutorizo()!=null) {
				autorizo_e=seleccionado.getAutorizo().getNombre();
			}
			
			descargaListaArchivos();
	
			
		}		
	}
	
	
	

	public void inicializaFiltros(Boolean buscar){
		tipo_b=null;
		vencimiento_inicio_b = null;
		vencimiento_final_b = null;
		obra_b=null;
		proveedor_b=null;
		concepto_b=null;
		if(buscar) {
			busquedaPrincipal();
		}
	}


	public void inicializaCapturar(){
		tipo_e="";
		concepto_e="";
		fecha_e = Calendar.getInstance().getTime();
		importe_e=0d;
		archivo_e="";
		vencimiento_e= Calendar.getInstance().getTime();
		factura_e="";
		tipo_factura_e="CREDITO";
		estatus_e="PENDIENTE";
		pdf_to_show=carpeta_trabajo+"error.pdf";
		obra_e = "";
		proveedor_e="";
		solicito_e="";
		autorizo_e="";
		detalle_e="";
		
		listaArchivos= new ArrayList<Archivo>();
		
	}



	private void asignaValoresRegistro() {
		registro_guardar.setTipo(tipo_e);
		registro_guardar.setFecha(fecha_e);
		registro_guardar.setImporte(importe_e);
		registro_guardar.setConcepto(concepto_e);
		registro_guardar.setVencimiento(vencimiento_e);
		registro_guardar.setDiasVencimiento(calculaDiasVencimiento(vencimiento_e.getTime()));
		registro_guardar.setFactura(factura_e);
		registro_guardar.setEstatus(estatus_e);
		registro_guardar.setTipo_factura(tipo_factura_e);
		registro_guardar.setDetalle(detalle_e);
		
		registro_guardar.setObra(null);
		if(obra_e!=null && !obra_e.equals("")) {
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setObra(obra);
		}
		registro_guardar.setProveedor(null);
		if(proveedor_e!=null && !proveedor_e.equals("")) {
			Proveedor proveedor = listaProveedores.stream().filter(elem->elem.getNombre().toUpperCase().equals(proveedor_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setProveedor(proveedor);
		}
		registro_guardar.setSolicito(null);
		if(solicito_e!=null && !solicito_e.equals("")) {
			Empleado solicito =listaEmpleados.stream().filter(elem->elem.getNombre().toUpperCase().equals(solicito_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setSolicito(solicito);
		}
		registro_guardar.setAutorizo(null);
		if(autorizo_e!=null && !autorizo_e.equals("")) {
			Empleado autorizo =listaEmpleados.stream().filter(elem->elem.getNombre().toUpperCase().equals(autorizo_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setAutorizo(autorizo);
		}		
	}

	
	
	
	private void asignaValoresRegistroGasto() {
		gasto = new Gasto();
		gasto.setTipo(tipo_e);
		gasto.setFecha(fecha_e);
		gasto.setImporte(importe_e);
		gasto.setConcepto(concepto_e);
		gasto.setVencimiento(vencimiento_e);
		gasto.setFactura(factura_e);
		gasto.setEstatus(estatus_e);
		gasto.setTipo_factura("CREDITO");
		gasto.setDetalle(detalle_e);
		
		gasto.setObra(null);
		if(obra_e!=null && !obra_e.equals("")) {
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_e.toUpperCase())).findFirst().orElse(null);
			gasto.setObra(obra);
		}
		gasto.setProveedor(null);
		if(proveedor_e!=null && !proveedor_e.equals("")) {
			Proveedor proveedor = listaProveedores.stream().filter(elem->elem.getNombre().toUpperCase().equals(proveedor_e.toUpperCase())).findFirst().orElse(null);
			gasto.setProveedor(proveedor);
		}
		gasto.setSolicito(null);
		if(solicito_e!=null && !solicito_e.equals("")) {
			Empleado solicito =listaEmpleados.stream().filter(elem->elem.getNombre().toUpperCase().equals(solicito_e.toUpperCase())).findFirst().orElse(null);
			gasto.setSolicito(solicito);
		}
		gasto.setAutorizo(null);
		if(autorizo_e!=null && !autorizo_e.equals("")) {
			Empleado autorizo =listaEmpleados.stream().filter(elem->elem.getNombre().toUpperCase().equals(autorizo_e.toUpperCase())).findFirst().orElse(null);
			gasto.setAutorizo(autorizo);
		}		
	}
	

	
	
	public void descargaListaPDF() {
		Body body = new Body();
		body.setFilter("CUENTAS_PAGAR_PDF_DISPONIBLES");	
		String pathArchivos=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp";
		body.setFilter1(pathArchivos+carpeta_cpagar);
		listaArchivosPDF=tools.listadoString("tools/stringList", header, body, 30);
	}
	
	
	public void seleccionarElementoArchivo(){
		pdf_to_show=carpeta_trabajo+"error.pdf";
		if(archivoSeleccionado!=null ){
			pdf_to_show=carpeta_cpagar+archivoSeleccionado.getNombre();
		}
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
		body.setFilter("BY_CUENTAS_PAGAR");	
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
			archivo.setTipo("CPAGAR");
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
		LOG.info("***************** vwCuentasPagar.accionGuardar() ****************");
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			PrimeFaces.current().ajax().update(":formaID:mensajeID");
			return;
		}
		
		registro_guardar = new CuentaPagar();//SI ES NUEVO SE CREA UN NUEVO REGISTRO 
		if(!nuevo) {			
			registro_guardar=seleccionado;//SI NO ES NUEVO SE ACTUALIZA EL REGISTRO CON EL ID SELECCIONADO
		}
		//SE ASIGNAN VALORES AL REGISTRO YA SEA NUEVO O NO 
		asignaValoresRegistro();
		
		if(!estatus_e.equals("PAGADO")) {
			guardaRegistro();
		}else {
			guardaRegistroPagado();
		}
		
	}
	
	
	private void guardaRegistro() {	
		Body body = new Body();
		body.setCuentaPagar(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("cuentaPagar/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getCuentaPagar().getId();
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

	
	private void guardaRegistroPagado() {
		Body body = new Body();
		Timestamp now = Timestamp.from(Instant.now());	
		seleccionado.setDeleted(now);
		body.setCuentaPagar(seleccionado);
		Respuesta resp = tools.ejecutaRespuesta("cuentaPagar/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			
			asignaValoresRegistroGasto();
			
			body.setGasto(gasto);
			resp = tools.ejecutaRespuesta("gasto/save", header, body, 30);
			if(resp!=null && resp.getCode()==200) {
				body = gSon.fromJson(resp.getData(), Body.class);
				gasto.setId(body.getGasto().getId());
			
				actualizaArchivosGasto(gasto);
				
				if(listaArchivos!=null && listaArchivos.size()>0) {
					for(Archivo archivo:listaArchivos) {
						mueveArchivo(carpeta_cpagar+archivo.getNombre(), carpeta_gastos+archivo.getNombre());
					}
				}
				addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);		
				
				inicializaFiltros(true);
			}else {
				addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
			}
		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}			
		PrimeFaces.current().ajax().update(":formaID");
	}
	
	
	private void mueveArchivo(String source, String target) {
		try {
			String pathArchivos=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp";
			Files.move(Paths.get(pathArchivos+source), Paths.get(pathArchivos+target), StandardCopyOption.REPLACE_EXISTING);
			LOG.info("Archivo "+source+" movido a "+target);
		} catch (IOException e) {
			LOG.error("Error en mueveArchivos() "+e.getMessage());
		}
	}
	
	private void actualizaArchivosGasto(Gasto gasto) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
			listaArchivos.stream().forEach(elem-> elem.setTipo("GASTO"));
			listaArchivos.stream().forEach(elem-> elem.setGasto(gasto));		
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
	
	
	private void actualizaArchivos(CuentaPagar cuentaPagar) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
			listaArchivos.stream().forEach(elem-> elem.setCuentaPagar(cuentaPagar));		
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
		String result="TIPO";
		//if (tipo_e==null || tipo_e.equals("")) return result;
		//result="PROVEEDOR";
		//if (proveedor_e==null || proveedor_e.equals("")) return result;
		//result="IMPORTE";
		//if (importe_e==null || importe_e<=0d) return result;
		result="ESTATUS";
		if (estatus_e.equals("PAGADO") && nuevo) return result;
		return "";
	}
	
	
	
    
	public void redirectGastos() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("gastos.xhtml");
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






	public List<CuentaPagar> getListaPrincipal() {
		return listaPrincipal;
	}



	public void setListaPrincipal(List<CuentaPagar> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}



	public CuentaPagar getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(CuentaPagar seleccionado) {
		this.seleccionado = seleccionado;
	}



	public Date getFecha_e() {
		return fecha_e;
	}



	public void setFecha_e(Date fecha_e) {
		this.fecha_e = fecha_e;
	}















	public Date getFecha_inicio_b() {
		return vencimiento_inicio_b;
	}



	public void setFecha_inicio_b(Date fecha_inicio_b) {
		this.vencimiento_inicio_b = fecha_inicio_b;
	}



	public Date getFecha_final_b() {
		return vencimiento_final_b;
	}



	public void setFecha_final_b(Date fecha_final_b) {
		this.vencimiento_final_b = fecha_final_b;
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








	





	public String getTipo_e() {
		return tipo_e;
	}


	public void setTipo_e(String tipo_e) {
		this.tipo_e = tipo_e;
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


	public String getTipo_b() {
		return tipo_b;
	}


	public void setTipo_b(String tipo_b) {
		this.tipo_b = tipo_b;
	}



	public String getProveedor_b() {
		return proveedor_b;
	}


	public void setProveedor_b(String proveedor_b) {
		this.proveedor_b = proveedor_b;
	}


	public String getProveedor_e() {
		return proveedor_e;
	}


	public void setProveedor_e(String proveedor_e) {
		this.proveedor_e = proveedor_e;
	}


	public List<String> getListaNombreProveedores() {
		return listaNombreProveedores;
	}


	public void setListaNombreProveedores(List<String> listaNombreProveedores) {
		this.listaNombreProveedores = listaNombreProveedores;
	}



	public List<String> getListaCatalogoGastos() {
		return listaCatalogoGastos;
	}



	public void setListaCatalogoGastos(List<String> listaCatalogoGastos) {
		this.listaCatalogoGastos = listaCatalogoGastos;
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



	public List<Proveedor> getListaProveedores() {
		return listaProveedores;
	}



	public void setListaProveedores(List<Proveedor> listaProveedores) {
		this.listaProveedores = listaProveedores;
	}



	public List<Empleado> getListaEmpleados() {
		return listaEmpleados;
	}



	public void setListaEmpleados(List<Empleado> listaEmpleados) {
		this.listaEmpleados = listaEmpleados;
	}



	public List<String> getListaNombreEmpleados() {
		return listaNombreEmpleados;
	}



	public void setListaNombreEmpleados(List<String> listaNombreEmpleados) {
		this.listaNombreEmpleados = listaNombreEmpleados;
	}



	public String getSolicito_e() {
		return solicito_e;
	}



	public void setSolicito_e(String solicito_e) {
		this.solicito_e = solicito_e;
	}



	public String getAutorizo_e() {
		return autorizo_e;
	}



	public void setAutorizo_e(String autorizo_e) {
		this.autorizo_e = autorizo_e;
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





	public String getConcepto_b() {
		return concepto_b;
	}



	public void setConcepto_b(String concepto_b) {
		this.concepto_b = concepto_b;
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



	public Double getTotalCuentasPagar() {
		return totalCuentasPagar;
	}



	public void setTotalCuentasPagar(Double totalCuentasPagar) {
		this.totalCuentasPagar = totalCuentasPagar;
	}



	public Date getVencimiento_e() {
		return vencimiento_e;
	}



	public void setVencimiento_e(Date vencimiento_e) {
		this.vencimiento_e = vencimiento_e;
	}



	public String getFactura_e() {
		return factura_e;
	}



	public void setFactura_e(String factura_e) {
		this.factura_e = factura_e;
	}



	public String getTipo_factura_e() {
		return tipo_factura_e;
	}



	public void setTipo_factura_e(String tipo_factura_e) {
		this.tipo_factura_e = tipo_factura_e;
	}



	public String getEstatus_e() {
		return estatus_e;
	}



	public void setEstatus_e(String estatus_e) {
		this.estatus_e = estatus_e;
	}



	public List<String> getListaEstatus() {
		return listaEstatus;
	}



	public void setListaEstatus(List<String> listaEstatus) {
		this.listaEstatus = listaEstatus;
	}



	public String getDetalle_e() {
		return detalle_e;
	}



	public void setDetalle_e(String detalle_e) {
		this.detalle_e = detalle_e;
	}




}
