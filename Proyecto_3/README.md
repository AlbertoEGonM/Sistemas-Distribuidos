# Proyecto 3: simulación distribuida de asteroides

**Alumno:** Gonzalez Martinez Alberto Ezequiel  
**Grupo:** 7CM3  
**Materia:** Sistemas Distribuidos

El servidor muestra un espacio negro de **1000 × 1000 píxeles** usando Swing y AWT. El cliente permite crear asteroides mediante una interfaz de texto de Lanterna. Ambos procesos se comunican exclusivamente por HTTP. No se usa Maven.

## Inicio rápido en Windows

Requisito: **JDK 11 o posterior**, con `java` y `javac` disponibles en PATH. El JAR de Lanterna ya está en `lib/`.

En una terminal PowerShell, entra a esta carpeta:

```powershell
cd 'C:\Users\gonza\OneDrive\Desktop\Sistemas Distribuidos\Proyecto_3'
.\compilar.ps1
.\servidor.ps1
```

En **otra terminal PowerShell**, entra a la misma carpeta y ejecuta:

```powershell
cd 'C:\Users\gonza\OneDrive\Desktop\Sistemas Distribuidos\Proyecto_3'
.\cliente.ps1
```

En Windows, Lanterna abre su emulador de terminal en una ventana independiente. Sigue siendo la interfaz textual de Lanterna y se opera por teclado. Usa **TAB** para cambiar de campo y **ENTER** para activar un botón. En terminales compatibles con ANSI se puede solicitar la terminal de texto con `-TerminalTexto`.

Si PowerShell bloquea los scripts por su política de ejecución, se pueden usar directamente los comandos Java de la siguiente sección.

El área negra mide 1000 × 1000; el marco del sistema operativo ocupa espacio adicional. Una pantalla con menos altura útil puede recortar la ventana. Para verla completa durante la grabación, conviene una pantalla con altura útil suficiente y escalado de Windows al 100 %.

## Compilar y ejecutar manualmente (para el video)

Desde `Proyecto_3`, en PowerShell:

```powershell
New-Item -ItemType Directory -Force bin
javac --release 11 --add-modules jdk.httpserver -encoding UTF-8 -cp 'lib/lanterna-3.1.2.jar' -d bin src/*.java
java --add-modules jdk.httpserver -cp bin ServidorAsteroides 8080
```

En otra terminal, desde la misma carpeta:

```powershell
java -cp 'bin;lib/lanterna-3.1.2.jar' ClienteAsteroides http://localhost:8080
```

El separador de classpath de Windows es `;`. En Linux/macOS se cambia por `:`. La opción `--release 11` produce clases compatibles con Java 11 y versiones posteriores.

### Otra máquina o puerto

```powershell
# Servidor
.\servidor.ps1 -Puerto 8090
# Cliente en la misma computadora
.\cliente.ps1 -Servidor http://localhost:8090
# Cliente en otra computadora: sustituye por la IP real del servidor
.\cliente.ps1 -Servidor http://192.168.1.50:8090
```

El servidor escucha en las interfaces de red disponibles. Para dos equipos, ambos deben tener conectividad y el firewall debe permitir el puerto elegido. El programa es una simulación para una red de prácticas y no incorpora autenticación.

## Uso del cliente

1. **Rapidez:** número mayor que cero, en píxeles por segundo.
2. **Dirección:** componentes `dx`, `dy` de un vector no nulo. Por ejemplo `(1, 0)` va a la derecha, `(0, -1)` hacia arriba y `(-1, 1)` hacia abajo a la izquierda. No es una posición de destino. Se normaliza automáticamente; `(1, 1)` y `(10, 10)` producen la misma dirección.
3. **Giro:** velocidad angular en grados por segundo. Positivo significa horario y negativo antihorario, porque el eje Y de la pantalla crece hacia abajo. Cero permite movimiento sin giro.
4. Activa **Crear asteroide** para enviar los valores al servidor.
5. Para un lote, escribe `n` en **Cantidad aleatoria** y activa **Crear n aleatorios**.
6. El contador muestra el total confirmado por el servidor, incluyendo creaciones de otros clientes. **Actualizar contador / reconectar** consulta el valor actual.

Los decimales manuales admiten punto o coma. Para evitar valores extremos, la rapidez se limita a 5000 px/s, el giro a ±3600 grados/s y cada lote a 10000 asteroides. Se permiten sucesivos lotes; no se eliminan asteroides durante la ejecución. Los aleatorios usan rapidez de 40 a 180 px/s, dirección uniforme entre 0 y 360 grados y giro de ambos signos con magnitud entre 15 y 180 grados/s.

Al iniciar, el servidor tiene cero asteroides. Un asteroide recién creado puede tardar en verse porque nace completamente fuera del área y entra por su trayectoria; a menor rapidez, mayor espera. Cerrar el cliente conserva la simulación. Cerrar el servidor termina la sesión y su contenido en memoria.

## Archivos y responsabilidades

| Archivo en `src/` | Responsabilidad |
| --- | --- |
| `Asteroide.java` | Forma local, posición, velocidad, giro y dibujo de copias en los bordes. |
| `FabricaAsteroides.java` | Formas aleatorias únicas, validación de atributos y nacimiento exterior. |
| `Simulacion.java` | Lista compartida, altas y actualización del modelo. |
| `PanelEspacio.java` | Panel Swing negro y temporizador de animación. |
| `ServidorAsteroides.java` | Ventana del servidor y rutas HTTP del JDK. |
| `ClienteHTTP.java` | Peticiones HTTP síncronas y lectura del contador. |
| `ClienteAsteroides.java` | Campos, botones y navegación de Lanterna. |

