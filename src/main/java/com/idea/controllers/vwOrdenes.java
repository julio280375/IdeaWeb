package com.idea.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.idea.Tools;
import com.idea.objects.business.Orden;
import com.idea.objects.business.Proveedor;
import com.idea.objects.business.Hash;
import com.idea.objects.business.Obra;
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
@ManagedBean(name = "vwOrdenes")
@SessionScoped
@RequestScoped
public class vwOrdenes  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwOrdenes.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	private DateFormat df_ddMMMyyyy= new SimpleDateFormat("dd-MMM-yyyy");
	private DateFormat df_yyyyMMdd= new SimpleDateFormat("yyyy-MM-dd");	
	private Header header;
	private List<Orden> listaPrincipal;
	private Orden seleccionado;
	private Boolean editable;
	private Boolean nuevo;
	private String filtro_anterior;
	private Orden registro_guardar;
	private String carpeta_archivos;
	private String carpeta_trabajo;
	private List<String>listaCatalogoGastos=new ArrayList<>();
	private List<Obra>listaObras;
	private List<String>listaNombreObras=new ArrayList<>();
	private List<Proveedor>listaProveedores;
	private List<String>listaNombreProveedores=new ArrayList<>();
	private List<Empleado>listaEmpleados;
	private List<String>listaNombreEmpleados=new ArrayList<>();
	private String pdf_to_show;
	private UploadedFile file;
	private StreamedContent fileDOWN;
	private Tools tools =new Tools();
	private List<String> listaArchivosPDF;

	//Buscar	
	private Date fecha_inicio_b;
	private Date fecha_final_b;
	private String obra_b;
	private String proveedor_b;
	
	
	//Editar
	private String tipo_e;
	private Date fecha_e;
	private Double importe_e;
	private String concepto_e;
	private String archivo_e;
	private String obra_e;
	private String proveedor_e;
	private String solicito_e;
	private String autorizo_e;
	
	
	

	public void iniciaVista() {
		LOG.info("**************** vwOrdenes.iniciaVista() ****************");
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
			if(seleccionado.getObra()!=null) {
				obra_e=seleccionado.getObra().getNombre();
			}
			if(seleccionado.getProveedor()!=null) {
				proveedor_e=seleccionado.getProveedor().getNombre();
			}
			if(seleccionado.getSolicito()!=null) {
				solicito_e=seleccionado.getSolicito().getNombre();
			}
			if(seleccionado.getAutorizo()!=null) {
				autorizo_e=seleccionado.getAutorizo().getNombre();
			}
			archivo_e=seleccionado.getArchivo(); 
			pdf_to_show=carpeta_trabajo+"error.pdf";
			if(archivo_e!=null && !archivo_e.equals("")){
				pdf_to_show=carpeta_archivos+archivo_e;
			}
		}		
	}
	

	
	private void asignaValoresRegistro() {
		registro_guardar.setTipo(tipo_e);
		registro_guardar.setFecha(fecha_e);
		registro_guardar.setImporte(importe_e);
		registro_guardar.setConcepto(concepto_e);
		registro_guardar.setArchivo(archivo_e);
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
	
	
	public void inicializaFiltros(Boolean buscar){
		fecha_inicio_b = null;
		fecha_final_b = null;
		obra_b=null;
		proveedor_b=null;
		if(buscar) {
			busquedaPrincipal();
		}
	}
	
	
	public void inicializaCapturar(){
		tipo_e="";
		importe_e=0d;
		concepto_e="";
		fecha_e = Calendar.getInstance().getTime();
		archivo_e="";
		pdf_to_show=carpeta_trabajo+"error.pdf";
		
		obra_e = "";
		proveedor_e="";
		solicito_e="";
		autorizo_e="";
		
	}
	
	
	
	public void accionAgregar(){
		
		inicializaCapturar();
		
		editable=true;
		
		nuevo=true;
	}
	
	
	public void accionModificar(){			
		
		editable=true;		
		nuevo=false;
	}	
	
	
	public void accionCancelar(){
			
		seleccionarElemento();
			
		editable=false;
		nuevo=false;
	}
	

	
	public void accionGuardar(){
		String strValida=resultadoValidaGuardado();
		if (!strValida.equals("")) {
			addMessage("Error al guardar, registro incompleto", "Capturar información faltante "+strValida, FacesMessage.SEVERITY_WARN);
			return;
		}
		registro_guardar = new Orden();
		if(!nuevo) {
			registro_guardar=seleccionado;
		}
		 
		asignaValoresRegistro();
		
		Body body = new Body();
		body.setOrden(registro_guardar);
		Respuesta resp = tools.ejecutaRespuesta("orden/save", header, body, 30);
		if(resp!=null && resp.getCode()==200) {
			if(nuevo) {				
				body = gSon.fromJson(resp.getData(), Body.class);
				Integer id=body.getOrden().getId();
				registro_guardar.setId(id);
				if(listaPrincipal==null) listaPrincipal=new ArrayList<>();
				listaPrincipal.add(registro_guardar);
				seleccionado=listaPrincipal.stream().filter(elem->elem.getId().equals(id)).findFirst().orElse(null);
			}
			addMessage("Registros guardados correctamente.","Se guardó la información del elemento.",FacesMessage.SEVERITY_INFO);
			
			descargaListaPDF();
			
			seleccionarElemento();			
		}else {
			addMessage("Error al guardar información.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
		}
		editable=false;
		nuevo=false;
	}
	
	
	public void accionResumen(){			


	}	
	
	
	public void descargaListaPDF() {
		Body body = new Body();
		body.setFilter("ORDENES_PDF_DISPONIBLES");	
		String pathArchivos=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp";
		body.setFilter1(pathArchivos+carpeta_archivos);
		listaArchivosPDF=tools.listadoString("tools/stringList", header, body, 30);
	}
	
	
	
	public void busquedaPrincipal() {

		String filtro="";
		if (proveedor_b!=null && proveedor_b.trim().length()>0) filtro="#PROVEEDOR#";
		if (obra_b!=null && obra_b.trim().length()>0) filtro="#OBRA#";
		if (fecha_inicio_b!=null) {
			filtro=filtro+"#FECHA#";
			if(fecha_final_b!=null) filtro=filtro.replace("#FECHA#","#PERIODO#");
		}
		
		
		Body body = new Body();
		switch(filtro) {
		case "":
			body.setFilter("BY_GASTO_NULL");
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
		
		if(filtro_anterior.equals("BY_GASTO_NULL")) {
			seleccionado=listaPrincipal.get(0);
			seleccionarElemento();
		}
	
		seleccionado=null;
		
		listaPrincipal=tools.listadoOrdenes("orden/filter", header, body, 30);
		if(listaPrincipal!=null && listaPrincipal.size()>0) {
			seleccionado=listaPrincipal.get(0);
		}
		filtro_anterior=body.getFilter();
		seleccionarElemento();
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
	
	public void accionDescargarPDF(){
		Body body = new Body();
		body.setNombreArchivo("Recibo-Ago.pdf");
		Respuesta respuesta= tools.descargaRespuesta("archivos/download", header, body, 30);
		if(respuesta.getCode().equals(0)) {
			
		}
	}
	
	
	
	
	private String resultadoValidaGuardado() {
		String result="TIPO";
		if (tipo_e==null || tipo_e.equals("")) return result;
		//result="OBRA";
		//if (obra_e==null || obra_e.equals("")) return result;
		//result="PROVEEDOR";
		//if (proveedor_e==null || proveedor_e.equals("")) return result;
		result="SOLICITO";
		if (solicito_e==null || solicito_e.equals("")) return result;
		result="IMPORTE";
		if (importe_e==null || importe_e<=0d) return result;
		return "";
		
	}
	
	
	
	
	public void upload() {
        if (file != null) {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
     

     
    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	
	
	
	public StreamedContent descargarFilterFiles() {
			
			Configuracion configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_TRABAJO")).findFirst().orElse(null);
			
			String ruta_nombre_archivo=configuracion.getValor()+ "ordenes.csv";
			
			writeCsvFromBean(ruta_nombre_archivo);				
		
			try {
				
				FileDownload(ruta_nombre_archivo);
				
				deleteFiles(ruta_nombre_archivo);
				
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}		
				
			addMessage("Archivos generados correctamente.","Se generaron los archivos de dispersion del dia.",FacesMessage.SEVERITY_INFO);
		
			return fileDOWN;
		
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
        
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


	
	private void deleteFiles(String srcFile) throws IOException{

        //Files.deleteIfExists(Paths.get(srcFile));
  
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeCsvFromBean(String path)  {
		try {
			if (listaPrincipal.size()>0) {					
			    Writer writer  = new FileWriter(path.toString());
			    StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
			       .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
			       .withIgnoreField(Orden.class, Orden.class.getDeclaredField("archivo"))
			       .withIgnoreField(Orden.class, Orden.class.getDeclaredField("obra"))
			       .withOrderedResults(true)
			       .build();
			    sbc.setOrderedResults(true);
			    sbc.write(listaPrincipal);
			    writer.close();
			   
			    LOG.info(path+" Guardado correctamente!");
			}
	    } catch (Exception e) { 
	    	LOG.info("[writeCsvFromBean] "+e.getMessage());
		}
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






	public List<Orden> getListaPrincipal() {
		return listaPrincipal;
	}



	public void setListaPrincipal(List<Orden> listaPrincipal) {
		this.listaPrincipal = listaPrincipal;
	}



	public Orden getSeleccionado() {
		return seleccionado;
	}



	public void setSeleccionado(Orden seleccionado) {
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





	public Double getImporte_e() {
		return importe_e;
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



	public String getArchivo_e() {
		return archivo_e;
	}



	public void setArchivo_e(String archivo_e) {
		this.archivo_e = archivo_e;
	}






	public StreamedContent getFileDOWN() {
		return fileDOWN;
	}


	public void setFileDOWN(StreamedContent fileDOWN) {
		this.fileDOWN = fileDOWN;
	}



	public UploadedFile getFile() {
		return file;
	}



	public void setFile(UploadedFile file) {
		this.file = file;
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


	public List<String> getListaGastos() {
		return listaCatalogoGastos;
	}


	public void setListaGastos(List<String> listaGastos) {
		this.listaCatalogoGastos = listaGastos;
	}





	public List<String> getListaNombreEmpleados() {
		return listaNombreEmpleados;
	}


	public void setListaNombreEmpleados(List<String> listaNombreEmpleados) {
		this.listaNombreEmpleados = listaNombreEmpleados;
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


	public List<String> getListaCatalogoGastos() {
		return listaCatalogoGastos;
	}


	public void setListaCatalogoGastos(List<String> listaCatalogoGastos) {
		this.listaCatalogoGastos = listaCatalogoGastos;
	}


	public List<String> getListaArchivosPDF() {
		return listaArchivosPDF;
	}


	public void setListaArchivosPDF(List<String> listaArchivosPDF) {
		this.listaArchivosPDF = listaArchivosPDF;
	}




}
