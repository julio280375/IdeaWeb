package com.idea.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.idea.Tools;
import com.idea.objects.business.Gasto;
import com.idea.objects.business.Hash;
import com.idea.objects.business.Ingreso;
import com.idea.objects.business.MovimientoBanorteExcel;
import com.idea.objects.system.Body;
import com.idea.objects.system.Configuracion;
import com.idea.objects.system.Header;
import com.idea.objects.system.Respuesta;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;




@SuppressWarnings("deprecation")
@Component
@ManagedBean(name = "vwMovimientos")
@SessionScoped
public class vwMovimientos  {

	transient HttpSession session;
	private static Logger LOG =  LoggerFactory.getLogger(vwMovimientos.class);
	private NumberFormat currencyFormat = NumberFormat.getInstance(Locale.US);
	DateFormat formatddMMyyyy= new SimpleDateFormat("dd/MM/yyyy");
	private Header header;
	private ArrayList<String> listaBancos = new ArrayList<String>(Arrays.asList("BANORTE"));
	private List<String> listaArchivos;
	private String banco;
	private String archivo;
	private String carpeta_movimientos;
	private Tools tools =new Tools();
	private List<MovimientoBanorteExcel>listaGastosBanorte;
	private List<MovimientoBanorteExcel>listaIngresosBanorte;
	private List<Gasto> listaGastos;
	private List<Ingreso> listaIngresos;
	private MovimientoBanorteExcel gastoBanorteSeleccionado;
	private MovimientoBanorteExcel ingresoBanorteSeleccionado;
	private Double totalGastos,totalIngresos;
	
	
	public void iniciaVista() {
		LOG.info("**************** vwMovimientos.iniciaVista() ****************");
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
		
		descargaListaArchivos();
	}
	
	
	
	private void descargaListaArchivos() {
		Body body = new Body();
		body.setFilter("ARCHIVOS_MOVIMIENTOS");	
		body.setFilter1(carpeta_movimientos);
		listaArchivos=tools.listadoString("tools/stringList", header, body, 30);
	}
	
	
	public void llenaListaMovimientos(){		
		Boolean archivoLeido=false;
		switch(banco) {
		case "BANORTE":
			archivoLeido=readFileBanorte(carpeta_movimientos+archivo);
			break;
		}
		if (!archivoLeido) {
			addMessage("Leer Archvo","Error al leer archivo de movimientos.", FacesMessage.SEVERITY_WARN);
		}
	}	
	
	
	
	public void limpiaListaMovimientos(){		
		listaIngresosBanorte= new ArrayList<>();
		listaGastosBanorte= new ArrayList<>();
	}
	
	
	
	public void importaGastos(){		

		switch(banco) {
			case "BANORTE" : EnviaGastosBanorte();								
			break;
		}

	}	
	
