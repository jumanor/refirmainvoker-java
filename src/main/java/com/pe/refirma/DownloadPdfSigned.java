package com.pe.refirma;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.json.JSONObject;

import com.pe.refirma.utils.Utils;

@WebServlet(name = "/DownloadPdfSigned", urlPatterns="/downloadPdfSigned/*")
public class DownloadPdfSigned extends HttpServlet{
	
	private static final Logger LOGGER = Logger.getLogger(DownloadPdfSigned.class);
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
     	
        response.setStatus(HttpServletResponse.SC_OK);
	}	
	/**
	 * Descargamos el documento PDF firmado mediante GET
	 */
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
        
        PrintWriter writer=null;
        try {
        	
	        String pathInfo = request.getPathInfo();
	        String[] splits = pathInfo.split("/");
	        
	        if(splits.length != 4) {//dir/file/token
				
	        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        	writer =  response.getWriter();
	 	        writer.print("URI incorrecto");
	 			writer.flush();
				return;
			}
	        
			String fileSigned = URLDecoder.decode(splits[2], StandardCharsets.UTF_8.toString()).trim()+"[R].pdf"; 
			String dir = URLDecoder.decode(splits[1], StandardCharsets.UTF_8.toString()).trim()+"[R]"; 
			String token= URLDecoder.decode(splits[3], StandardCharsets.UTF_8.toString()).trim();
			
			if(Utils.verificarAccesoToken(response,token)==false)
	    		return;
			
			Path path = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "upload" +
			File.separator + "signed"+File.separator+dir+File.separator + fileSigned);
			
			LOGGER.info(path);
			byte[] data = Files.readAllBytes(path);	
			
			response.setContentType ("application/pdf");	
			response.setHeader("Content-disposition", "filename=" + fileSigned);	
			response.setHeader("Cache-Control", "max-age=30");
	        response.setHeader("Pragma", "No-cache");
	        response.setDateHeader("Expires",0);
	        response.setContentLength(data.length);
	        
	        ServletOutputStream out = response.getOutputStream();
	        out.write(data,0,data.length);
	        out.flush();
	        out.close();
	        
        }catch(IOException e){
        	LOGGER.error(e.getMessage());
        	e.printStackTrace();
        	response.setStatus(404);
        	writer =  response.getWriter();
 	        writer.print("No se encontro el documento");
 			writer.flush();
        }
    }
	/**
	 * Descargamos el documento PDF firmado en Base64 mediante POST
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
		response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
		
		PrintWriter writer=null;
		
		try {
			
			writer =  response.getWriter();
			
			String pathInfo = request.getPathInfo();
			String[] splits = pathInfo.split("/");
			
			if(splits.length != 3) {//dir/file
				
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	 	        writer.print("URI incorrecto");
	 			writer.flush();
				return;
			}
			
			String file=URLDecoder.decode(splits[2], StandardCharsets.UTF_8.toString()).trim();
			String fileSigned = file+"[R].pdf"; 
			String dir = URLDecoder.decode(splits[1], StandardCharsets.UTF_8.toString()).trim()+"[R]"; 
			String token=request.getHeader("x-access-token");
			
			if(Utils.verificarAccesoToken(response,token)==false)
	    		return;
			
			Path path = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "upload" +
			File.separator + "signed"+File.separator+dir+File.separator + fileSigned);
			
			LOGGER.info(path.toString());
			byte[] data = Files.readAllBytes(path);	
			
			String pdfEncodeByte = Base64.getEncoder().encodeToString(data);
			
			JSONObject obj=new JSONObject();
			obj.put("codigo",2000);//codigo interno
			obj.put("data", pdfEncodeByte);
			 
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(200);
			writer.print(obj.toString());
			writer.flush();
			
		}catch(IOException e) {
			
			LOGGER.error(e.getMessage());
			e.printStackTrace();
        	response.setStatus(404);
 	        writer.print("No se encontro el documento");
 			writer.flush();
		}
    }
}
