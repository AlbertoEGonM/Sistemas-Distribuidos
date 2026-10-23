"""Pruebas reales con dos workers, dispatcher y NGINX. Solo biblioteca estandar."""
import concurrent.futures
import hashlib
import pathlib
import subprocess
import time
import urllib.request
import urllib.error
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
PROCESSES = []
LOGS = []
settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True, text=True)
JAVA = str(pathlib.Path(re.search(r'java.home = (.+)', settings.stderr).group(1).strip()) / 'bin/java.exe')

def start(args, name, cwd=ROOT):
    if args[0] == 'java':
        args[0] = JAVA
    log = open(ROOT / 'evidencias' / (name + '.log'), 'w', encoding='utf-8')
    LOGS.append(log)
    process = subprocess.Popen(args, cwd=cwd, stdout=log, stderr=subprocess.STDOUT)
    PROCESSES.append(process)
    return process

def request(url, body=None, method=None):
    req = urllib.request.Request(url, data=None if body is None else body.encode(), method=method)
    try:
        with urllib.request.urlopen(req, timeout=180) as response:
            return response.status, response.read().decode(), dict(response.headers)
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode(), dict(error.headers)

def wait(url):
    for _ in range(100):
        try:
            request(url)
            return
        except (OSError, urllib.error.URLError):
            time.sleep(.1)
    raise AssertionError('No inicio: ' + url)

def digest(word):
    return hashlib.sha256(word.encode()).hexdigest()

def run():
    (ROOT / 'evidencias').mkdir(exist_ok=True)
    try:
        for port in (8081, 8082):
            start(['java', '-cp', 'classes', 'ServidorHTTP', str(port)], 'worker-' + str(port))
            wait(f'http://localhost:{port}/status')
        start(['java', '-cp', 'classes', 'WebServer'], 'dispatcher')
        wait('http://localhost:8080/dispatcher')
        nginx = ROOT / 'nginx/runtime/nginx-1.28.0'
        subprocess.run([str(nginx / 'nginx.exe'), '-t', '-p', str(nginx) + '/', '-c', str(ROOT / 'nginx/nginx.conf')], check=True)
        start([str(nginx / 'nginx.exe'), '-p', str(nginx) + '/', '-c', str(ROOT / 'nginx/nginx.conf')], 'nginx', nginx)
        wait('http://localhost:8090/')
        status, html, _ = request('http://localhost:8090/')
        assert status == 200 and 'SHA-256' in html
        status, text, headers = request('http://localhost:8090/dispatcher', digest('aaaaa'))
        assert status == 200 and 'Contrasena: aaaaa' in text
        count = int(text.split('Tareas realizadas: ')[1].split(' /')[0])
        assert count <= 2, text
        assert 'X-debug-info' in headers
        (ROOT / 'evidencias/consulta.txt').write_text(text, encoding='utf-8')
        print('OK: NGINX sirve la interfaz y conecta al dispatcher; parada temprana:', count)
        with concurrent.futures.ThreadPoolExecutor(2) as pool:
            reports = list(pool.map(lambda word: request('http://localhost:8090/dispatcher', digest(word)), ['abaco', 'bbbbb']))
        for word, (status, text, _) in zip(['abaco', 'bbbbb'], reports):
            assert status == 200 and ('Contrasena: ' + word) in text
        print('OK: consultas simultaneas independientes')
        for method, path, body, expected in [
            ('POST', '/dispatcher', 'invalido', 400),
            ('DELETE', '/dispatcher', None, 405),
            ('GET', '/dispatcher/no', None, 404),
            ('POST', '/dispatcher', 'a' * 4097, 413),
            ('GET', '/dispatcher?hash=' + digest('aaaaa'), None, 200),
        ]:
            assert request('http://localhost:8080' + path, body, method)[0] == expected
        print('OK: validacion, GET, limites de cuerpo y metodos HTTP')
        status, text, _ = request('http://localhost:8090/dispatcher', digest('fuera-del-espacio'))
        assert status == 200 and 'Contrasena: NULL' in text and 'Tareas realizadas: 26 / 26' in text
        print('OK: busqueda exhaustiva sin coincidencia, 26 tareas')
        cli = subprocess.run(['java', '-jar', 'classes/Application.jar', digest('aaaaa')], cwd=ROOT, capture_output=True, text=True, timeout=60)
        assert cli.returncode == 0 and 'Contrasena: aaaaa' in cli.stdout
        print('OK: ejercicio 1 ejecutado desde el JAR')
        PROCESSES[0].terminate()
        PROCESSES[0].wait(timeout=10)
        failed = request('http://localhost:8080/dispatcher', digest('aaaaa'))
        assert failed[0] == 502, failed
        print('OK: worker no disponible produce HTTP 502')
    finally:
        nginx = ROOT / 'nginx/runtime/nginx-1.28.0'
        if (nginx / 'logs/nginx.pid').exists():
            subprocess.run([str(nginx / 'nginx.exe'), '-p', str(nginx) + '/', '-c', str(ROOT / 'nginx/nginx.conf'), '-s', 'quit'], cwd=nginx, capture_output=True)
        for process in reversed(PROCESSES):
            if process.poll() is None:
                try:
                    process.wait(timeout=3) if process == PROCESSES[-1] else process.terminate()
                except subprocess.TimeoutExpired:
                    process.terminate()
                process.wait(timeout=10)
        for log in LOGS:
            log.close()

if __name__ == '__main__':
    run()
