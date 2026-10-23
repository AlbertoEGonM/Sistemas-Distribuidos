# Verificacion realizada

Pruebas reales ejecutadas el 23 de octubre de 2026 con Java 26.0.1 y NGINX 1.28.0 en Windows.

- Compilacion y empaquetado JAR correctos.
- Configuracion de NGINX validada con nginx -t.
- Interfaz accesible a traves del proxy, HTTP 200.
- Busqueda de aaaaa con parada temprana: 2 tareas ejecutadas.
- Consultas simultaneas abaco y bbbbb: resultados correctos e independientes.
- Hash invalido: HTTP 400; metodo incorrecto: 405; ruta incorrecta: 404; cuerpo excesivo: 413.
- Consulta GET con parametro hash: HTTP 200.
- Hash sin coincidencia dentro del espacio de cinco letras: NULL y 26 tareas.
- Cliente ejecutado desde Application.jar: resultado correcto.
- Worker detenido: HTTP 502.
- Inicio automatico con iniciar.ps1 comprobado.

consulta.txt conserva una respuesta real con su tiempo de procesamiento.
Las imagenes referencia-2.png a referencia-4.png son paginas del enunciado, NO capturas de ejecucion.
Faltan las capturas del endpoint y del navegador junto a la terminal: la herramienta de navegador
no pudo vincular la pestaña de esta sesion. El README explica como obtenerlas con el sistema iniciado.
