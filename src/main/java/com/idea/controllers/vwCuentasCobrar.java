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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import com.idea.objects.business.CuentaCobrar;
import com.idea.objects.business.Gasto;
import com.idea.objects.business.Ingreso;
import com.idea.objects.business.Obra;
import com.idea.objects.business.Orden;
import com.idea.objects.business.Proveedor;
import com.idea.objects.business.Resumen;
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
@ManagedBean(name = "vwCuentasCobrar")
@SessionScoped
@RequestScoped
public class vwCuentasCobrar  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwCuentasCobrar.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
	private DateFormat df_ddMMMyyyy= new SimpleDateFormat("dd-MMM-yyyy");
	private DateFormat df_yyyyMMdd= new SimpleDateFormat("yyyy-MM-dd");	
	private List<String>listaEstatus= new ArrayList<String>(Arrays.asList("PENDIENTE", "ANTICIPO", "PAGADO", "CANCELADO"));
	private Header header;
	private List<CuentaCobrar> listaPrincipal;
	private CuentaCobrar seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private CuentaCobrar registro_guardar;
	private String carpeta_archivos;
	private String carpeta_trabajo;
	private List<String>listaCatalogoIngresos=new ArrayList<>();
	private List<Obra>listaObras;
	private List<String>listaNombreObras=new ArrayList<>();
	private List<Archivo>listaArchivos=new ArrayList<>();
	private Double totalCuentasCobrar;
	private Ingreso ingreso;
	private Tools tools =new Tools();
	
	private List<Resumen> listaResumenObra;
	private List<Resumen> listaResumenEstatus;
	
	private DonutChartModel donutModelObras;
	private DonutChartModel donutModelEstatus;

	private String archivo_e;
	private List<String> listaArchivosPDF;
	private String pdf_to_show;
	private Archivo archivoSeleccionado;

	//Buscar	
	private String obra_b;
	private String concepto_b;
	private String estatus_b;
	private Date fecha_inicio_b;
	private Date fecha_final_b;
	
	//Editar
	private Date fecha_e;
	private String concepto_e;
	private Double importe_e;
	private String factura_e;
	private String detalle_e;
	private String obra_e;
	private String estatus_e;


	
	

	public void iniciaVista() {
		LOG.info("***************** vwCuentasCobrar.iniciaVista() ****************");
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
		
		donutModelObras = new DonutChartModel();
		
		donutModelEstatus = new DonutChartModel();

		
	}
	
	
	
	public void generaExcelCSV() {
		
		writeCsvFromBean(carpeta_archivos + "cuentas_x_cobrar.csv");	
		try {
		    TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException ie) {
		    Thread.currentThread().interrupt();
		}
		
		/*
		 * try{
	          Runtime.getRuntime().exec("cmd /c start "+carpeta_archivos + "cuentas_x_cobrar.csv");
	          }catch(IOException  e){
	              e.printStackTrace();
	          }
		*/
		addMessage("Archivo generado correctamente.","Se generó el archivo: "+carpeta_archivos + "cuentas_x_cobrar.csv",FacesMessage.SEVERITY_INFO);

	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeCsvFromBean(String path)  {
		try {
			if (listaPrincipal.size()>0) {					
			    Writer writer  = new FileWriter(path.toString());
			    StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
			       .withSeparator(CSVWriter.DEFAULT_SEPARATOR)			     
			       .build();
			    sbc.write(listaPrincipal);
			    writer.close();
			   
			    LOG.info(path+" Guardado correctamente!");
			}
	    } catch (Exception e) { 
	    	LOG.info("[writeCsvFromBean] "+e.getMessage());
		}
	}
	
	
	
	public void accionResumen(){		
		
		createDonutModelObras();
		
		createDonutModelEstatus();
		
		preparaListasResumen();
	}	
	
	
	private void preparaListasResumen() {
		Resumen resumen;
		Double importe;
			        
		listaResumenEstatus=new ArrayList<>();
        for(String estatus : listaEstatus) {
        	importe= listaPrincipal.stream().filter(elem-> elem.getEstatus().equals(estatus)).mapToDouble(elem->elem.getImporte()).sum();
        	if(importe>0d) {
	        	resumen = new Resumen();
	        	resumen.setConcepto(estatus);        	
	        	resumen.setImporte(importe);
	        	listaResumenEstatus.add(resumen);
        	}
        }
		    
        listaResumenObra=new ArrayList<>();
		List<String> obras = listaPrincipal.stream().filter(elem-> elem.getObra()!=null).map(elem -> elem.getObra().getNombre()).distinct().collect(Collectors.toList()); 
        for(String obra : obras) {
        	importe= listaPrincipal.stream().filter(elem-> elem.getObra()!=null).filter(elem-> elem.getObra().getNombre().equals(obra)).mapToDouble(elem->elem.getImporte()).sum();
        	resumen = new Resumen();
        	resumen.setConcepto(obra);        	
        	resumen.setImporte(importe);
        	listaResumenObra.add(resumen);
        }
        importe= listaPrincipal.stream().filter(elem-> elem.getObra()==null || elem.getObra().getId()==null).mapToDouble(elem->elem.getImporte()).sum();
        resumen = new Resumen();
    	resumen.setConcepto("Sin Obra");        	
    	resumen.setImporte(importe);
    	listaResumenObra.add(resumen);
	}
	
	
	
	private void createDonutModelEstatus() {
		ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Random rand = new Random();
        Double valor;
        
        for(String label:listaEstatus) {
        	valor = listaPrincipal.stream().filter(elem-> elem.getEstatus().equals(label)).mapToDouble(elem->elem.getImporte()).sum();
        	if(valor>0d) {
        		labels.add(label);
	        	values.add(valor);
	        	String color=tools.regresaColor(rand.nextInt((138 - 1) + 1) + 1);
	        	colors.add(color);
        	}
        }

        DonutChartOptions options = new DonutChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        donutModelEstatus.setOptions(options);
        
        dataSet.setData(values);
        dataSet.setBackgroundColor(colors);
        data.setLabels(labels);
        data.addChartDataSet(dataSet);
        donutModelEstatus.setData(data);          
	}	
	
	private void createDonutModelObras() {
		ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Random rand = new Random();
        Double valor;
        labels = listaPrincipal.stream().filter(elem-> elem.getObra()!=null).map(elem -> elem.getObra().getNombre()).distinct().collect(Collectors.toList()); 
        Collections.sort(labels);
        for(String label:labels) {
        	valor = listaPrincipal.stream().filter(elem-> elem.getObra()!=null).filter(elem-> elem.getObra().getNombre().equals(label)).mapToDouble(elem->elem.getImporte()).sum();
        	values.add(valor);
        	String color=tools.regresaColor(rand.nextInt((138 - 1) + 1) + 1);
        	colors.add(color);
        }
        valor = listaPrincipal.stream().filter(elem-> elem.getObra()==null || elem.getObra().getId()==null).mapToDouble(elem->elem.getImporte()).sum();
        if(valor > 0d) {
        	values.add(valor);
        	labels.add("Sin Obra");
        }
        DonutChartOptions options = new DonutChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        donutModelObras.setOptions(options);
        
        dataSet.setData(values);
        dataSet.setBackgroundColor(colors);
        data.setLabels(labels);
        data.addChartDataSet(dataSet);
        donutModelObras.setData(data);          
	}	
	
	
	
	public void accionPreview(){
		
		String origen=carpeta_archivos+archivoSeleccionado.getNombre();
		
		if(Files.exists(Paths.get(origen))) {
		
			String destino=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp" + carpeta_trabajo + "pdf_to_show.pdf";
			
			mueveArchivo(origen, destino);
			
			try {
			    TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException ie) {
			    Thread.currentThread().interrupt();
			}
				
			pdf_to_show=carpeta_trabajo + "pdf_to_show.pdf";
		}else {
			
			pdf_to_show=carpeta_trabajo + "error.pdf";
			
			addMessage("Error al mostrar archivo.","El archivo "+origen+" no existe...", FacesMessage.SEVERITY_WARN);

		}
		
	}
	
	
	private void mueveArchivo(String source, String target) {
		try {
			
			Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {
			LOG.error("Error en mueveArchivos() "+e.getMessage());
		}
	}
	

	
	private void leeConfiguracion() {
		
		Configuracion configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_TRABAJO")).findFirst().orElse(null);
		
		carpeta_trabajo=configuracion.getValor();
		
		configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("RUTA_LOCAL_ARCHIVOS")).findFirst().orElse(null);
		
		carpeta_archivos=configuracion.getValor();
		
		
	}

	
	private void descargaCatalogos() {
		
		Body body = new Body();
		body.setFilter("INGRESOS");		
		listaCatalogoIngresos=tools.listadoString("tools/stringList", header, body, 30);
		Collections.sort(listaCatalogoIngresos);
		
		body.setFilter("ALL");
		listaObras=tools.listadoObras("obra/filter", header, body, 30);
		if(listaObras!=null && listaObras.size()>0) {
			listaNombreObras=listaObras.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
			Collections.sort(listaNombreObras);
		}
		
		descargaListaPDF();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		
		if (obra_b!=null && obra_b.trim().length()>0) 
			filtro="#OBRA#";
		else
			if (concepto_b!=null && concepto_b.trim().length()>0) 
				filtro="#CONCEPTO#";
			else
				if (estatus_b!=null && estatus_b.trim().length()>0) 
					filtro="#ESTATUS#";
				else
					if (fecha_inicio_b!=null) {
						filtro=filtro+"#FECHA#";
						if(fecha_final_b!=null) filtro=filtro.replace("#FECHA#","#PERIODO#");
					}
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("ALL");
			break;
		case "#ESTATUS#":
			body.setFilter("BY_ESTATUS");
			body.setFilter1(estatus_b);
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
		case "#FECHA#":	
			body.setFilter("BY_FECHA");
			body.setFilter1(df_yyyyMMdd.format(fecha_inicio_b));
			break;
		case "#PERIODO#":
			body.setFilter("BY_PERIODO");
			body.setFilter1(df_yyyyMMdd.format(fecha_inicio_b));
			body.setFilter2(df_yyyyMMdd.format(fecha_final_b));
			break;
		
		}
		
		if(filtro_anterior.equals("ALL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
			
		}
	
		seleccionado=null;
		listaPrincipal=tools.listadoCuentasCobrar("cuentaCobrar/filter", header, body, 30);
		totalCuentasCobrar = 0d;
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
			totalCuentasCobrar = listaPrincipal.stream().mapToDouble(elem->elem.getImporte()).sum();
		}
		
		//EXTRAE EL NOMBRE DE LA OBRA Y SE LO ASIGNA AL STR_OBRA
		listaPrincipal.stream().filter(elem->elem.getObra()!=null).forEach(elem-> elem.setStr_obra(elem.getObra().getNombre()));

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
			estatus_e=seleccionado.getEstatus();
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
		fecha_inicio_b = null;
		fecha_final_b = null;
		estatus_b="PENDIENTE";
		pdf_to_show=carpeta_trabajo+"error.pdf";
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
		estatus_e="PENDIENTE";
		obra_e = "";
		pdf_to_show=carpeta_trabajo+"error.pdf";
		listaArchivos= new ArrayList<Archivo>();
	}



	private void asignaValoresRegistro() {
		
		registro_guardar.setFecha(fecha_e);
		registro_guardar.setImporte(importe_e);
		registro_guardar.setConcepto(concepto_e);
		registro_guardar.setDetalle(detalle_e);
		registro_guardar.setFactura(factura_e);
		registro_guardar.setEstatus(estatus_e);
		registro_guardar.setTipo_factura("CREDITO");
		
		registro_guardar.setObra(null);
		if(obra_e!=null && !obra_e.equals("")) {
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_e.toUpperCase())).findFirst().orElse(null);
			registro_guardar.setObra(obra);
		}
		
	}

	
	
