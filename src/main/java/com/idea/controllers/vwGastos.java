package com.idea.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.donut.DonutChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.idea.Tools;
import com.idea.objects.business.Archivo;
import com.idea.objects.business.Gasto;
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


@Named(value = "vwGastos")
@ViewScoped
public class vwGastos implements Serializable  {


	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwGastos.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
	private DateFormat df_ddMMMyyyy= new SimpleDateFormat("dd-MMM-yyyy");
	private DateFormat df_yyyyMMdd= new SimpleDateFormat("yyyy-MM-dd");	
	private Header header;
	private List<Gasto> listaPrincipal;
	private Gasto seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Gasto registro_guardar;
	private String carpeta_archivos;
	private String carpeta_trabajo;
	private List<String>listaCatalogoGastos=new ArrayList<>();
	private List<Obra>listaObras;
	private List<String>listaNombreObras=new ArrayList<>();
	private List<Proveedor>listaProveedores;
	private List<String>listaNombreProveedores=new ArrayList<>();
	private List<Empleado>listaEmpleados;
	private List<String>listaNombreEmpleados=new ArrayList<>();
	private List<Orden> listaOrdenes;
	private List<Archivo>listaArchivos=new ArrayList<>();
	private List<Resumen> listaResumenProveedor;
	private List<Resumen> listaResumenObra;
	private List<Resumen> listaResumenTipo;
	

	
	private Orden ordenSeleccionado;
	private DonutChartModel donutModelProveedores;
	private DonutChartModel donutModelObras;
	private DonutChartModel donutModelTipo;
	private Double totalGasto;


	private DefaultStreamedContent  fileDOWN;
	private Tools tools =new Tools();
	
	private String archivo_e;
	private List<String> listaArchivosCombo;
	private String pdf_to_show;
	private Archivo archivoSeleccionado;

	//Buscar	
	private String tipo_b;
	private Date fecha_inicio_b;
	private Date fecha_final_b;
	private String obra_b;
	private String proveedor_b;
	private String concepto_b;
	
	
	//Editar
	private Date fecha_e;
	private String tipo_e;
	private String concepto_e;
	private String factura_e;
	private Double importe_e;
	private String detalle_e;
	private String proveedor_e;
	private String obra_e;
	private String solicito_e;
	private String autorizo_e;
	private Integer orden_id_e;
	private String orden_tipo_e;


