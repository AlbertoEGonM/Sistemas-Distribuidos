# Práctica Clase 16: servidor HTTP
Alberto Ezequiel Gonzalez Martinez — Grupo 6CM3

## Alcance
El archivo WebServer.java resuelve los cinco ejercicios en un servidor independiente, comentado. No es una transcripción del video ni una modificación del servidor original: esos materiales no pudieron consultarse. En /task se responde «Solicitud procesada»; si tu servidor original realiza otra operación, conserva esa operación y agrega los bloques indicados aquí. Para /searchtoken se suponen tokens de tres letras mayúsculas A-Z, separados por espacios; verifica que coincida con tu Clase 7. La búsqueda distingue mayúsculas. El resultado cambia porque los tokens son aleatorios.

## Preparación en WSL
Descomprime el ZIP. Abre una terminal dentro de la carpeta que contiene WebServer.java (puedes usar `cd /mnt/c/Users/TU_USUARIO/Downloads/practica16`). Sustituye la ruta por la real.

Comprueba que tengas un JDK 11 o superior:
```bash
java -version
javac -version
```
Si falta javac, en Ubuntu/WSL:
```bash
sudo apt update
sudo apt install default-jdk curl
```
Compila una vez:
```bash
javac -encoding UTF-8 WebServer.java
```
`javac` transforma el código fuente en clases ejecutables; `-encoding UTF-8` permite leer correctamente acentos en el archivo. El nombre público WebServer debe coincidir con WebServer.java.

## Ejercicio 1: ocho hilos frente a uno
Abre TRES terminales WSL: una para el servidor y dos para los clientes.

En la terminal del servidor:
```bash
java WebServer 8 5000
```
El primer argumento es el número de hilos. El segundo es la pausa en milisegundos de /task: 5000 equivale a cinco segundos. La pausa ocurre dentro del handler, no en main.

En cada terminal cliente ejecuta casi al mismo tiempo:
```bash
time curl -v --header 'X-Debug:true' --data 'prueba' localhost:8080/task
```
Con ocho hilos, ambas solicitudes tardarán aproximadamente cinco segundos. Detén el servidor con Ctrl+C y reinícialo:
```bash
java WebServer 1 5000
```
Repite las dos solicitudes simultáneas. Una tardará aproximadamente cinco segundos y la otra cerca de diez, si llegan juntas. Si la segunda llega un segundo después, tardará cerca de nueve. Usa `real` para comparar: mide el tiempo transcurrido, incluida la espera. `user` y `sys` miden CPU del cliente curl y no sustituyen a `real`.

Explicación para entregar:
> Con ocho hilos las dos solicitudes pueden atenderse en paralelo. Cada tarea duerme cinco segundos y las dos respuestas llegan aproximadamente después de ese intervalo. Con un hilo, la segunda solicitud espera a que termine la primera; por eso su tiempo de respuesta aumenta. Para dos solicitudes que llegan juntas, el promedio aproximado pasa de cinco a siete y medio segundos. El header de depuración mide desde que inicia el handler, de modo que no incluye la espera previa en la cola; por ello puede indicar unos cinco segundos aunque curl reporte cerca de diez.

Captura requerida: la salida de `time` en las dos terminales para cada configuración y evidencia de con cuántos hilos se inició el servidor. No ejecutes dos servidores en el mismo puerto.

## Ejercicio 2: bytes y cuerpo del POST
Detén el servidor anterior y ejecútalo sin pausa para el resto:
```bash
java WebServer 8 0
```
En otra terminal:
```bash
curl -v --data 'nombre=Alberto&grupo=6CM3' localhost:8080/task
```
El servidor debe mostrar:
```text
Bytes del cuerpo: 25
Contenido: nombre=Alberto&grupo=6CM3
```
Bloque relevante de atender:
```java
byte[] cuerpo = exchange.getRequestBody().readAllBytes();
String texto = new String(cuerpo, StandardCharsets.UTF_8);
imprimirSolicitud(exchange, cuerpo.length, texto);
```
Se lee el cuerpo una sola vez. `cuerpo.length` cuenta bytes reales, mientras que `texto.length()` cuenta unidades UTF-16 y puede dar una cantidad distinta. Si usas `--data 'acción'`, son siete bytes UTF-8. El ejemplo enviado con curl se imprime tal cual: no se decodifican parámetros de formulario.

Captura requerida: comando curl con sus parámetros y consola del servidor con el cuerpo y su cantidad de bytes.

