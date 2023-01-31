# Refirma Invoker Integration (Java) - Reniec
Implementación del Motor de Firma Digital - Refirma Invoker Integration - del [RENIEC](https://dsp.reniec.gob.pe/refirma_suite/main/web/main.jsf).

Esta implementación esta construida con Java (JSP)

Se recomienda revisar la Implementación de [Refirma Invoker Integration construida con Golang](https://github.com/jumanor/refirmainvoker).

# Características 
- Soporte para firmar varios documentos (ReFirma PCX)
- Api Rest, puede integrarse en cualquier proyecto web (Php, Python, Java, etc)
- Json Web Tokens (JWT)
- Soporte para protocolo https (SSL/TLS)

# Documentos de la Implementación
- Un paper https://cutt.ly/yN0cAXL
- Refirma Suite https://bit.ly/2ktJRY2
- Guía de integración invoker https://bit.ly/34wctnn
- Argumentos invoker https://bit.ly/2J5

# Instalación
1. Instalar maven.
2. Ejecutar el siguiente comando: mvn clean install.
3. Se crea el archivo **invoker.war** en la carpeta target.
4. Instalar **invoker.war** en un contenedor de servlets (Widfly, Glassfish, Apache Tomcat, etc). 
5. En el servidor donde se ejecuta **invoker.war** ubicar el user home *(ver: [System.getProperty("user.home")](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html))* y crear la carpeta **refirma-invoker-java** y dentro de esta crear el archivo **config.properties** con la siguiente información:
    ``` bash
    # Identificador proporcionado por RENIEC
    clientId=K57845459hkj
    # Identificador proporcionado por RENIEC
    clientSecret=TYUOPDLDFDG
    # Clave secreta para generar Tokens
    secretKeyJwt=muysecretokenjwt
    # Usuario que accedera a la API
    userAccessApi=usuarioAccesoApi
    # Tiempo de expiración del Token en minutos. Ejemplo 5 minutos (Opcional)
    timeExpireToken=5
    # Maximo tamaño del archivo 7z en bytes. Ejemplo 10 megas (Opcional)
    maxFileSize7z=10485760
    ``` 
6. Levantar el contenedor de servlets

# Funcionamiento

Esta implementación de *Refirma Invoker Integration* se puede usar en ***cualquier proyecto web*** (Php, Java, Python, etc) solo tiene que consumir las Api Rest implementadas, para controlar el acceso se usa JSON Web Tokens ([JWT](https://jwt.io/)).

Esta disponible un video del funcionamiento (ejemplos) en los siguientes enlaces: [enlace1](https://www.youtube.com/watch?v=GPdfa7NeKZw).

Refirma Invoker usa **Microsoft Click Once** para invocar a Refirma PCX.

En caso use **Visual Studio Code** instale el plugin [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) que habilita un Servidor Web Embebido.

Para probar los ejemplos tiene que seguir los siguientes pasos:

1. Si esta usando navegador **Chome** o **Firefox** instala los siguientes plugins para habilitar **Microsoft Click Once**:

    - Chrome instale este [plugin](https://chrome.google.com/webstore/detail/clickonce-for-google-chro/kekahkplibinaibelipdcikofmedafmb) 

    - Firefox instale este [plugin](https://addons.mozilla.org/es/firefox/addon/meta4clickoncelauncher/?utm_source=addons.mozilla.org&utm_medium=referral&utm_content=search)  
    
2. En caso use el navegador **Edge** no es necesario instalar nada adicional.

3. Copia la carpeta [example](https://github.com/jumanor/refirmainvoker-java/tree/master/example) de este repositorio en un Servidor Web

4. Ingresa a cualquier ejemplo que desee probar ejecutando **test.html**


``` javascript
//Listamos los documentos que se desean firmar digitalmente
let pdfs=[];
pdfs[0]={url:"http://miservidor.com/docs1.pdf",name:"doc1"};
pdfs[1]={url:"http://miservidor.com/docs2.pdf",name:"doc2"};

//Enviamos la posicion en donde se ubicara la representación gráfica de la firma digital
let firmaParam={};
firmaParam.posx=10;
firmaParam.posy=12;
firmaParam.reason="Soy el autor del documento pdf";
firmaParam.stampSigned="http://miservidor.com/estampillafirma.png";//parametro opcional
firmaParam.pageNumber=0; //parametro opcional, pagina donde se pondra la firma visible 

//Llamamos a Refirma Invoker Integration con la dirección ip en donde se ejecuta main.exe o main
let firma=new RefirmaInvoker("http://192.168.1.10:8080/invoker");
//Importante:
//El Sistema de Gestion Documental se encarga de la autenticación y envía un token al Cliente
//Este método se usa solo como demostración no se debe de usar en el Cliente
let token=await firma.autenticacion("usuarioAccesoApi");
//Realiza el proceso de Firma Digital
let url_base=await firma.ejecutar(pdfs,firmaParam,token);

//En este caso obtenemos los documentos firmados digitalmente y los enviamos a un frame
document.getElementById("frame1").src=url_base+"/"+encodeURI("doc1")+"/"+encodeURI(token);
document.getElementById("frame2").src=url_base+"/"+encodeURI("doc2")+"/"+encodeURI(token);
```          

El *Sistema de Gestión Documental* autentica a los Usuarios normalmente contra una Base de Datos,
despues de la autencación satisfactoria se debe de consumir  el API REST /autenticacion de ReFirma Invoker 
y enviar el **token** al Cliente.

![a link](https://drive.google.com/uc?export=view&id=1h4dQG-IFukSkxRO2CEM5zuWIVmisxuCU)

Ejemplo en Python
``` python
import requests
import json
api_url = "http://127.0.0.1:8080/invoker/autenticacion"
param={"usuarioAccesoApi":"usuarioAccesoApi"}
response = requests.post(api_url,json=param)
if response.status_code == 200:
	token=response.json().get("data")
	print(token)

```
Ejemplo en Php
``` php
$params=array("usuarioAccesoApi"=>"usuarioAccesoApi");
$postdata=json_encode($params);
$opts = array('http' =>
    array(
    'method' => 'POST',
    'header' => 'Content-type: application/json',
    'content' => $postdata
    )
);
$context = stream_context_create($opts);
@$response = file_get_contents("http://127.0.0.1:8080/invoker/autenticacion", false, $context);
if(isset($http_response_header) == true){
    
    $status_line = $http_response_header[0];
    preg_match('{HTTP\/\S*\s(\d{3})}', $status_line, $match);
    $status = $match[1];

    if ($status == 200){
        $obj=json_decode($response,true);
        $token=$obj["data"];
        echo $token;
    }    
}
```

# Contribución

Por favor contribuya usando [Github Flow](https://guides.github.com/introduction/flow/). Crea un fork, agrega los commits, y luego abre un [pull request](https://github.com/fraction/readme-boilerplate/compare/).

# License
Copyright © 2023 [Jorge Cotrado](https://github.com/jumanor). <br />
This project is [MIT](https://github.com/jumanor/refirmainvoker/blob/master/License) licensed.
