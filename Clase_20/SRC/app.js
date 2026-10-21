const formulario = document.getElementById("formulario");
const mensaje = document.getElementById("mensaje");
const boton = document.getElementById("enviar");
const resultado = document.getElementById("resultado");

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    boton.disabled = true;
    resultado.textContent = "Enviando solicitud...";

    try {
        const respuesta = await fetch("/task", {
            method: "POST",
            headers: {
                "Content-Type": "text/plain; charset=UTF-8",
                "X-Debug": "true"
            },
            body: mensaje.value
        });

        const texto = await respuesta.text();
        const tiempo = respuesta.headers.get("X-Debug-info");

        resultado.textContent =
            `HTTP ${respuesta.status}\n${texto}` +
            (tiempo ? `\n${tiempo}` : "");
    } catch (error) {
        resultado.textContent = "No se pudo enviar la solicitud: " + error.message;
    } finally {
        boton.disabled = false;
    }
});