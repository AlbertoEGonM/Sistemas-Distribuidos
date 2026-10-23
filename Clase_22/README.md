# Clase 22 - Procesamiento distribuido, parte 2

Implementa los tres ejercicios: cliente asincrono, dispatcher HTTP y NGINX con interfaz web.
Solo busca palabras de exactamente cinco letras ASCII minusculas (a-z), sin acentos.

## Inicio rapido (PowerShell, desde Clase_22)

Requisito: JDK 11 o superior, con java, javac y jar en PATH.

```powershell
./iniciar.ps1
```

Abre http://localhost:8090, pulsa **Cargar ejemplo: abaco** y despues **Buscar contrasena**.
NGINX sirve la pagina y reenvia /dispatcher al puerto 8080. El navegador no accede directamente a los workers.

Los servicios se ejecutan en segundo plano. Para ver la salida del dispatcher y tomar la captura con el navegador al lado:

```powershell
Get-Content ./evidencias/dispatcher.log -Wait
```

Deten el seguimiento con Ctrl+C. Para cerrar los servicios del proyecto:

```powershell
./detener.ps1
```

## Ejercicio 1: cliente asincrono

Con los workers encendidos:

```powershell
java -jar classes/Application.jar
# Tambien admite un hash como primer argumento:
java -jar classes/Application.jar HASH_SHA256
```

Sin argumento usa el hash de `abaco`. Application crea 26 tareas `hash,letra`.
Aggregator asigna una peticion por worker y entrega la siguiente al que termina.
Al recibir una palabra valida deja de enviar tareas. Las solicitudes ya enviadas terminan
y se incluyen en los resultados, ordenados por el indice original. No se imprimen tareas que no se enviaron.
El tiempo total incluye esta espera. Cada consulta tiene su propio estado sincronizado.

Compilacion manual alternativa:

```powershell
cd source
javac -encoding UTF-8 -d ../classes *.java
cd ../classes
jar cfe Application.jar Application Application.class Aggregator*.class FuerzaBrutaHash*.class networking
java -jar Application.jar
```

El JAR incluye las clases internas y el motor que valida la respuesta de los workers.

## Ejercicio 2: endpoint /dispatcher

POST recibe el hash como texto plano (sin formulario ni JSON). GET acepta `?hash=HASH` o `?HASH`.

```powershell
$hash = 'ed968e840d10d2d313a870bc131a4e2c311d7ad09bdf32b3418147221f51a6e2'
Invoke-WebRequest -Uri http://localhost:8080/dispatcher -Method POST -ContentType 'text/plain' -Body $hash | Select-Object -ExpandProperty Content
```

Ese hash corresponde a `aaaaa`. La respuesta contiene las tareas ejecutadas, sus workers y resultados,
la palabra, la cantidad de tareas y el tiempo total. La cabecera X-debug-info tambien incluye el tiempo.
Una busqueda completa sin coincidencias devuelve NULL y 26 tareas. Una entrada invalida devuelve 400;
un fallo de worker devuelve 502. No se reintentan tareas fallidas.

Para iniciar los componentes manualmente, usa una terminal para cada comando:

```powershell
java -cp classes ServidorHTTP 8081
java -cp classes ServidorHTTP 8082
java -cp classes WebServer 8080
```

WebServer y Application aceptan, despues de su primer argumento, URLs de workers alternativas:

```powershell
java -cp classes WebServer 8080 http://192.168.1.10:8081/task http://192.168.1.11:8082/task
```

## Ejercicio 3: NGINX

Configuracion: nginx/nginx.conf. Interfaz: web/index.html. Puerto publico local: 8090.
Se utiliza NGINX 1.28.0 para Windows, descargado del sitio oficial.
Si necesitas restaurar el runtime en otra computadora:

```powershell
Invoke-WebRequest https://nginx.org/download/nginx-1.28.0.zip -OutFile nginx/nginx-1.28.0.zip
Expand-Archive nginx/nginx-1.28.0.zip -DestinationPath nginx/runtime -Force
```

NGINX actua como reverse proxy; Java divide la busqueda y decide cuando terminar.
Los workers se basan en Clase_21 y usan un MessageDigest independiente por solicitud.
Se conserva /status. El endpoint auxiliar /ipn requiere los archivos resources de la practica anterior;
no se utiliza en esta practica.

## Verificacion y evidencias

Con los servicios detenidos, ejecuta con Python 3:

```powershell
python -u tests/integracion.py
```

La prueba inicia los servicios, verifica la interfaz via NGINX, parada temprana, consultas simultaneas,
validacion HTTP, busqueda exhaustiva, JAR y worker caido; despues cierra sus procesos.
Los registros quedan en evidencias/. No inicies las pruebas mientras ejecutas iniciar.ps1.

Para la entrega, captura el endpoint mostrando el tiempo (ejercicio 2), y el navegador con una consulta
junto a la terminal siguiendo dispatcher.log (ejercicio 3). Los tiempos dependen de la computadora;
dos workers en una misma maquina no equivalen a dos equipos fisicamente independientes.