	public void importaIngresos(){		

		switch(banco) {
			case "BANORTE" : EnviaIngresosBanorte();								
			break;
		}

	}	
	
	
	
public void EnviaGastosBanorte() {
		
		if(listaGastosBanorte.size()>0) {
			List<MovimientoBanorteExcel> listaMovimientosExcel = new ArrayList<>();
			Body body = new Body();	 
			body.setFilter("MOVIMIENTO_GASTOS_BANORTE");
			List<Hash> listaHashMovimientos=tools.listadoHash("tools/hashList",header, body, 60);
			if(listaHashMovimientos!=null && listaHashMovimientos.size()>0) {
				listaMovimientosExcel = listaGastosBanorte.stream().filter(firstList -> listaHashMovimientos.stream().noneMatch(secondList -> firstList.getMovimiento().equals(Long.valueOf(secondList.getDescripcion())))).collect(Collectors.toList());		 			
			}else {
				listaMovimientosExcel = listaGastosBanorte;
			}
			
			if(listaMovimientosExcel!=null && listaMovimientosExcel.size()>0) {
				listaGastos = new ArrayList<>();
				for(MovimientoBanorteExcel movimiento : listaMovimientosExcel) {								
					try {
						Gasto gasto = new Gasto();
						gasto.setMovimiento(movimiento.getMovimiento());
						gasto.setFecha(formatddMMyyyy.parse(movimiento.getFecha()));
						gasto.setAutorizo(header.getEmpleado());
						gasto.setSolicito(header.getEmpleado());
						gasto.setImporte(Double.valueOf(movimiento.getRetiro().replace("$", "").replace(",", "")));
						gasto.setDetalle(movimiento.getDetalle());
						gasto.setConcepto(movimiento.getDescripcion());
						listaGastos.add(gasto);
					} catch (ParseException e) {
						e.printStackTrace();
					}						
				}
				body = new Body();
				body.setListaGastos(listaGastos);
				Respuesta resp = tools.ejecutaRespuesta("gasto/saveAll", header, body, 30);
				if(resp!=null && resp.getCode()==200) {
					addMessage("Movimientos guardados correctamente.","Se procesaron "+String.valueOf(listaGastos.size())+" movimientos.",FacesMessage.SEVERITY_INFO);
				}else {
					addMessage("Error al guardar movimientos.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
				}
			}else {
				addMessage("Error al guardar movimientos.","No hay movimientos nuevos por agregar", FacesMessage.SEVERITY_WARN);
			}
		}
	}	
	
	
	
	
	
	
	public void EnviaIngresosBanorte() {
		
		if(listaIngresosBanorte.size()>0) {
			List<MovimientoBanorteExcel> listaMovimientosExcel = new ArrayList<>();
			Body body = new Body();	 
			body.setFilter("MOVIMIENTO_INGRESOS_BANORTE");
			List<Hash> listaHashMovimientos=tools.listadoHash("tools/hashList",header, body, 60);
			if(listaHashMovimientos!=null && listaHashMovimientos.size()>0) {
				listaMovimientosExcel = listaIngresosBanorte.stream().filter(firstList -> listaHashMovimientos.stream().noneMatch(secondList -> firstList.getMovimiento().equals(Long.valueOf(secondList.getDescripcion())))).collect(Collectors.toList());		 			
			}else {
				listaMovimientosExcel = listaIngresosBanorte;
			}
			
			if(listaMovimientosExcel!=null && listaMovimientosExcel.size()>0) {
				listaIngresos = new ArrayList<>();
				for(MovimientoBanorteExcel movimiento : listaMovimientosExcel) {								
					try {
						Ingreso ingreso = new Ingreso();
						ingreso.setMovimiento(movimiento.getMovimiento());
						ingreso.setFecha(formatddMMyyyy.parse(movimiento.getFecha()));
						ingreso.setImporte(Double.valueOf(movimiento.getDeposito().replace("$", "").replace(",", "")));
						ingreso.setDetalle(movimiento.getDetalle());
						ingreso.setConcepto(movimiento.getDescripcion());
						listaIngresos.add(ingreso);
					} catch (ParseException e) {
						e.printStackTrace();
					}						
				}
				body = new Body();
				body.setListaIngresos(listaIngresos);
				Respuesta resp = tools.ejecutaRespuesta("ingreso/saveAll", header, body, 30);
				if(resp!=null && resp.getCode()==200) {
					addMessage("Movimientos guardados correctamente.","Se procesaron "+String.valueOf(listaIngresos.size())+" movimientos.",FacesMessage.SEVERITY_INFO);
				}else {
					addMessage("Error al guardar movimientos.","Respuesta servidor: "+resp.getMessage(), FacesMessage.SEVERITY_WARN);
				}
			}else {
				addMessage("Error al guardar movimientos.","No hay movimientos nuevos por agregar", FacesMessage.SEVERITY_WARN);
			}
		}
	}	
	
	
	
	private Boolean readFileBanorte(String path) {
		try{				
			InputStreamReader input =new InputStreamReader(new FileInputStream(path), "UTF-8");
			CSVReader reader=new CSVReader(input);
			CsvToBean<MovimientoBanorteExcel> csvToBean = new CsvToBean<MovimientoBanorteExcel>();
			Map<String, String> columnMapping = new HashMap<String, String>();
			columnMapping.put("MOVIMIENTO", "movimiento");
			columnMapping.put("RETIROS", "retiro");
			columnMapping.put("DEPÓSITOS", "deposito");
			columnMapping.put("FECHA", "fecha");
			columnMapping.put("DESCRIPCIÓN","descripcion");
			columnMapping.put("DESCRIPCIÓN DETALLADA","detalle");
			//columnMapping.put("DEP�SITOS", "deposito");
			//columnMapping.put("DESCRIPCI�N","descripcion");
			//columnMapping.put("DESCRIPCI�N DETALLADA","detalle");
			HeaderColumnNameTranslateMappingStrategy<MovimientoBanorteExcel> strategy = new HeaderColumnNameTranslateMappingStrategy<MovimientoBanorteExcel>();
			strategy.setType(MovimientoBanorteExcel.class);
			strategy.setColumnMapping(columnMapping);
			csvToBean.setMappingStrategy(strategy);
			csvToBean.setCsvReader(reader);
			List<MovimientoBanorteExcel> listaMovimientosBanorte = csvToBean.parse();
			input.close();
			listaIngresosBanorte=listaMovimientosBanorte.stream().filter(elem -> elem.getRetiro()==null||elem.getRetiro().equals("-")).collect(Collectors.toList());
			listaGastosBanorte=listaMovimientosBanorte.stream().filter(elem -> elem.getDeposito()==null||elem.getDeposito().equals("-")).collect(Collectors.toList());
			
			this.totalIngresos=listaIngresosBanorte.stream().mapToDouble(elem->Double.parseDouble(elem.getDeposito().replace("$", "").replace(",", ""))).sum();
			
			totalGastos=listaGastosBanorte.stream().mapToDouble(elem->Double.parseDouble(elem.getRetiro().replace("$", "").replace(",", ""))).sum();
			
			
		}catch(Exception e) {		
			LOG.error("Error de lectura en archivo, "+e.getMessage());
			
			return false;
		}					
		return true;
	}
	
	
	
	
	
	private void leeConfiguracion() {
		
		Configuracion configuracion = header.getConfiguracion().stream().filter(elem->elem.getConcepto().equals("FOLDER_MOVIMIENTOS_BANCO")).findFirst().orElse(null);
		
		String valor =configuracion.getValor();
		
		carpeta_movimientos=System.getProperty("user.dir").replace("\\", "/")+"/src/main/webapp"+valor;
		
	}
	
	
	
	public void addMessage(String msgPrimary, String msgSecundary, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();		
	    context.addMessage(null, new FacesMessage(severity, msgPrimary, msgSecundary));
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
	
	
	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}



	public List<String> getListaArchivos() {
		return listaArchivos;
	}



	public void setListaArchivos(List<String> listaArchivos) {
		this.listaArchivos = listaArchivos;
	}



	public ArrayList<String> getListaBancos() {
		return listaBancos;
	}



	public void setListaBancos(ArrayList<String> listaBancos) {
		this.listaBancos = listaBancos;
	}



	public String getBanco() {
		return banco;
	}



	public void setBanco(String banco) {
		this.banco = banco;
	}



	public String getArchivo() {
		return archivo;
	}



	public void setArchivo(String archivo) {
		this.archivo = archivo;
	}



	public List<MovimientoBanorteExcel> getListaGastosBanorte() {
		return listaGastosBanorte;
	}



	public void setListaGastosBanorte(List<MovimientoBanorteExcel> listaGastosBanorte) {
		this.listaGastosBanorte = listaGastosBanorte;
	}



	public List<MovimientoBanorteExcel> getListaIngresosBanorte() {
		return listaIngresosBanorte;
	}



	public void setListaIngresosBanorte(List<MovimientoBanorteExcel> listaIngresosBanorte) {
		this.listaIngresosBanorte = listaIngresosBanorte;
	}



	public Double getTotalGastos() {
		return totalGastos;
	}



	public void setTotalGastos(Double totalGastos) {
		this.totalGastos = totalGastos;
	}



	public Double getTotalIngresos() {
		return totalIngresos;
	}



	public void setTotalIngresos(Double totalIngresos) {
		this.totalIngresos = totalIngresos;
	}



	public MovimientoBanorteExcel getIngresoBanorteSeleccionado() {
		return ingresoBanorteSeleccionado;
	}



	public void setIngresoBanorteSeleccionado(MovimientoBanorteExcel ingresoBanorteSeleccionado) {
		this.ingresoBanorteSeleccionado = ingresoBanorteSeleccionado;
	}



	public MovimientoBanorteExcel getGastoBanorteSeleccionado() {
		return gastoBanorteSeleccionado;
	}



	public void setGastoBanorteSeleccionado(MovimientoBanorteExcel gastoBanorteSeleccionado) {
		this.gastoBanorteSeleccionado = gastoBanorteSeleccionado;
	}








}