private void asignaValoresRegistroIngreso() {
		ingreso = new Ingreso();
		ingreso.setFecha(fecha_e);
		ingreso.setImporte(importe_e);
		ingreso.setConcepto(concepto_e);
		ingreso.setDetalle(detalle_e);
		ingreso.setFactura(factura_e);
		ingreso.setEstatus(estatus_e);
		ingreso.setTipo_factura("CREDITO");
		ingreso.setObra(null);
		if(obra_e!=null && !obra_e.equals("")) {
			Obra obra=listaObras.stream().filter(elem->elem.getNombre().toUpperCase().equals(obra_e.toUpperCase())).findFirst().orElse(null);
			ingreso.setObra(obra);
		}
		
	}
	

	
	public void descargaListaPDF() {
		Body body = new Body();
		body.setFilter("CUENTAS_COBRAR_PDF_DISPONIBLES");	
		body.setFilter1(carpeta_archivos);
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
		body.setFilter("BY_CUENTAS_COBRAR");	
		body.setFilter1(seleccionado.getId().toString());
		listaArchivos=tools.listadoArchivos("archivo/filter", header, body, 30);
		if(listaArchivos!=null && listaArchivos.size()>0) {
			archivoSeleccionado=listaArchivos.get(0);
		}
		
	}
	
	
	public void agregaArchivo() {
		if(archivo_e!=null && !archivo_e.equals("")) {
			Archivo archivo=new Archivo();
			archivo.setNombre(archivo_e);
			archivo.setTipo("CCOBRAR");
			if(listaArchivos==null) {
				listaArchivos=new ArrayList<>();
			}
			listaArchivos.add(archivo);
			listaArchivosPDF.remove(archivo.getNombre());	
			archivoSeleccionado=archivo;
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
	

	
	public void accionEliminar(){
		LOG.info("***************** vwCuentasCobrar.accionEliminar() ****************");
		Body body = new Body();
		body.setCuentaCobrar(seleccionado);
		Respuesta resp = tools.ejecutaRespuesta("cuentaCobrar/delete", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			addMessage("Registro ha sido eliminado","El registro se elimino correctamente.", FacesMessage.SEVERITY_INFO);			
			inicializaFiltros(true);
		}else {
			addMessage("Error al eliminar el registro","Elimine archivos relacionados al registro", FacesMessage.SEVERITY_WARN);
		}
		
	}
	
	
	
	public void accionGuardar(){
		LOG.info("***************** vwCuentasCobrar.accionGuardar() ****************");
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			PrimeFaces.current().ajax().update(":formaID:mensajeID");
			return;
		}
		
		registro_guardar = new CuentaCobrar();//SI ES NUEVO SE CREA UN NUEVO REGISTRO 
		if(!nuevo) {			
			registro_guardar=seleccionado;//SI NO ES NUEVO SE ACTUALIZA EL REGISTRO CON EL ID SELECCIONADO
		}
		//SE ASIGNAN VALORES AL REGISTRO YA SEA NUEVO O NO 
		asignaValoresRegistro();
		
		guardaRegistro();
		/*
		if(!estatus_e.equals("PAGADO")) {
			guardaRegistro();
		}else {
			guardaRegistroPagado();
		}
		*/
	}
	
	
	private void guardaRegistro() {	
		Body body = new Body();
		body.setCuentaCobrar(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("cuentaCobrar/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getCuentaCobrar().getId();
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
		body.setCuentaCobrar(seleccionado);
		Respuesta resp = tools.ejecutaRespuesta("cuentaCobrar/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			
			asignaValoresRegistroIngreso();
			
			body.setIngreso(ingreso);
			resp = tools.ejecutaRespuesta("ingreso/save", header, body, 30);
			if(resp!=null && resp.getCode()==200) {
				body = gSon.fromJson(resp.getData(), Body.class);
				ingreso.setId(body.getIngreso().getId());
			
				actualizaArchivosIngreso(ingreso);
				
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
	
	
	
	private void actualizaArchivosIngreso(Ingreso ingreso) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
			listaArchivos.stream().forEach(elem-> elem.setTipo("INGRESO"));
			listaArchivos.stream().forEach(elem-> elem.setIngreso(ingreso));		
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
	
	
	private void actualizaArchivos(CuentaCobrar param) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
			listaArchivos.stream().forEach(elem-> elem.setCuentaCobrar(param));		
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
		String result="FECHA";
		if (fecha_e==null || fecha_e.equals("")) return result;
		//result="PROVEEDOR";
		//if (proveedor_e==null || proveedor_e.equals("")) return result;
		//result="SOLICITO";
		//if (solicito_e==null || solicito_e.equals("")) return result;
		//result="IMPORTE";
		//if (importe_e==null || importe_e<=0d) return result;
		result="ESTATUS";
		if ((estatus_e==null)||(estatus_e.equals("PAGADO") && nuevo)) return result;

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






	public List<CuentaCobrar> getListaPrincipal() {
		return listaPrincipal;
	}



	public void setListaPrincipal(List<CuentaCobrar> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}



	public CuentaCobrar getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(CuentaCobrar seleccionado) {
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


	public Double getImporte_e() {
		return importe_e;
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
		return totalCuentasCobrar;
	}



	public void setTotalIngreso(Double totalIngreso) {
		this.totalCuentasCobrar = totalIngreso;
	}



	public String getFactura_e() {
		return factura_e;
	}



	public void setFactura_e(String factura_e) {
		this.factura_e = factura_e;
	}



	public List<String> getListaEstatus() {
		return listaEstatus;
	}



	public void setListaEstatus(List<String> listaEstatus) {
		this.listaEstatus = listaEstatus;
	}



	public String getEstatus_e() {
		return estatus_e;
	}



	public void setEstatus_e(String estatus_e) {
		this.estatus_e = estatus_e;
	}



	public Date getFecha_inicio_b() {
		return fecha_inicio_b;
	}



	public void setFecha_inicio_b(Date fecha_inicio_b) {
		this.fecha_inicio_b = fecha_inicio_b;
	}



	public Date getFecha_final_b() {
		return fecha_final_b;
	}



	public void setFecha_final_b(Date fecha_final_b) {
		this.fecha_final_b = fecha_final_b;
	}



	public Double getTotalCuentasCobrar() {
		return totalCuentasCobrar;
	}



	public void setTotalCuentasCobrar(Double totalCuentasCobrar) {
		this.totalCuentasCobrar = totalCuentasCobrar;
	}




	public String getEstatus_b() {
		return estatus_b;
	}



	public void setEstatus_b(String estatus_b) {
		this.estatus_b = estatus_b;
	}


	public List<Resumen> getListaResumenObra() {
		return listaResumenObra;
	}



	public void setListaResumenObra(List<Resumen> listaResumenObra) {
		this.listaResumenObra = listaResumenObra;
	}




	public DonutChartModel getDonutModelObras() {
		return donutModelObras;
	}



	public void setDonutModelObras(DonutChartModel donutModelObras) {
		this.donutModelObras = donutModelObras;
	}



	public List<Resumen> getListaResumenEstatus() {
		return listaResumenEstatus;
	}



	public void setListaResumenEstatus(List<Resumen> listaResumenEstatus) {
		this.listaResumenEstatus = listaResumenEstatus;
	}



	public DonutChartModel getDonutModelEstatus() {
		return donutModelEstatus;
	}



	public void setDonutModelEstatus(DonutChartModel donutModelEstatus) {
		this.donutModelEstatus = donutModelEstatus;
	}



}