## Ejercicio 3: headers y pares key - value
```bash
curl -v --header 'X-Alumno:Alberto' --header 'X-Grupo:6CM3' --header 'X-Debug:true' --data 'prueba' localhost:8080/task
```
Busca el método imprimirSolicitud. `getRequestHeaders()` obtiene un mapa de nombres y listas de valores. `headers.size()` cuenta nombres distintos. El ciclo externo recorre cada nombre y el interno imprime cada valor, de modo que no se pierden headers con varios valores. También se muestra la cantidad de pares.

curl agrega headers como Host, User-Agent, Accept, Content-Length y Content-Type. Por eso el total puede ser mayor que los tres personalizados. El orden y la capitalización pueden cambiar: los nombres HTTP no distinguen mayúsculas.

Captura requerida: comando curl y registro del servidor con el total y todos los pares. Adjunta el método imprimirSolicitud como código añadido.

## Ejercicio 4: tiempo legible
```bash
curl -v --header 'X-Debug:true' --data 'prueba' localhost:8080/task
```
La salida detallada de curl contiene un header con este formato (valores ilustrativos):
```text
< X-debug-info: La operacion tomo 2266869700 nanosegundos = 2 segundos con 266 milisegundos.
```
Conversión usada en formatearTiempo:
```java
long segundos = ns / 1_000_000_000L;
long milisegundos = (ns % 1_000_000_000L) / 1_000_000L;
```
La división entera obtiene segundos completos. El módulo conserva el sobrante, convertido después a milisegundos completos. No se redondea. `System.nanoTime() - inicio` mide el intervalo dentro del handler hasta preparar la respuesta; no incluye la transmisión al cliente ni la espera en cola. Sin X-Debug o con X-Debug:false, ese header no se envía.

Captura requerida: curl -v mostrando el header. Adjunta formatearTiempo y el bloque que agrega X-Debug-info.

## Ejercicio 5: /searchtoken
Con el servidor activo y sin pausa, ejecuta EXACTAMENTE el comando solicitado:
```bash
time curl -v --header 'X-Debug:true' --data '34567890,SOL' localhost:8080/searchtoken
```
- `--data` envía un POST. El cuerpo contiene la cantidad y la subcadena separados por coma.
- El servidor construye 34 567 890 tokens aleatorios y busca SOL.
- El cuerpo de la respuesta contiene únicamente el número de coincidencias; no contiene la cadenota ni una frase.
- El tiempo aparece en el header X-Debug-info con segundos y milisegundos.
- Esta entrada produce 138 271 560 caracteres, contando espacios. La cadenota se almacena en memoria; usa una JVM con memoria suficiente. Si obtienes OutOfMemoryError, reinicia con `java -Xmx1g WebServer 8 0` y repite el mismo comando curl.

Pruebas pequeñas antes de la grande:
```bash
curl --data '1000,IPN' localhost:8080/searchtoken
curl --data '1000,SOL' localhost:8080/searchtoken
curl --data '0,SOL' localhost:8080/searchtoken
curl -v --data '5,SO' localhost:8080/searchtoken
```
Las dos primeras pueden responder cero por azar. La tercera debe responder cero. La última responde HTTP 400 porque requiere tres letras. No confundir solicitudes incorrectas con fallos del servidor.

Captura requerida: comando exacto con 34567890,SOL, header del tiempo, número devuelto y salida de time. Adjunta el registro de createContext, buscarTokens y el código compartido de depuración.

## Cómo se organiza el código
- main: crea el servidor, registra endpoints y configura el pool.
- atender: comprueba ruta y método, lee el POST, ejecuta la tarea, agrega depuración y envía la respuesta.
- imprimirSolicitud: muestra bytes, texto y headers de /task.
- formatearTiempo: convierte nanosegundos en segundos y milisegundos.
- buscarTokens: valida la entrada, construye la cadenota y cuenta apariciones.

HttpExchange representa una solicitud y su respuesta. createContext asocia una ruta con un handler. El pool limita cuántos handlers pueden trabajar simultáneamente. sendResponseHeaders envía el estado HTTP y la longitud del cuerpo en bytes. close libera los recursos del intercambio.

## Entrega
Código comentado, capturas reales de los cinco ejercicios y explicación del primero. Las capturas deben tomarse en tu computadora. Si el profesor exige comentar exactamente el código del video, hay que aplicar estos cambios a ese archivo y ajustar los comentarios a su explicación.
