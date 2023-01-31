package com.pe.refirma.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class Utils {
	
	public static boolean verificarAccesoToken(HttpServletResponse response,String token) throws IOException {
		
		if(token==null) {
    		PrintWriter writer =  response.getWriter();
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	writer.print("ACCESO NO AUTORIZADO");
			writer.flush(); 
        	return false;
        }
    	if(Jwt.verificarJWT(token)==false){
    		
    		PrintWriter writer =  response.getWriter();
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	writer.print("ACCESO NO AUTORIZADO");
			writer.flush(); 
        	return false;
    	}
    
    	return true;
	}
}
