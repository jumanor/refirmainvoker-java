package com.pe.refirma.utils;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import org.jboss.logging.Logger;
public class Jwt {
	
	private static final Logger LOGGER = Logger.getLogger(Jwt.class);

	public final static String ALGORITMO_CIFRADO = AlgorithmIdentifiers.RSA_USING_SHA256;
    private final static String SECRET_KEY_JWT = Configuration.getInstance().getSecretKeyJwt();
    private final static String ISSUER="jumanor@gmail.com";
    private static int TIME_EXPIRE_TOKEN=Configuration.getInstance().getTimeExpireToken();
    
    private static RsaJsonWebKey cifrador;
    
    static {
    	
    	inicializaCifrador();
    }
    
    public static void inicializaCifrador() {
        try {
            cifrador = RsaJwkGenerator.generateJwk( 2048 );
            cifrador.setKeyId( SECRET_KEY_JWT );
        }
        catch ( JoseException e ) {
        	LOGGER.error("Error al iniciar el cifrador."+e.getMessage());
        }
    }
    public static String generarJWT() throws JoseException {
        JwtClaims claims = new JwtClaims();
        // La aplicación que genera el token
        claims.setIssuer(ISSUER);
        // Cuando expirará el token (10 minutos)
        claims.setExpirationTimeMinutesInTheFuture( TIME_EXPIRE_TOKEN );
        // Identificador unico para el token
        claims.setGeneratedJwtId();
        // El token ha sido creado ahora
        claims.setIssuedAtToNow();
        // Tiempo en el pasado por el que el token no puede ser valido (2 minutos)
        claims.setNotBeforeMinutesInThePast( 2 );        
        
        JsonWebSignature jws = new JsonWebSignature();
        // Contenido del token en formato JSON
        jws.setPayload( claims.toJson() );
        // Id de la clave de cifrado
        jws.setKeyIdHeaderValue( cifrador.getKeyId() );
        // Clave de cifradogetSecretKeyJwt
        jws.setKey( cifrador.getPrivateKey() );
 
        // Seleccion de algoritmo de cifrado
        jws.setAlgorithmHeaderValue( ALGORITMO_CIFRADO );
        
        // Generacion de token compactado
        String jwt = jws.getCompactSerialization();
        
        return jwt;
    }
    public static boolean verificarJWT(String token){
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
        //El token debe tener fecha de expiracion
        .setRequireExpirationTime() 
        //Permitir un intervalo de segundos de margen a la hora de validar el tiempo de expiracion
        .setAllowedClockSkewInSeconds(30)         
        //El token debe llevar como aplicacion la nuestra
        .setExpectedIssuer(ISSUER) 
        //Recupero la clave del cifrador
        .setVerificationKey(cifrador.getKey())
        //Confirmo que el algoritmo de cifrado es el indicado
        .setJwsAlgorithmConstraints(     new AlgorithmConstraints(ConstraintType.WHITELIST, 
                        AlgorithmIdentifiers.RSA_USING_SHA256)).build();
        
        try {
        	//Compruebo el token
        	JwtClaims jwtClaims = jwtConsumer.processToClaims( token ); 
        	LOGGER.info("JWT validation succeeded! " + jwtClaims);
        	
        	return true;
        	
        }catch(InvalidJwtException e) {
        	
        	return false;
        }
    }
}
