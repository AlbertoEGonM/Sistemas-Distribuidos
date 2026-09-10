#include <stdio.h>
#include <stdlib.h>
#include <time.h>

// comando Time Nombre_archivo para el tiempo y se toma el mas grande
// comando gcc gcc ipn_busqueda.c -o ipn_busqueda para compilar el programa y comando ./ipn_busqueda 17576 para ejecutarlo y generar la cadenota de 17576 palabras

int main(int argc, char *argv[])
{
    if (argc < 2) // Verifica si se proporcionó un argumento
    {
        printf("Uso: %s <numero_de_palabras>\n", argv[0]);
        return 1;
    }
    int n = atoi(argv[1]); // Convierte el argumento a un número entero
    if (n <= 0)
    {
        printf("El valor de n debe ser mayor a 0 \n");
        return 1;
    }
    size_t tamano = (size_t)n * 4;               // Cada palabra tiene 3 letras + 1 espacio
    char *cadenota = (char *)malloc(tamano + 1); // +1 para el carácter nulo al final

    if (cadenota == NULL) // Verifica si la asignación de memoria fue exitosa
    {
        printf("Error: No se pudo asignar memoria.\n");
        return 1;
    }

    srand((unsigned int)time(NULL)); // Inicializa la semilla para la generación de números aleatorios

    for (int i = 0; i < n; i++) // Genera n palabras aleatorias de 3 letras
    {
        int base = i * 4;
        cadenota[base] = 'A' + (rand() % 26);
        cadenota[base + 1] = 'A' + (rand() % 26);
        cadenota[base + 2] = 'A' + (rand() % 26);
        cadenota[base + 3] = ' '; // Espacio para separar
    }
    cadenota[tamano] = '\0'; // Agrega el carácter nulo al final de la cadena
    int contador = 0;
    printf("Buscando la subcadena \"IPN\" en la cadenota...\n\n"); // Imprime un mensaje indicando que se está buscando la subcadena "IPN"

    for (size_t i = 0; i < tamano - 2; i++) // Itera a través de la cadenota hasta el penúltimo carácter
    {
        if (cadenota[i] == 'I' && cadenota[i + 1] == 'P' && cadenota[i + 2] == 'N')
        {
            contador++;
            printf("-> Encontrado en la posición (índice): %zu\n", i); // Imprime la posición (índice) donde se encontró la subcadena "IPN"
        }
    }

    printf("Total de ocurrencias de \"IPN\": %d\n", contador); // Imprime el total de ocurrencias encontradas de la subcadena "IPN"

    free(cadenota); // Libera la memoria asignada para la cadenota

    return 0;
}