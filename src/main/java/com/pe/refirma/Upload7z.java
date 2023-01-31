package com.pe.refirma;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.jboss.logging.Logger;

import com.pe.refirma.utils.Utils;


@WebServlet(name = "/Upload7z", urlPatterns="/upload7z")
public class Upload7z extends HttpServlet {
	
	private static final Logger LOGGER = Logger.getLogger(Upload7z.class);
	
	private static final long serialVersionUID = 1L;	
	
	private static final int THRESHOLD_SIZE 	= 1024 * 1024 * 3; 	// MB
	private static final int MAX_FILE_SIZE 		= 1024 * 1024 * 100; // MB
	private static final int MAX_REQUEST_SIZE 	= 1024 * 1024 * 110; // MB 
	
	/**
	 * Exclusivamente utilizado por ReFirmaPCX para subir los documentos (firmados) que esta comprimidos con 7z
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {    	  	
    	try {	    	
			if (!ServletFileUpload.isMultipartContent(request)) {	
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);	
				return;
			}	
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD_SIZE);
			factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
			
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setFileSizeMax(MAX_FILE_SIZE);
			upload.setSizeMax(MAX_REQUEST_SIZE);
								
			String uploadPath = System.getProperty("java.io.tmpdir") + File.separator + "upload"+File.separator+"signed";	
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}				
			
			List<FileItem> formItems = upload.parseRequest(request);
			Iterator<FileItem> iter = formItems.iterator();						
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();				
				if (!item.isFormField()) {
					String token = item.getFieldName();	
					if(Utils.verificarAccesoToken(response,token)==false)
		        		return;
					
					String fileName = URLDecoder.decode(item.getName(),"UTF-8");						
					String filePath = uploadPath + File.separator + fileName;
					File storeFile = new File(filePath);			
					item.write(storeFile);
						
					String signedFolder = FilenameUtils.removeExtension(fileName);
					
					//Descomprimimos el archivo 7z 
					SevenZFile sevenZFile = new SevenZFile(new File(filePath));
				    SevenZArchiveEntry entry = sevenZFile.getNextEntry();
				    while(entry!=null){
				    	
				    	File directorio = new File(uploadPath+File.separator+signedFolder);
				    	if(!directorio.exists()) {
				    		directorio.mkdirs();	
				    	}
				    	
				    	LOGGER.info(entry.getName());
				        
				        FileOutputStream out = new FileOutputStream(uploadPath+File.separator+signedFolder+File.separator+entry.getName());
				        byte[] content = new byte[(int) entry.getSize()];
				        
				        sevenZFile.read(content, 0, content.length);
				        out.write(content);
				        out.close();
				        entry = sevenZFile.getNextEntry();
				    }
				    sevenZFile.close();
						
				}
			}			
			response.setStatus(HttpServletResponse.SC_OK);	
			
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			ex.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
		}		
    }
}
