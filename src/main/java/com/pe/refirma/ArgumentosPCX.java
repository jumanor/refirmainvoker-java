package com.pe.refirma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.refirma.utils.Configuration;
import com.pe.refirma.utils.Utils;

@WebServlet(name = "/ArgumentosPCX", urlPatterns="/argumentsServletPCX")
public class ArgumentosPCX extends HttpServlet{
	
	private static final Logger LOGGER = Logger.getLogger(ArgumentosPCX.class);
	private static final long serialVersionUID = 1L;
	
	private final Configuration config = Configuration.getInstance();	
	
	public class JMalformedURLException extends Exception{
		private static final long serialVersionUID = 1L;
		
		public JMalformedURLException(String mensaje) {
			// TODO Auto-generated constructor stub
			super(mensaje);
		}
	}
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
     	
        response.setStatus(HttpServletResponse.SC_OK);
        
	}////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static void createSevenZipFile(File folderToZip,File outPutComprimido) {
		
		// Create 7z file.
		try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(outPutComprimido)) {
				 	
			//File folderToZip = new File("compress-me");
	 
			// Walk through files, folders & sub-folders.
			Files.walk(folderToZip.toPath()).forEach(p -> {
				File file = p.toFile();
	 
				// Directory is not streamed, but its files are streamed into 7z file with
				// folder in it's path
				if (!file.isDirectory()) {
					LOGGER.info("Seven Zipping file - " + file);
					try (FileInputStream fis = new FileInputStream(file)) {
						String name = file.getName();
						SevenZArchiveEntry entry_1 = sevenZOutput.createArchiveEntry(file, name);
						sevenZOutput.putArchiveEntry(entry_1);
						sevenZOutput.write(Files.readAllBytes(file.toPath()));
						sevenZOutput.closeArchiveEntry();
					} catch (IOException e) {
						LOGGER.error(e.getMessage());
						e.printStackTrace();
					}
				}
			});
	 
			// Complete archive entry addition.
			sevenZOutput.finish();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * Crea un archivo 7z con los PDFs descargados
	 * 
	 * @param urls
	 * @return
	 * @throws JMalformedURLException
	 */
	private String createFile7z(JSONArray urls) throws JMalformedURLException {
		
		String nameUUID=UUID.randomUUID().toString();
		String rutaMain = System.getProperty("java.io.tmpdir") + File.separator + "upload"+File.separator+nameUUID;
		LOGGER.info("Ruta temporal: "+rutaMain);
		
		//Creamos un directorio
		File rutaMainFile = new File(rutaMain);
		if (!rutaMainFile.exists()) {
			rutaMainFile.mkdir();
		}
		
		String url="";
		
		try {
			
			for(int i=0;i<urls.length();i++){
				
				url=urls.getJSONObject(i).getString("url");
	        	String name=urls.getJSONObject(i).getString("name");
				URL urlDownload = new URL(url);
        		String filepath=rutaMain+File.separator+name+".pdf";
            	File file=new File(filepath);
            	//Descargamos los PDFs
				FileUtils.copyURLToFile(urlDownload, file,10000,30000);
        		
				
			}//end for
			
			File folder7Z = new File(rutaMain);
			File outPutComprimido = new File(System.getProperty("java.io.tmpdir") + File.separator + "upload"+File.separator+nameUUID+".7z");
			createSevenZipFile(folder7Z, outPutComprimido);
			
		}catch(MalformedURLException ex) {
    		
			ex.printStackTrace();
    		throw new JMalformedURLException("No se pudo descargar URL "+url+" con mensaje: "+ex.getMessage());
    	}
    	catch(FileNotFoundException ex) {
			
    		ex.printStackTrace();
			throw new JMalformedURLException("No se encuentra URL "+url+" con mensaje: "+ex.getMessage());
		
		}
    	catch(IOException ex) {
    		
    		ex.printStackTrace();
    		throw new JMalformedURLException("No se pudo crear URL "+url+" con mensaje: "+ex.getMessage());
    	}
    	catch(Exception ex) {
			
			ex.printStackTrace();
			throw ex;
		}
		
		return nameUUID;
		
	}
	/**
	 * Cadena de argumentos que se envia a refirma PCX
	 * 
	 * @param protocol
	 * @param documentName
	 * @param fileDownloadUrl
	 * @param fileDownloadLogoUrl
	 * @param fileDownloadStampUrl
	 * @param fileUploadUrl
	 * @param posx
	 * @param posy
	 * @param reason
	 * @param pageNumber
	 * @param token
	 * @return
	 * @throws JsonProcessingException
	 */
	private String paramWeb(String protocol,String documentName,String fileDownloadUrl,String fileDownloadLogoUrl,String fileDownloadStampUrl,
			String fileUploadUrl, String posx,String posy,String reason,String pageNumber, String token) throws JsonProcessingException {
		
		String param = "";
		
		ObjectMapper mapper = new ObjectMapper(); 
		   
		Map<String, String> map = new HashMap<>();
        map.put("app", "pcx");
        map.put("mode", "lot-p");
        map.put("clientId", config.getClientId());   
        map.put("clientSecret", config.getClientSecret());         
        map.put("idFile", token); //FieldName Multipart, al momento de recibir el archivo subido se puede utilizar este argumento como identificador.
        map.put("type", "W"); //L=Documento está en la PC , W=Documento está en la Web.
        map.put("protocol", protocol); //T=http, S=https (SE RECOMIENDA HTTPS)
        map.put("fileDownloadUrl",fileDownloadUrl);
        map.put("fileDownloadLogoUrl", fileDownloadLogoUrl);
        map.put("fileDownloadStampUrl", fileDownloadStampUrl);
        map.put("fileUploadUrl", fileUploadUrl);      
        map.put("contentFile", documentName);
        map.put("reason", reason);
        map.put("isSignatureVisible", "true");             
        map.put("stampAppearanceId", "0"); //0:(sello+descripcion) horizontal, 1:(sello+descripcion) vertical, 2:solo sello, 3:solo descripcion
        map.put("pageNumber", pageNumber);
        map.put("posx", posx); 
        map.put("posy", posy);                 
        map.put("fontSize", "7"); 
        map.put("dcfilter", ".*FIR.*|.*FAU.*"); //".*" todos, solo firma ".*FIR.*|.*FAU.*"
        map.put("signatureLevel", "0");               
        map.put("outputFile", "out-"+documentName);    
        map.put("maxFileSize", config.getMaxFileSize7z()); //Por defecto será 5242880 5MB - Maximo 100MB
        //JSON
        param = mapper.writeValueAsString(map);                    
        LOGGER.info(param);
                  
        //Base64 (JAVA 8)
        param = Base64.getEncoder().encodeToString(param.getBytes(StandardCharsets.UTF_8));           
        LOGGER.info(param); 
        
        return param;
		
	}////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private URL isUrlAvailable(String url) throws JMalformedURLException {
		
		URL urlHttp=null;
		
		try {
			urlHttp = new URL(url);
			HttpURLConnection huc = (HttpURLConnection) urlHttp.openConnection();
			huc.setRequestMethod("HEAD");
			int responseCode = huc.getResponseCode();
			
			if(responseCode!=200) {
				return null;
			}
			
		}
		catch(IOException ex) {
			ex.printStackTrace();
			throw new JMalformedURLException("no se pudo verificar acceso a "+url);
		}
		
		return urlHttp;
	}////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * URI llamado por el Cliente para contruir Cadena de Argumentos en BASE64
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
	
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
        
        PrintWriter writer=null;
        
        try {

        	writer =  response.getWriter();
        	String token=request.getHeader("x-access-token");
        	
        	if(Utils.verificarAccesoToken(response,token)==false)
        		return;
        	
        	
        	String pathServlet = request.getServletPath();
            String fullPathServlet = request.getRequestURL().toString();
            int resInt = fullPathServlet.length() - pathServlet.length();
            String serverURL = fullPathServlet.substring(0, resInt);
            
            String protocol = "";
            if (serverURL.contains("https://")){
         	   protocol = "S";
            }else{
         	   protocol = "T";
            }
            
	        JSONObject inputParameter=new JSONObject(IOUtils.toString(request.getReader()));
	        
	        String documentNameUUID=createFile7z(inputParameter.getJSONArray("pdfs"));
	        
	        
	        String documentName7z = documentNameUUID + ".7z";
	        String fileDownloadUrl = serverURL + "/download7z?documentName=" + documentNameUUID+"&token="+token ;
	        String fileDownloadLogoUrl = serverURL + "/public/iLogo.png";
	        String fileDownloadStampUrl = serverURL + "/public/iFirma.png";
	        String fileUploadUrl = serverURL + "/upload7z";
	        String pageNumber="0";
	        String posx=inputParameter.getJSONObject("firma").getInt("posx")+"";
	        String posy=inputParameter.getJSONObject("firma").getInt("posy")+"";
	        String reason=inputParameter.getJSONObject("firma").getString("reason");
	        
	        //parametros opcionales
	        try {
	        	
	        	fileDownloadStampUrl=inputParameter.getJSONObject("firma").getString("stampSigned");
	        	URL url=isUrlAvailable(fileDownloadStampUrl);
	        	if(url==null) {
	        		throw new JMalformedURLException(" No se pudo descargar imagen "+fileDownloadStampUrl);
	        	}
	        	//Restriccion muy extraña de Refirma Invoker
	        	if(protocol.equals("T") && url.getProtocol().equals("https")) {
	        		throw new JMalformedURLException(" No se pudo descargar imagen "+fileDownloadStampUrl +" utilice protocolo http");
	        	}
	        	if(protocol.equals("S") && url.getProtocol().equals("http")) {
	        		throw new JMalformedURLException(" No se pudo descargar imagen "+fileDownloadStampUrl +" utilice protocolo https");
	        	}
	        	
	        	LOGGER.info("stampSigned: "+fileDownloadStampUrl);
	        
	        }catch(JSONException ex) {
	        	
	        	LOGGER.info("parametro stampSigned no encontrado, usando default "+fileDownloadStampUrl);
	        }
	        
	        try {
	        	
	        	pageNumber=inputParameter.getJSONObject("firma").getInt("pageNumber")+"";
	        
	        }catch(JSONException ex) {
	        	
	        	LOGGER.info("parametro pageNumber no encontrado, usando default "+pageNumber);
	        }
	        
	        
	        String argumentosEnc=paramWeb(protocol, documentName7z, fileDownloadUrl, fileDownloadLogoUrl,
	        		fileDownloadStampUrl, fileUploadUrl, posx, posy, reason, pageNumber, token);
	        
	        String urlBasePDFDownloadSigned = serverURL + "/downloadPdfSigned/" + documentNameUUID;
	        
	        
	        JSONObject data=new JSONObject();
	        data.put("argumentosBase64", argumentosEnc);
	        data.put("urlBase",urlBasePDFDownloadSigned);
	        
	        JSONObject salida=new JSONObject();
	        salida.put("codigo",2000);//código interno
	        salida.put("data",data);
	        
	        response.setStatus(200);
	        writer.print(salida.toString());
			writer.flush(); 
			
			
        }catch(JMalformedURLException ex) {
        	
	        response.setStatus(404);
	        writer.print(ex.getMessage());
			writer.flush();
			
        }catch(Exception ex) {
        	
        	LOGGER.error(ex.getMessage());
        	ex.printStackTrace();
        }
        
       
	}///////////////////////////////////////////////////////////////////////////////////////////////
}	
