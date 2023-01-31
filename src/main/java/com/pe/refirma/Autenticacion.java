package com.pe.refirma;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jose4j.lang.JoseException;
import org.json.JSONObject;

import com.pe.refirma.utils.Configuration;
import com.pe.refirma.utils.Jwt;


@WebServlet(name = "/Autenticacion", urlPatterns="/autenticacion")
public class Autenticacion extends HttpServlet{
	
	private static final Logger LOGGER = Logger.getLogger(Autenticacion.class);
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
     	
        response.setStatus(HttpServletResponse.SC_OK);
        
	}////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
	
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers","Content-Type,x-requested-with,cache-control,key,value,x-access-token");
        
        PrintWriter writer=null;
        try {
        	
        	writer =  response.getWriter();
        	
	        JSONObject inputParameter=new JSONObject(IOUtils.toString(request.getReader()));
	        String usuarioAccesoApi=inputParameter.getString("usuarioAccesoApi");
	       
	        if(!usuarioAccesoApi.equals(Configuration.getInstance().getUserAccessApi())) {
	        	
	        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	 	        writer.print("Usuario no autorizado");
	 			writer.flush();
	        	return;
	        }
        
			String tokenString=Jwt.generarJWT();
			LOGGER.info("Token generado: "+tokenString);
	        
	        JSONObject data=new JSONObject();
	        data.put("codigo", 2000);//codigo interno
	        data.put("data", tokenString);
	        
	        response.setStatus(200);
	        writer.print(data.toString());
			writer.flush(); 
		
        } catch (JoseException e) {
			// TODO Auto-generated catch block
        	LOGGER.error(e.getMessage());
			e.printStackTrace();
        	response.setStatus(404);
 	        writer.print("No se pudo generar token");
 			writer.flush();
        	
		} catch(IOException e) {
			
			LOGGER.error(e.getMessage());
			e.printStackTrace();
        	response.setStatus(404);
 	        writer.print("No se pudo generar token");
 			writer.flush();
		}
        
	}///////////////////////////////////////////////////////////////////////////////////////////////
}
