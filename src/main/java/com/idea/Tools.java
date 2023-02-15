package com.idea;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.idea.controllers.vwGastos;
import com.idea.objects.business.Archivo;
import com.idea.objects.business.CuentaCobrar;
import com.idea.objects.business.CuentaPagar;
import com.idea.objects.business.Gasto;
import com.idea.objects.business.Hash;
import com.idea.objects.business.Ingreso;
import com.idea.objects.business.Obra;
import com.idea.objects.business.Orden;
import com.idea.objects.business.Proveedor;
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

public class Tools {
	
	private String mickeyIDEA = "1338CS1338CS1338";
	private static Logger LOG =  LoggerFactory.getLogger(Tools.class);
	private Gson gSon= new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	
	
	
	public List<CuentaCobrar> listadoCuentasCobrar(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();		
		List<CuentaCobrar> lista = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<CuentaCobrar>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	
	public List<Ingreso> listadoIngresos(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Ingreso> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Ingreso>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	
	public List<CuentaPagar> listadoCuentasPagar(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();		
		List<CuentaPagar> lista = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<CuentaPagar>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	
	public List<Archivo> listadoArchivos(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Archivo> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
				
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Archivo>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	
	public List<Orden> listadoOrdenes(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();		
				
		List<Orden> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
				
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Orden>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}

	
	public List<Proveedor> listadoProveedores(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Proveedor> lista = null;
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
				
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Proveedor>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}





	public List<Configuracion> listadoConfiguracion(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		String jsonResponse;
		Respuesta resp = new Respuesta();				
		List<Configuracion> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
		
		MediaType mediaType = MediaType.parse("application/json");
		
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Configuracion>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	public  Respuesta descargaRespuesta(String servicio, Header header, Body body, Integer TimeMinutes){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();						
		OkHttpClient client = new OkHttpClient().newBuilder()
			.connectTimeout(TimeMinutes, TimeUnit.MINUTES) 
			.writeTimeout(TimeMinutes, TimeUnit.MINUTES) 
			.readTimeout(TimeMinutes, TimeUnit.MINUTES) 
			.build();		
		
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();
	    try{    	
	    	Response response = client.newCall(request).execute();
	    	if(response.code()!=200) {
	    		resp.setCode(101);
	    		resp.setMessage("ERROR AL DESCARGAR EL ARCHIVO");
	    	}else {
	    		InputStream is = response.body().byteStream();
	    		BufferedInputStream input = new BufferedInputStream(is);	    		
				OutputStream output = new FileOutputStream(body.getNombreArchivo());				
				byte[] data = new byte[1024];
				int count;
				long total = 0;				
				while ((count = input.read(data)) != -1) {
				    total += count;
				    output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				resp.setCode(0);
	    		resp.setMessage("OK");
	    	}
	    }catch(Exception ex){	    	
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL CONECTARSE AL SERVIDOR");
	    }	    
		return resp;
	}
	
	
	public StreamedContent descargaStreamed(String servicio, Header header, Body body, Integer TimeSeconds) {
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();					
		StreamedContent archivo = null;					
	
			
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
		
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();					
			
		try {
			Response response = client.newCall(request).execute();
			ByteArrayOutputStream auxOut = new ByteArrayOutputStream();
			IOUtils.copy(response.body().byteStream(), auxOut);
			response.body().close();

			InputStream in = new ByteArrayInputStream(auxOut.toByteArray());
			auxOut.close();
			
			archivo = new DefaultStreamedContent(in, "application/pdf", body.getNombreArchivo());

		} catch (IOException e) {
			System.out.println(e.getMessage());
			
		}
		return archivo;
	}
	

	public List<Empleado> listadoEmpleados(String servicio,Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();						
		List<Empleado> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
				
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Empleado>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}

	
	
	public List<Gasto> listadoGastos(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Gasto> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Gasto>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	


	public List<Hash> listadoHash(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Hash> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Hash>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	public List<String> listadoString(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<String> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	

		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<String>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}
	
	
	public  Respuesta ejecutaRespuesta(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();												
		String c=gSon.toJson(body);
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();		
		try{ 
			@SuppressWarnings("deprecation")
			Request request = new Request.Builder()
					.url(header.getUrl() + servicio)
					.addHeader("Content-Type", "application/json")
					.addHeader("xHeader", gSon.toJson(header))	
					.post(RequestBody.create(media, gSon.toJson(body)))					
					.build();			
	       	
	     	Response response = client.newCall(request).execute();
			resp = gSon.fromJson(response.body().string(), Respuesta.class);
			if(resp==null) {
				resp = new Respuesta();
				resp.setCode(100);	    	
		    	resp.setMessage(servicio);
			}
	    }catch(Exception ex){	    	
	    	resp.setCode(100);	    	
	    	resp.setMessage(ex.getMessage());
	    }
		return resp;
	}

	
	public List<Obra> listadoObras(String servicio, Header header, Body body, Integer TimeSeconds){
		MediaType media = MediaType.parse("application/json; charset=utf-8");
		Respuesta resp = new Respuesta();				
		List<Obra> lista = null;
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.writeTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.readTimeout(TimeSeconds, TimeUnit.SECONDS) 
				.build();	
				
		Request request = new Request.Builder()
				.url(header.getUrl()+servicio)
				.addHeader("xheader",  gSon.toJson(header))
				.post(RequestBody.create(media, gSon.toJson(body)))	
				.addHeader("Content-Type", "application/json")
				.build();		
	    try{
	    	Response response = client.newCall(request).execute();
	    	resp = gSon.fromJson(response.body().string(), Respuesta.class);	
	    	Type listType = new TypeToken<List<Obra>>() {}.getType();
	    	if(resp.getCode()==200) {;
	    		lista = gSon.fromJson(resp.getData(), listType);
	    	}					
			
	    }catch(Exception ex){	
	    	resp=new Respuesta();
	    	resp.setCode(100);
	    	resp.setMessage("ERROR AL EJECUTAR EL SERVICIO: "+servicio);
	    }	    
	    return lista == null || lista.isEmpty() ? null : lista;
	}

	
	

	public String regresaColor(Integer indice) {
		String color="#C0C0C0";
		switch(indice) {
			case 1:color="#8B0000"; break;
			case 2:color="#A52A2A"; break;
			case 3:color="#B22222"; break;
			case 4:color="#DC143C"; break;
			case 5:color="#FF0000"; break;
			case 6:color="#FF6347"; break;
			case 7:color="#FF7F50"; break;
			case 8:color="#CD5C5C"; break;
			case 9:color="#F08080"; break;
			case 10:color="#E9967A"; break;
			case 11:color="#FA8072"; break;
			case 12:color="#FFA07A"; break;
			case 13:color="#FF4500"; break;
			case 14:color="#FF8C00"; break;
			case 15:color="#FFA500"; break;
			case 16:color="#FFD700"; break;
			case 17:color="#B8860B"; break;
			case 18:color="#DAA520"; break;
			case 19:color="#EEE8AA"; break;
			case 20:color="#BDB76B"; break;
			case 21:color="#F0E68C"; break;
			case 22:color="#808000"; break;
			case 23:color="#FFFF00"; break;
			case 24:color="#9ACD32"; break;
			case 25:color="#556B2F"; break;
			case 26:color="#6B8E23"; break;
			case 27:color="#7CFC00"; break;
			case 28:color="#7FFF00"; break;
			case 29:color="#ADFF2F"; break;
			case 30:color="#006400"; break;
			case 31:color="#008000"; break;
			case 32:color="#228B22"; break;
			case 33:color="#00FF00"; break;
			case 34:color="#32CD32"; break;
			case 35:color="#90EE90"; break;
			case 36:color="#98FB98"; break;
			case 37:color="#8FBC8F"; break;
			case 38:color="#00FA9A"; break;
			case 39:color="#00FF7F"; break;
			case 40:color="#2E8B57"; break;
			case 41:color="#66CDAA"; break;
			case 42:color="#3CB371"; break;
			case 43:color="#20B2AA"; break;
			case 44:color="#2F4F4F"; break;
			case 45:color="#008080"; break;
			case 46:color="#008B8B"; break;
			case 47:color="#00FFFF"; break;
			case 48:color="#00FFFF"; break;
			case 49:color="#E0FFFF"; break;
			case 50:color="#00CED1"; break;
			case 51:color="#40E0D0"; break;
			case 52:color="#48D1CC"; break;
			case 53:color="#AFEEEE"; break;
			case 54:color="#7FFFD4"; break;
			case 55:color="#B0E0E6"; break;
			case 56:color="#5F9EA0"; break;
			case 57:color="#4682B4"; break;
			case 58:color="#6495ED"; break;
			case 59:color="#00BFFF"; break;
			case 60:color="#1E90FF"; break;
			case 61:color="#ADD8E6"; break;
			case 62:color="#87CEEB"; break;
			case 63:color="#87CEFA"; break;
			case 64:color="#191970"; break;
			case 65:color="#000080"; break;
			case 66:color="#00008B"; break;
			case 67:color="#0000CD"; break;
			case 68:color="#0000FF"; break;
			case 69:color="#4169E1"; break;
			case 70:color="#8A2BE2"; break;
			case 71:color="#4B0082"; break;
			case 72:color="#483D8B"; break;
			case 73:color="#6A5ACD"; break;
			case 74:color="#7B68EE"; break;
			case 75:color="#9370DB"; break;
			case 76:color="#8B008B"; break;
			case 77:color="#9400D3"; break;
			case 78:color="#9932CC"; break;
			case 79:color="#BA55D3"; break;
			case 80:color="#800080"; break;
			case 81:color="#D8BFD8"; break;
			case 82:color="#DDA0DD"; break;
			case 83:color="#EE82EE"; break;
			case 84:color="#FF00FF"; break;
			case 85:color="#DA70D6"; break;
			case 86:color="#C71585"; break;
			case 87:color="#DB7093"; break;
			case 88:color="#FF1493"; break;
			case 89:color="#FF69B4"; break;
			case 90:color="#FFB6C1"; break;
			case 91:color="#FFC0CB"; break;
			case 92:color="#FAEBD7"; break;
			case 93:color="#F5F5DC"; break;
			case 94:color="#FFE4C4"; break;
			case 95:color="#FFEBCD"; break;
			case 96:color="#F5DEB3"; break;
			case 97:color="#FFF8DC"; break;
			case 98:color="#FFFACD"; break;
			case 99:color="#FAFAD2"; break;
			case 100:color="#FFFFE0"; break;
			case 101:color="#8B4513"; break;
			case 102:color="#A0522D"; break;
			case 103:color="#D2691E"; break;
			case 104:color="#CD853F"; break;
			case 105:color="#F4A460"; break;
			case 106:color="#DEB887"; break;
			case 107:color="#D2B48C"; break;
			case 108:color="#BC8F8F"; break;
			case 109:color="#FFE4B5"; break;
			case 110:color="#FFDEAD"; break;
			case 111:color="#FFDAB9"; break;
			case 112:color="#FFE4E1"; break;
			case 113:color="#FFF0F5"; break;
			case 114:color="#FAF0E6"; break;
			case 115:color="#FDF5E6"; break;
			case 116:color="#FFEFD5"; break;
			case 117:color="#FFF5EE"; break;
			case 118:color="#F5FFFA"; break;
			case 119:color="#708090"; break;
			case 120:color="#778899"; break;
			case 121:color="#B0C4DE"; break;
			case 122:color="#E6E6FA"; break;
			case 123:color="#FFFAF0"; break;
			case 124:color="#F0F8FF"; break;
			case 125:color="#F8F8FF"; break;
			case 126:color="#F0FFF0"; break;
			case 127:color="#FFFFF0"; break;
			case 128:color="#F0FFFF"; break;
			case 129:color="#FFFAFA"; break;
			case 130:color="#000000"; break;
			case 131:color="#696969"; break;
			case 132:color="#808080"; break;
			case 133:color="#A9A9A9"; break;
			case 134:color="#C0C0C0"; break;
			case 135:color="#D3D3D3"; break;
			case 136:color="#DCDCDC"; break;
			case 137:color="#F5F5F5"; break;
			case 138:color="#FFFFFF"; break;
		}
		return color;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
