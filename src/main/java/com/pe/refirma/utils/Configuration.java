package com.pe.refirma.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;


public class Configuration {
	
	private static final Logger LOGGER = Logger.getLogger(Configuration.class);
   
    private static Configuration instance;
    private static Properties defaultProperties;
    
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }
    private Configuration() {
    	
    	defaultProperties = loadDefaultProperties(); 
    }
    private Properties loadDefaultPropertiesResources() {
    	
	    String resourceName = "/config.properties";
	    LOGGER.info("ubicacion config.properties: src.main.resources.config.properties: ");
	    InputStream fis = null;
	    Properties prop = new Properties();
	    try {
	        fis = this.getClass().getResourceAsStream(resourceName);
	        prop.load(fis);
	        LOGGER.info("archivo config.properties cargado satisfactoriamtente: src.main.resources.config.properties");
	    } catch (IOException ioe) {
	    	LOGGER.error(ioe.getStackTrace().toString());
	    } finally {
	        if (fis != null) {
	            try {
	                fis.close();
	            } catch (Exception e) {
	            	LOGGER.error(e.getStackTrace().toString());
	            }
	        }
	    }
	    return prop;
    }
    private Properties loadDefaultProperties() {
    	
    	InputStream input = null;
    	Properties prop=new Properties();
    	try {
    		
	    	String ruta=System.getProperty("user.home")+"/refirma-invoker-java/config.properties";
	    	System.out.println("ubicacion config.properties: "+ruta);
	    	File f = new File(ruta);
	    	if(f.exists() && !f.isDirectory()) {
	    			
				input = new FileInputStream(ruta);
				prop.load(input);
				System.out.println("config.properties cargado satisfactoriamente: "+ruta);
	    		
	    	}
	    	else {
	    		System.out.println("archivo config.properties no encontrado: "+ruta);
	    		return loadDefaultPropertiesResources();
	    	}
	    }
    	catch(Exception e) {
    		
    		LOGGER.error(e.getStackTrace().toString());
    		
    	}finally {
    		
    		if (input != null) {
	            try {
	            	input.close();
	            } catch (Exception e) {
	            	LOGGER.error(e.getStackTrace().toString());
	            }
	        }
    	}
    	
    	return prop;
    }
    
    /*PROPERTIES*/ 
    public String getClientId() {
        return defaultProperties.getProperty("clientId").trim();
    }
    public String getClientSecret() {
        return defaultProperties.getProperty("clientSecret").trim();
    }
    public String getUserAccessApi() {
    	return defaultProperties.getProperty("userAccessApi").trim();
    }
    public String getSecretKeyJwt() {
    	return defaultProperties.getProperty("secretKeyJwt").trim();
    }
    public String getMaxFileSize7z() {
    	
        String maxFileSize7z=defaultProperties.getProperty("maxFileSize7z").trim(); 
        if(maxFileSize7z!=null) {
        	
        	return maxFileSize7z;
        }
    	
    	return  "10485760";//valor por defecto
    }
    public int getTimeExpireToken() {
    	 
    	String timeExpireToken=defaultProperties.getProperty("timeExpireToken");
 	    if(timeExpireToken!=null) {
 	    	try{
 	    		
 	    		int expire=Integer.parseInt(timeExpireToken);
 	    		
 	    		return expire;
 	    	
 	    	}catch(NumberFormatException ex) {
 	    		
 	    		ex.printStackTrace();
 	    		throw ex;
 	    	}
 	    }
    	
		return 5;//valor por defecto
	 }
}
