# Guion para la demostración

Antes de grabar, repasa `README.md` y el código para poder explicar la normalización, la creación fuera de pantalla, el giro y el cruce de bordes. Usa una sesión nueva del servidor para que el contador comience en cero.

1. Abre dos terminales en `Proyecto_3`. Inicia OBS y captura la zona con las terminales y las ventanas del programa. Asegúrate de que se vea completa el área negra de 1000 × 1000.
2. En la primera terminal muestra la compilación con `javac` y el inicio de `ServidorAsteroides`, usando los comandos del README. La ventana debe iniciar vacía.
3. En la segunda terminal ejecuta `ClienteAsteroides`. En Windows se abrirá la terminal emulada de Lanterna.
4. Crea **un asteroide manual**: rapidez `100`, dirección `(1, 1)`, giro `45`. Activa **Crear asteroide**. Muestra el contador en **1** y espera a que entre desde un borde. Se verá el giro horario.
5. Deja la cantidad aleatoria en `5` y activa **Crear n aleatorios**. El total será **6**.
6. Crea **otro asteroide manual**: rapidez `120`, dirección `(-1, 0)`, giro `-90`. El total será **7**. El signo negativo produce giro antihorario.
7. Cambia la cantidad a `10` y crea el lote. El total será **17**.
8. Cambia la cantidad a `1000` y crea el lote. El total será **1017**. Mantén la grabación unos segundos para que se vea la entrada gradual y el movimiento continuo.
9. Muestra figuras que salen de un borde y se ven en el opuesto. Termina la grabación.

No hace falta narración según el enunciado. El video debe ser una captura real de la ejecución. Esta carpeta contiene el código y las guías; **queda pendiente grabar el video**.

Para la entrega, sube cada archivo `.java` de `src/` por separado y el video, siguiendo las indicaciones del curso. El enunciado pide no entregar archivos comprimidos ni enlaces externos como sustituto. No necesitas subir las clases compiladas, pruebas o capturas temporales, salvo que el profesor las solicite.
