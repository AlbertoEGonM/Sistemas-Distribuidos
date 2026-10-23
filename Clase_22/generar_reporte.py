from pathlib import Path
from xml.sax.saxutils import escape
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Preformatted, KeepTogether
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
import pypdfium2 as pdfium

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'output/pdf'
OUT.mkdir(parents=True, exist_ok=True)
NAVY = colors.HexColor('#17334D')
BLUE = colors.HexColor('#226B9C')
PALE = colors.HexColor('#EEF4F8')
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='TitleReport', fontName='Helvetica-Bold', fontSize=30, leading=35, textColor=NAVY, spaceAfter=20))
styles.add(ParagraphStyle(name='SubtitleReport', fontName='Helvetica', fontSize=16, leading=23, textColor=BLUE, spaceAfter=15))
styles.add(ParagraphStyle(name='BodyReport', fontSize=10.5, leading=16, textColor=NAVY, spaceAfter=10))
styles.add(ParagraphStyle(name='HeadingReport', fontName='Helvetica-Bold', fontSize=18, leading=23, textColor=NAVY, spaceAfter=16))
styles.add(ParagraphStyle(name='SubReport', fontName='Helvetica-Bold', fontSize=12, leading=17, textColor=BLUE, spaceBefore=10, spaceAfter=8))
styles.add(ParagraphStyle(name='SmallReport', fontSize=9, leading=13, textColor=NAVY, spaceAfter=7))
styles.add(ParagraphStyle(name='CodeReport', fontName='Courier', fontSize=8, leading=12, backColor=PALE, borderPadding=10, spaceBefore=8, spaceAfter=14))
story=[]

def p(text, style='BodyReport'):
    story.append(Paragraph(text, styles[style]))