	public void iniciaVista() {
		LOG.info("***************** vwGastos.iniciaVista() ****************");
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

		donutModelProveedores = new DonutChartModel();	
		donutModelObras = new DonutChartModel();
		donutModelTipo = new DonutChartModel();

	}
	
	
	public void generaExcelCSV() {
		
		writeCsvFromBean(carpeta_archivos + "gastos.csv");	
		try {
		    TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException ie) {
		    Thread.currentThread().interrupt();
		}
		/*
		try{
	       Runtime.getRuntime().exec("cmd /c start "+carpeta_archivos + "gastos.csv");
		}catch(IOException  e){
          e.printStackTrace();
		}
		*/
		addMessage("Archivo generado correctamente.","Se generó el archivo: "+carpeta_archivos + "gastos.csv",FacesMessage.SEVERITY_INFO);

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
	
	 
	


	private void FileDownload(String filePath) {

		final File file = new File(filePath);
		String fileName = file.getName();
		try {
		
			final byte[] attachment = Files.readAllBytes(Paths.get(filePath));
			
			final ByteArrayInputStream bais=new ByteArrayInputStream(attachment);
		
			fileDOWN = DefaultStreamedContent.builder()
	                .name(fileName)
	                .contentType("text/csv")
	                .stream(() -> bais)
	                .build();	 
	        
	        bais.close();
	        
	        LOG.info("FileDownload : "+filePath);
	    
		} catch (IOException e) {
			LOG.error("[FileDownload] "+e.getMessage());
		}
		
	}



	private void deleteFiles(String srcFile) throws IOException{
	
	    Files.deleteIfExists(Paths.get(srcFile));
	
	}

	
	
	
	
	
	
	
	
	private void leeConfiguracion() {
		
		Configuracion configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_TRABAJO")).findFirst().orElse(null);
		
		carpeta_trabajo=configuracion.getValor();
		
		configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("RUTA_LOCAL_ARCHIVOS")).findFirst().orElse(null);
		
		carpeta_archivos=configuracion.getValor();
	

	}

	
	private void descargaCatalogos() {
		
		Body body = new Body();
		body.setFilter("GASTOS");		
		listaCatalogoGastos=tools.listadoString("tools/stringList", header, body, 30);
		Collections.sort(listaCatalogoGastos);
		
		body.setFilter("ALL");
		listaObras=tools.listadoObras("obra/filter", header, body, 30);
		if(listaObras!=null && listaObras.size()>0) {
			listaNombreObras=listaObras.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
			Collections.sort(listaNombreObras);
		}
		
		listaProveedores=tools.listadoProveedores("proveedor/filter", header, body, 30);
		if(listaProveedores!=null && listaProveedores.size()>0) {
			listaNombreProveedores=listaProveedores.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
			Collections.sort(listaNombreProveedores);
		}
		
		listaEmpleados=tools.listadoEmpleados("empleado/filter", header, body, 30);
		if(listaEmpleados!=null && listaEmpleados.size()>0) {
			listaNombreEmpleados=listaEmpleados.stream().map(elem -> elem.getNombre()).distinct().collect(Collectors.toList());
			Collections.sort(listaNombreEmpleados);
		}
		
		descargaListaPDF();
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		
		if (proveedor_b!=null && proveedor_b.trim().length()>0) filtro="#PROVEEDOR#";
		if (obra_b!=null && obra_b.trim().length()>0) filtro="#OBRA#";
		if (fecha_inicio_b!=null) {
			filtro=filtro+"#FECHA#";
			if(fecha_final_b!=null) filtro=filtro.replace("#FECHA#","#PERIODO#");
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
		ordenSeleccionado=null;
		listaPrincipal=tools.listadoGastos("gasto/filter", header, body, 30);
		totalGasto = 0d;
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
			ordenSeleccionado=seleccionado.getOrden();
			totalGasto = listaPrincipal.stream().mapToDouble(elem->elem.getImporte()).sum();
		}else {
			listaPrincipal=new ArrayList<>();
		}
		
		
		//EXTRAE EL NOMBRE DE LA OBRA Y SE LO ASIGNA AL STR_OBRA
		listaPrincipal.stream().filter(elem->elem.getObra()!=null).forEach(elem-> elem.setStr_obra(elem.getObra().getNombre()));

		//EXTRAE EL NOMBRE DEL PROVEEDOR Y SE LO ASIGNA AL STR_PROVEEDOR
		listaPrincipal.stream().filter(elem->elem.getProveedor()!=null).forEach(elem-> elem.setStr_proveedor(elem.getProveedor().getNombre()));


		listaPrincipal.stream().forEach(elem-> elem.setStr_fecha(df_ddMMMyyyy.format(elem.getFecha()).replace(".", "")));


		filtro_anterior=body.getFilter();
		
		seleccionarElemento();
	}
	
	
	
	
	public void seleccionarElemento(){
		
		editable=false;
		nuevo=false;
		
		if(seleccionado==null) {
			ordenSeleccionado=null;
			inicializaCapturar();			
			return;
		}
		
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			
			tipo_e=seleccionado.getTipo();			
			fecha_e=seleccionado.getFecha();
			importe_e=seleccionado.getImporte();
			concepto_e=seleccionado.getConcepto();
			detalle_e=seleccionado.getDetalle();
			factura_e=seleccionado.getFactura();
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
			
			ordenSeleccionado=seleccionado.getOrden();
			orden_id_e=null;
			orden_tipo_e=null;
			if(ordenSeleccionado!=null) {
				orden_id_e=ordenSeleccionado.getId();
				orden_tipo_e=ordenSeleccionado.getTipo();
			}
			
			descargaListaArchivos();
			
		}		
	}
	
	
	

	public void inicializaFiltros(Boolean buscar){
		tipo_b=null;
		fecha_inicio_b = null;
		fecha_final_b = null;
		obra_b=null;
		proveedor_b=null;
		concepto_b=null;
		pdf_to_show=carpeta_trabajo + "error.pdf";
		if(buscar) {
			busquedaPrincipal();
		}
	}


	public void inicializaCapturar(){
		tipo_e="";
		concepto_e="";
		fecha_e = Calendar.getInstance().getTime();
		importe_e=0d;
		detalle_e="";
		archivo_e="";
		factura_e="";
		pdf_to_show=carpeta_trabajo+"error.pdf";
		obra_e = "";
		proveedor_e="";
		solicito_e="";
		autorizo_e="";
		ordenSeleccionado=null;
		orden_id_e=null;
		orden_tipo_e=null;
		
		listaArchivos= new ArrayList<Archivo>();
		
	}



	private void asignaValoresRegistro() {
		
		registro_guardar.setTipo(tipo_e);
		registro_guardar.setFecha(fecha_e);
		registro_guardar.setImporte(importe_e);
		registro_guardar.setConcepto(concepto_e);
		registro_guardar.setDetalle(detalle_e);
		registro_guardar.setFactura(factura_e);
		registro_guardar.setTipo_factura("CONTADO");
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
		
		registro_guardar.setOrden(ordenSeleccionado);
	}

	
	
	
	
	public void descargaListaPDF() {
		Body body = new Body();
		body.setFilter("GASTOS_PDF_DISPONIBLES");	
		body.setFilter1(carpeta_archivos);
		listaArchivosCombo=tools.listadoString("tools/stringList", header, body, 30);
	}
	
	
	
	
	
	
	
	public void eliminaArchivo() {
		Body body = new Body();
		body.setArchivo(archivoSeleccionado);
		Respuesta resp = tools.ejecutaRespuesta("archivo/delete", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			listaArchivos.remove(archivoSeleccionado);
			
			listaArchivosCombo.add(archivoSeleccionado.getNombre());
		}
	}
	
	
	
	private void descargaListaArchivos() {
		Body body = new Body();
		body.setFilter("BY_GASTO");	
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
			archivo.setTipo("GASTO");
			if(listaArchivos==null) {
				listaArchivos=new ArrayList<>();
			}
			listaArchivos.add(archivo);
			listaArchivosCombo.remove(archivo.getNombre());	
			archivoSeleccionado=archivo;
		}
	}
	
	
	
	public void accionEliminar(){
		LOG.info("***************** vwGastos.accionEliminar() ****************");
		Body body = new Body();
		body.setGasto(seleccionado);
		Respuesta resp = tools.ejecutaRespuesta("gasto/delete", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			addMessage("Registro ha sido eliminado","El registro se elimino correctamente.", FacesMessage.SEVERITY_INFO);			
			inicializaFiltros(true);
		}else {
			addMessage("Error al eliminar el registro","Elimine archivos relacionados al registro", FacesMessage.SEVERITY_WARN);
		}
	}
	
	
	
	public void accionAgregar(){
		
		inicializaCapturar();
		
		editable=true;
		
		nuevo=true;
	}
	
	
	public void accionModificar(){			
		
		if( listaArchivosCombo==null ) {
			listaArchivosCombo= new ArrayList<String>();
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
		LOG.info("***************** vwGastos.accionGuardar() ****************");
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			PrimeFaces.current().ajax().update(":formaID:mensajeID");
			return;
		}
		registro_guardar = new Gasto();//SI ES NUEVO SE CREA UN NUEVO REGISTRO 
		if(!nuevo) {			
			registro_guardar=seleccionado;//SI NO ES NUEVO SE ACTUALIZA EL REGISTRO CON EL ID SELECCIONADO
		}
		//SE ASIGNAN VALORES AL REGISTRO YA SEA NUEVO O NO 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setGasto(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("gasto/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getGasto().getId();
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
	
	
	public void llenaListaOrdenes(){		
		Body body = new Body();
		body.setFilter("BY_GASTO_NULL");
		listaOrdenes=tools.listadoOrdenes("orden/filter", header, body, 30);
	}	
	
	
	
	private void actualizaArchivos(Gasto gasto) {
		if(listaArchivos!=null && listaArchivos.size()>0) {
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
	
	
	
	
	public void seleccionarOrden(){
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('dialogOrdenesVar').hide();");
		
		orden_id_e=ordenSeleccionado.getId();
		orden_tipo_e=ordenSeleccionado.getTipo();
		
		tipo_e=ordenSeleccionado.getTipo();
		fecha_e=ordenSeleccionado.getFecha();
		importe_e=ordenSeleccionado.getImporte();
		concepto_e=ordenSeleccionado.getConcepto();
		
		
		proveedor_e="";
		if(ordenSeleccionado.getProveedor()!=null) {
			proveedor_e=ordenSeleccionado.getProveedor().getNombre();
		}
		obra_e="";
		if(ordenSeleccionado.getObra()!=null) {
			obra_e=ordenSeleccionado.getObra().getNombre();
		}
		solicito_e="";
		if(ordenSeleccionado.getSolicito()!=null) {
			solicito_e=ordenSeleccionado.getSolicito().getNombre();
		}
		autorizo_e="";
		if(ordenSeleccionado.getAutorizo()!=null) {
			autorizo_e=ordenSeleccionado.getAutorizo().getNombre();
		}
		archivo_e="";
		
	}
	
	
	
	public void accionResumen(){		
		
		createDonutModelProveedores();
		
		createDonutModelObras();
		
		//createDonutModelTipo();
		
		preparaListasResumen();
	}	
	
	
	private void preparaListasResumen() {
		Resumen resumen;
		Double importe;
		listaResumenProveedor=new ArrayList<>();
		List<String> proveedores = listaPrincipal.stream().filter(elem-> elem.getProveedor()!=null).map(elem -> elem.getProveedor().getNombre()).distinct().collect(Collectors.toList()); 
        for(String proveedor : proveedores) {
        	importe= listaPrincipal.stream().filter(elem-> elem.getProveedor()!=null).filter(elem-> elem.getProveedor().getNombre().equals(proveedor)).mapToDouble(elem->elem.getImporte()).sum();
         	resumen = new Resumen();
        	resumen.setConcepto(proveedor);        	
        	resumen.setImporte(importe);
        	listaResumenProveedor.add(resumen);
        }
        importe= listaPrincipal.stream().filter(elem-> elem.getProveedor()==null || elem.getProveedor().getId()==null).mapToDouble(elem->elem.getImporte()).sum();
        resumen = new Resumen();
    	resumen.setConcepto("Sin Proveedor");        	
    	resumen.setImporte(importe);
    	listaResumenProveedor.add(resumen);
        
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
	
	
	
	private void createDonutModelTipo() {
		ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Random rand = new Random();
        Double valor;
        labels = listaPrincipal.stream().filter(elem-> elem.getTipo()!=null && !elem.getTipo().equals("")).map(elem -> elem.getTipo()).distinct().collect(Collectors.toList()); 
        for(String label:labels) {
        	valor = listaPrincipal.stream().filter(elem-> elem.getTipo()!=null).filter(elem-> elem.getTipo().equals(label)).mapToDouble(elem->elem.getImporte()).sum();
        	values.add(valor);
        	String color=tools.regresaColor(rand.nextInt((138 - 1) + 1) + 1);
        	colors.add(color);
        }
        valor = listaPrincipal.stream().filter(elem-> elem.getTipo()==null || elem.getTipo().equals("")).mapToDouble(elem->elem.getImporte()).sum();
        if(valor > 0d) {
        	values.add(valor);
        	labels.add("Sin Tipo");
        }
        DonutChartOptions options = new DonutChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        donutModelTipo.setOptions(options);
        
        dataSet.setData(values);
        dataSet.setBackgroundColor(colors);
        data.setLabels(labels);
        data.addChartDataSet(dataSet);
        donutModelTipo.setData(data);          
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
	
	
	private void createDonutModelProveedores() {
		ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Random rand = new Random();
        Double valor;
        labels = listaPrincipal.stream().filter(elem-> elem.getProveedor()!=null).map(elem -> elem.getProveedor().getNombre()).distinct().collect(Collectors.toList()); 
        Collections.sort(labels);
        for(String label:labels) {
        	valor = listaPrincipal.stream().filter(elem-> elem.getProveedor()!=null).filter(elem-> elem.getProveedor().getNombre().equals(label)).mapToDouble(elem->elem.getImporte()).sum();
        	values.add(valor);
        	String color=tools.regresaColor(rand.nextInt((138 - 1) + 1) + 1);
        	colors.add(color);
        }
        valor = listaPrincipal.stream().filter(elem-> elem.getProveedor()==null || elem.getProveedor().getId()==null).mapToDouble(elem->elem.getImporte()).sum();
        if(valor > 0d) {
        	values.add(valor);
        	labels.add("Sin Proveedor");
        }
        DonutChartOptions options = new DonutChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        donutModelProveedores.setOptions(options);
        
        dataSet.setData(values);
        dataSet.setBackgroundColor(colors);
        data.setLabels(labels);
        data.addChartDataSet(dataSet);
        donutModelProveedores.setData(data);          
	}	
	
	
	
	private String resultadoValidaGuardado() {
		String result="FECHA";
		if (fecha_e==null || fecha_e.equals("")) return result;

		//if (tipo_e==null || tipo_e.equals("")) return result;
		//result="PROVEEDOR";
		//if (proveedor_e==null || proveedor_e.equals("")) return result;
		//result="SOLICITO";
		//if (solicito_e==null || solicito_e.equals("")) return result;
		//result="IMPORTE";
		//if (importe_e==null || importe_e<=0d) return result;
		return "";
	}
	
	
	
    
	public void redirectCuentasPagar() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("cuentasPagar.xhtml");
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






	public List<Gasto> getListaPrincipal() {
		return listaPrincipal;
	}



	public void setListaPrincipal(List<Gasto> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}



	public Gasto getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(Gasto seleccionado) {
		this.seleccionado = seleccionado;
	}



	public Date getFecha_e() {
		return fecha_e;
	}



	public void setFecha_e(Date fecha_e) {
		this.fecha_e = fecha_e;
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



	public List<Orden> getListaOrdenes() {
		return listaOrdenes;
	}



	public void setListaOrdenes(List<Orden> listaOrdenes) {
		this.listaOrdenes = listaOrdenes;
	}



	public Orden getOrdenSeleccionado() {
		return ordenSeleccionado;
	}



	public void setOrdenSeleccionado(Orden ordenSeleccionado) {
		this.ordenSeleccionado = ordenSeleccionado;
	}



	public Integer getOrden_id_e() {
		return orden_id_e;
	}



	public void setOrden_id_e(Integer orden_id_e) {
		this.orden_id_e = orden_id_e;
	}



	public String getOrden_tipo_e() {
		return orden_tipo_e;
	}



	public void setOrden_tipo_e(String orden_tipo_e) {
		this.orden_tipo_e = orden_tipo_e;
	}

	public DonutChartModel getDonutModelProveedores() {
		return donutModelProveedores;
	}

	public void setDonutModelProveedores(DonutChartModel donutModelProveedores) {
		this.donutModelProveedores = donutModelProveedores;
	}

	public DonutChartModel getDonutModelObras() {
		return donutModelObras;
	}

	public void setDonutModelObras(DonutChartModel donutModelObras) {
		this.donutModelObras = donutModelObras;
	}








	public List<String> getListaArchivosPDF() {
		return listaArchivosCombo;
	}







	public void setListaArchivosPDF(List<String> listaArchivosPDF) {
		this.listaArchivosCombo = listaArchivosPDF;
	}


	public Double getImporte_e() {
		return importe_e;
	}



	public Double getTotalGasto() {
		return totalGasto;
	}



	public void setTotalGasto(Double totalGasto) {
		this.totalGasto = totalGasto;
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



	public DonutChartModel getDonutModelTipo() {
		return donutModelTipo;
	}



	public void setDonutModelTipo(DonutChartModel donutModelTipo) {
		this.donutModelTipo = donutModelTipo;
	}



	public Boolean getNuevo() {
		return nuevo;
	}



	public void setNuevo(Boolean nuevo) {
		this.nuevo = nuevo;
	}


	public DefaultStreamedContent getFileDOWN() {
		return fileDOWN;
	}


	public void setFileDOWN(DefaultStreamedContent fileDOWN) {
		this.fileDOWN = fileDOWN;
	}


	public List<String> getListaArchivosCombo() {
		return listaArchivosCombo;
	}


	public void setListaArchivosCombo(List<String> listaArchivosCombo) {
		this.listaArchivosCombo = listaArchivosCombo;
	}




	public String getCarpeta_archivos() {
		return carpeta_archivos;
	}




	public void setCarpeta_archivos(String carpeta_archivos) {
		this.carpeta_archivos = carpeta_archivos;
	}




	public String getFactura_e() {
		return factura_e;
	}




	public void setFactura_e(String factura_e) {
		this.factura_e = factura_e;
	}




	public List<Resumen> getListaResumenTipo() {
		return listaResumenTipo;
	}




	public void setListaResumenTipo(List<Resumen> listaResumenTipo) {
		this.listaResumenTipo = listaResumenTipo;
	}




	public List<Resumen> getListaResumenProveedor() {
		return listaResumenProveedor;
	}




	public void setListaResumenProveedor(List<Resumen> listaResumenProveedor) {
		this.listaResumenProveedor = listaResumenProveedor;
	}




	public List<Resumen> getListaResumenObra() {
		return listaResumenObra;
	}




	public void setListaResumenObra(List<Resumen> listaResumenObra) {
		this.listaResumenObra = listaResumenObra;
	}




}
