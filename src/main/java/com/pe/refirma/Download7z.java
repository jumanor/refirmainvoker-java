package com.pe.refirma;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import com.pe.refirma.utils.Utils;

@WebServlet(name = "/Download7z", urlPatterns="/download7z")
public class Download7z extends HttpServlet{
	
	private static final Logger LOGGER = Logger.getLogger(Download7z.class);
	private static final long serialVersionUID = 1L;	
	
	/**
	 * Exclusivamente utilizado por ReFirmaPCX para subir los documentos (firmados) que esta comprimidos con 7z
	 */
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			String token=request.getParameter("token").toString();
			if(Utils.verificarAccesoToken(response,token)==false)
	    		return;
			
			String documentName7z = request.getParameter("documentName").toString()+".7z"; 
			String filename =  System.getProperty("java.io.tmpdir")+File.separator+"upload"+File.separator+documentName7z;
					
			Path path = Paths.get(filename);
			byte[] data = Files.readAllBytes(path);						
			
			response.setContentType ("application/pdf");	
			//response.setHeader("Content-disposition", "attachment; filename=" + documentName);	
			response.setHeader("Content-disposition", "filename=" + documentName7z);	
			response.setHeader("Cache-Control", "max-age=30");
	        response.setHeader("Pragma", "No-cache");
	        response.setDateHeader("Expires",0);
	        response.setContentLength(data.length);
	        
	        ServletOutputStream out = response.getOutputStream();
	        out.write(data,0,data.length);
	        out.flush();
	        out.close();
		}
		catch(IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
		}
    }

}