def heading(text): p(text,'HeadingReport')
def sub(text): p(text,'SubReport')
def code(text): story.append(Preformatted(text,styles['CodeReport']))
def page(): story.append(PageBreak())
def table(rows, widths):
    header_style=ParagraphStyle(name='HeaderCell',parent=styles['SmallReport'],textColor=colors.white,fontName='Helvetica-Bold')
    cells=[[Paragraph(escape(str(c)), header_style if i == 0 else styles['SmallReport']) for c in row] for i,row in enumerate(rows)]
    t=Table(cells,colWidths=widths,repeatRows=1,hAlign='LEFT')
    t.setStyle(TableStyle([
        ('BACKGROUND',(0,0),(-1,0),NAVY),('TEXTCOLOR',(0,0),(-1,0),colors.white),
        ('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),10),
        ('RIGHTPADDING',(0,0),(-1,-1),10),('TOPPADDING',(0,0),(-1,-1),8),
        ('BOTTOMPADDING',(0,0),(-1,-1),6),('ROWBACKGROUNDS',(0,1),(-1,-1),[PALE,colors.white]),
        ('LINEBELOW',(0,0),(-1,0),1,BLUE)]))
    story.append(t)

story.append(Spacer(1,70))
p('SISTEMAS DISTRIBUIDOS', 'SubtitleReport')
p('Procesamiento<br/>distribuido', 'TitleReport')
p('Parte 2 | Clase 22', 'SubtitleReport')
p('Cliente asíncrono, servidor despachador y proxy inverso con NGINX.')
story.append(Spacer(1,45))
sub('Integrantes')
p('GONZALEZ MARTINEZ ALBERTO EZEQUIEL<br/>MENDOZA CASTAÑEDA JOSE RICARDO')
sub('Fecha')
p('23 de octubre de 2026')
story.append(Spacer(1,35))
p('<b>Alcance del reporte.</b> Describe la implementación de los tres ejercicios y los resultados de las pruebas realizadas. Las capturas de pantalla solicitadas por el enunciado están pendientes de incorporarse.', 'SmallReport')

page()
heading('1. Objetivo y arquitectura')
p('Construir un sistema que reciba el hash SHA-256 de una palabra y distribuya su búsqueda entre dos nodos trabajadores. Un servidor despachador genera las subtareas, asigna trabajo de forma asíncrona y reúne los resultados. Una interfaz web permite realizar la consulta mediante NGINX.')
sub('Espacio de búsqueda')
p('Se consideran palabras de exactamente cinco letras minúsculas del alfabeto a-z. SHA-256 produce una huella de 256 bits, expresada como 64 caracteres hexadecimales. El programa calcula el hash de cada candidato y lo compara con el objetivo; no aplica una operación de descifrado.')
p('El espacio completo contiene <b>26<super>5</super> = 11,881,376 candidatos</b>. Al fijar la primera letra, cada tarea explora como máximo <b>26<super>4</super> = 456,976 candidatos</b>. Las 26 tareas cubren el espacio sin superponerse.')
sub('Recorrido de una consulta')
code('Navegador\n    |  HTTP :8090\n    v\nNGINX (interfaz web y proxy inverso)\n    |  /dispatcher\n    v\nWebServer :8080 -> Aggregator -> WebClient\n                         |              |\n                         v              v\n                  Worker :8081    Worker :8082\n                     /task           /task')
table([
 ['Componente','Responsabilidad'],
 ['Application','Ejercicio 1: genera tareas, invoca el agregador y muestra resultados.'],
 ['Aggregator / WebClient','Planificación dinámica y peticiones HTTP asíncronas.'],
 ['WebServer','Ejercicio 2: recibe el hash por /dispatcher y devuelve el reporte.'],
 ['ServidorHTTP / FuerzaBrutaHash','Búsqueda de cinco letras; procesamiento de una primera letra por tarea.'],
 ['NGINX / web/index.html','Ejercicio 3: interfaz y reenvío de solicitudes al despachador.'],
], [145,350])

page()
heading('2. Desarrollo de los ejercicios')
sub('Ejercicio 1. Cliente HTTP asíncrono')
p('Application obtiene el hash desde los argumentos; si no se proporciona, usa el de la palabra abaco. Genera las 26 tareas con el formato <b>hashObjetivo,letra</b>, desde a hasta z, y las entrega a Aggregator junto con las direcciones de los dos workers.')
p('El agregador inicia una cadena de CompletableFuture por worker. Al terminar una solicitud, el mismo worker recibe la siguiente tarea pendiente. Cada cadena mantiene como máximo una petición en curso. Un estado sincronizado por consulta protege el índice de asignación, los resultados y la condición de parada.')
p('Cuando llega una palabra, se valida su longitud, su primera letra y su hash. Si es válida, se detienen los nuevos envíos. Las tareas ya enviadas terminan y sus respuestas también se incluyen. Los resultados se ordenan por el índice original; el tiempo total incluye esta espera.')
sub('Ejercicio 2. Servidor despachador')
p('WebServer atiende <b>/dispatcher</b> en el puerto 8080. POST recibe el hash como texto plano; GET admite ?hash=HASH o ?HASH. Cada solicitud crea su propio agregador y sus 26 tareas, evitando compartir el estado de búsqueda entre usuarios.')
p('La respuesta contiene las tareas realizadas, la dirección de cada worker, los resultados, la contraseña encontrada o NULL, el número de tareas y el tiempo total. La cabecera <b>X-debug-info</b> incluye la duración en segundos. Un hash inválido devuelve HTTP 400; un fallo de comunicación con un worker devuelve HTTP 502.')
sub('Ejercicio 3. Interfaz web y NGINX')
p('La interfaz permite ingresar un hash de 64 caracteres y enviar la búsqueda. Incluye un ejemplo de abaco y muestra la respuesta del despachador usando texto, sin interpretar el contenido como HTML. NGINX sirve la página en el puerto 8090 y reenvía /dispatcher al puerto 8080.')
p('NGINX cumple la función de proxy inverso. La división en subtareas, la asignación a workers y la detección del resultado se realizan en Java. El proxy no sustituye al coordinador de procesamiento distribuido.')
sub('Decisiones de implementación')
p('Se conservan Application y WebServer para poder demostrar ambos ejercicios. Se reutiliza el motor de cinco letras de Clase_21. Cada petición del worker utiliza su propio MessageDigest. No se implementan reintentos ni recuperación de tareas ante la caída de un worker.')

page()
heading('3. Validación y resultados')
p('Las pruebas se ejecutaron en Windows con Java 26.0.1 y NGINX 1.28.0. Se verificaron dos workers, el despachador y el proxy mediante solicitudes HTTP reales. No se atribuyen estas mediciones a una ejecución en WSL ni a equipos físicamente independientes.')
table([
 ['Prueba','Resultado observado'],
 ['Compilación, JAR y NGINX','Compilación correcta, JAR ejecutable y configuración válida.'],
 ['Acceso mediante el proxy','Interfaz y /dispatcher disponibles mediante NGINX.'],
 ['Parada temprana: aaaaa','Palabra encontrada; se ejecutaron 2 de 26 tareas.'],
 ['Consultas simultáneas','abaco y bbbbb devueltas en consultas independientes.'],
 ['Búsqueda sin coincidencia','NULL después de completar las 26 tareas.'],
 ['Validación de entradas','HTTP 400, 404, 405 y 413 en los casos comprobados.'],
 ['Worker no disponible','HTTP 502, sin informar una búsqueda completa como exitosa.'],
], [170,325])
sub('Respuesta real registrada')
p('Hash objetivo de aaaaa:', 'SmallReport')
code('ed968e840d10d2d313a870bc131a4e2c311d7ad09bdf32b3418147221f51a6e2')
code('Tarea 1 | primera letra: a | worker: localhost:8081 | aaaaa\nTarea 2 | primera letra: b | worker: localhost:8082 | NULL\n\nContrasena: aaaaa\nTareas realizadas: 2 / 26\nTiempo total: 0.627372 segundos')
p('La transcripción resume las líneas de tareas del archivo evidencias/consulta.txt; la contraseña, la cantidad de tareas y el tiempo se conservan del registro. Es una muestra de ejecución, no una captura de pantalla.', 'SmallReport')
sub('Interpretación')
p('La parada temprana evita explorar las letras restantes. El tiempo depende de la carga del sistema, la planificación y el trabajo ya enviado. Dos workers en una misma computadora comparten CPU y memoria; por ello no se puede concluir que el tiempo sea la mitad del de una ejecución secuencial.')

page()
heading('4. Ejecución y entrega')
sub('Compilar y ejecutar desde WSL')
p('Requisitos: JDK 11 o superior y NGINX de Linux. Antes de iniciar, detener las instancias de Windows para liberar los puertos. Desde WSL:', 'SmallReport')
code('cd "/mnt/c/Users/gonza/OneDrive/Desktop/Sistemas Distribuidos/Clase_22"\nmkdir -p classes\njavac -encoding UTF-8 -d classes source/*.java')
p('Ejecutar cada servicio en una terminal distinta, desde Clase_22:', 'SmallReport')
code('java -cp classes ServidorHTTP 8081\njava -cp classes ServidorHTTP 8082\njava -cp classes WebServer 8080')
p('Para demostrar el ejercicio 1, con ambos workers activos:', 'SmallReport')
code('java -cp classes Application')
p('La configuración nginx/nginx.conf incluida utiliza rutas relativas al runtime de Windows. Para WSL se debe usar una configuración de Linux con la raíz apuntando a la ruta absoluta de Clase_22/web y el proxy /dispatcher dirigido a http://127.0.0.1:8080. El puerto de la interfaz es 8090.', 'SmallReport')
sub('Archivos que acompañan el reporte')
table([
 ['Ubicación','Contenido'],
 ['source/','Application, Aggregator, WebClient, WebServer y motor de búsqueda.'],
 ['web/ y nginx/','Interfaz web, configuración y runtime local de NGINX.'],
 ['README.md','Instrucciones de ejecución, pruebas y capturas.'],
 ['tests/integracion.py','Pruebas de integración con dos workers y NGINX.'],
 ['evidencias/','Respuesta registrada y descripción de las verificaciones.'],
], [145,350])
sub('Evidencia gráfica pendiente')
p('Para completar los requisitos gráficos de la entrega se deben incorporar: <b>(1)</b> una captura de /dispatcher con su resultado y tiempo total, y <b>(2)</b> una captura del navegador con una consulta junto a la terminal del despachador. Estas capturas no están incluidas en este documento.', 'SmallReport')
sub('Conclusión')
p('La implementación divide la búsqueda en 26 partes, las asigna dinámicamente a dos workers y coordina la parada cuando se encuentra una coincidencia. El despachador expone el cálculo mediante HTTP y NGINX permite acceder desde una interfaz web. Las pruebas verificaron el flujo completo y la separación del estado entre consultas.', 'SmallReport')
p('Referencia: M. en C. Ukranio Coronilla, Procesamiento Distribuido, Parte 2, enunciado de la práctica proporcionado. Base del proyecto: Clase_19 y Clase_21 del repositorio del curso.', 'SmallReport')

def footer(canvas,doc):
    canvas.saveState()
    width,height=A4
    canvas.setStrokeColor(colors.HexColor('#CFDCE6'))
    canvas.line(50,43,width-50,43)
    canvas.setFont('Helvetica',8)
    canvas.setFillColor(NAVY)
    canvas.drawString(50,29,'Sistemas Distribuidos | Clase 22 | Procesamiento distribuido - Parte 2')
    canvas.drawRightString(width-50,29,str(doc.page))
    canvas.restoreState()

target=OUT/'Reporte_Clase_22_Procesamiento_Distribuido.pdf'
doc=SimpleDocTemplate(str(target),pagesize=A4,rightMargin=50,leftMargin=50,topMargin=45,bottomMargin=60,
 title='Clase 22 - Procesamiento Distribuido Parte 2',author='GONZALEZ MARTINEZ ALBERTO EZEQUIEL; MENDOZA CASTAÑEDA JOSE RICARDO')
doc.build(story,onFirstPage=footer,onLaterPages=footer)
preview=ROOT/'tmp/pdfs'
preview.mkdir(parents=True,exist_ok=True)
pdf=pdfium.PdfDocument(str(target))
for i,page in enumerate(pdf):
    page.render(scale=1.2).to_pil().save(preview/f'reporte-{i+1}.png')
print(target)
print('Paginas:',len(pdf))