Cada clase se encuentra en un archivo separado y comienza con los datos del proyecto, alumno y grupo. Revisa que esos datos, tomados del Proyecto 2 del repositorio, sigan siendo correctos antes de entregar.

## Cómo funciona

### Formas únicas

Cada asteroide tiene de 9 a 16 vértices ordenados por ángulo alrededor del centro. Sus radios varían, lo que produce una silueta irregular cerrada sin cruces. Su radio máximo va de 12 a 34 píxeles. El primer vértice tiene radio normalizado 1 y los demás tienen radios menores; así hay un máximo único que fija la orientación de la forma.

La fábrica guarda en un `HashSet` una firma con el número de vértices y sus radios normalizados. Si una nueva combinación ya existe, la genera de nuevo. La firma no depende del tamaño ni del giro del asteroide; un cambio de tamaño no se usa para fingir una silueta diferente.

### Rapidez y rotación constantes

Para un vector de dirección `d = (dx, dy)`, se calculan:

```text
vx = v * dx / longitud(d)
vy = v * dy / longitud(d)
x(t + dt) = x(t) + vx * dt
y(t + dt) = y(t) + vy * dt
angulo(t + dt) = angulo(t) + omega * dt
```

El código normaliza primero las componentes con una escala para evitar desbordamientos numéricos. Convierte el giro a radianes internamente. `System.nanoTime()` proporciona el tiempo transcurrido real, de modo que la velocidad no depende de que el temporizador produzca exactamente 60 cuadros por segundo. El ángulo se reduce módulo `2π`.

### Nacimiento fuera de pantalla

Se elige aleatoriamente un borde por el que la velocidad apunte hacia el interior. El centro comienza a una distancia `radio + 1` fuera del borde; por eso ningún vértice es visible al nacer. El punto de entrada por ese borde también es aleatorio. La coordenada paralela al borde se ajusta según el tiempo que tarda en entrar, para que el vector elegido realmente lleve el asteroide al área.

Antes de la primera entrada solo se dibuja el polígono original. Se calcula el tiempo exacto hasta que el centro cruza el borde y, a partir de ese instante, se habilita el espacio periódico. Esto también funciona si un cuadro de animación tarda más de lo normal.

### Reaparición en el lado opuesto

Después de entrar, la posición del centro se reduce módulo 1000 en ambos ejes. Se dibujan copias parciales trasladadas 1000 píxeles cuando una figura toca un borde, incluidas las esquinas. Así la salida y entrada son continuas. Las copias son solo dibujo: no cuentan como nuevos asteroides. La instancia, su forma, velocidad y rotación permanecen iguales.

### Hilos y comunicación

- El hilo de eventos de Swing anima y dibuja el servidor.
- Cuatro trabajadores atienden solicitudes HTTP.
- Los métodos sincronizados de `Simulacion` protegen la lista mientras se agregan o dibujan figuras.
- El cliente tiene un trabajador de red que ejecuta **`HttpClient.send` síncrono**. Lanterna conserva su propio hilo para responder al teclado. Los resultados regresan mediante `invokeLater`.

No hay intercambio directo de objetos ni sockets propios entre cliente y servidor. Los POST no se reintentan automáticamente: si se pierde la respuesta después de que el servidor creó un lote, primero actualiza el contador para evitar agregarlo dos veces.

## Protocolo HTTP

| Método | Ruta | Cuerpo |
| --- | --- | --- |
| GET | `/estado` | Ninguno |
| POST | `/asteroides` | `v=100&dx=1&dy=0&omega=-45` |
| POST | `/aleatorios` | `n=1000` |

Los POST usan `Content-Type: application/x-www-form-urlencoded`. Los valores con signo `+` se codifican como `%2B`; el cliente lo hace automáticamente. La respuesta es texto UTF-8, por ejemplo `total=1017`. Crear devuelve HTTP 201 y consultar devuelve HTTP 200. Las entradas incorrectas reciben 400, las rutas desconocidas 404, los métodos incorrectos 405, un cuerpo excesivo 413 y un tipo de contenido incorrecto 415.

## Pruebas

```powershell
.\probar.ps1
```

No requieren JUnit ni ventanas abiertas. Compilan de nuevo y comprueban:

- 3000 formas únicas, nacimientos completamente exteriores y lienzo inicial negro.
- Normalización del vector, desplazamiento constante, giro con ambos signos y cruces de los cuatro bordes.
- Dibujo de fragmentos en lados opuestos y conservación de los asteroides.
- Servidor HTTP real en un puerto disponible, validación de errores y solicitudes concurrentes.
- La secuencia `0 → 1 → 6 → 7 → 17 → 1017` exigida para el video.
- Interfaz Lanterna real sobre una terminal virtual: TAB y ENTER para crear un asteroide, agregar cinco y salir.

Resultado verificado: **21049 comprobaciones correctas**. Se generan imágenes del modelo y una captura textual de Lanterna en `tmp/`. Las pruebas de interfaz usan terminal virtual; la apariencia de la ventana y el escalado del escritorio deben comprobarse al ejecutar los programas en tu computadora.

## Dependencia

Se incluye **Lanterna 3.1.2**, obtenida como JAR independiente desde Maven Central, sin ejecutar Maven:

- [Proyecto, fuentes y documentación oficial](https://github.com/mabe02/lanterna)
- [JAR de Lanterna 3.1.2](https://repo.maven.apache.org/maven2/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar)
- SHA-256 del JAR incluido: `cdc4e8a611561252ffadeb9135ad699fa305c7ce8cd2cbeb29bd37cbbc946301`.

Los archivos `.class` se generan en `bin/`. `bin/` y `tmp/` se excluyen de Git. El código, la dependencia y las guías quedan dentro de `Proyecto_3`.
