"""Compila, inicia un servidor aislado y verifica los cuatro metodos con curl real."""
from pathlib import Path
import concurrent.futures
import shutil
import socket
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]


def main():
    classes = ROOT / 'classes'
    classes.mkdir(exist_ok=True)
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(classes),
                    *map(str, (ROOT / 'source').glob('*.java'))], check=True)
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    base = f'http://localhost:{port}'
    evidence = ROOT / 'evidencias'
    evidence.mkdir(exist_ok=True)
    transcript = []
    curl = shutil.which('curl.exe') or shutil.which('curl')
    assert curl, 'Se requiere curl'
    with (evidence / 'servidor.log').open('w', encoding='utf-8') as log:
        server = subprocess.Popen(['java', '-cp', str(classes), 'WebServer', str(port)],
                                  stdout=log, stderr=subprocess.STDOUT)
        try:
            for _ in range(100):
                try:
                    with socket.create_connection(('127.0.0.1', port), timeout=.2):
                        break
                except OSError:
                    if server.poll() is not None:
                        raise RuntimeError('El servidor termino antes de iniciar')
                    time.sleep(.1)
            else:
                raise RuntimeError('El servidor no inicio')

            def request(method, path, expected=200, data=None, record=True):
                args = ['-sS', '--max-time', '10', '-X', method]
                if data is not None:
                    args += ['-H', 'Content-Type: text/plain; charset=UTF-8', '--data', data]
                args += [base + path]
                result = subprocess.run([curl, *args, '-w', '\n%{http_code}'],
                                        capture_output=True, check=True)
                body, status = result.stdout.decode('utf-8').rsplit('\n', 1)
                assert int(status) == expected, (method, path, status, body)
                if record:
                    transcript.append('> curl.exe ' + subprocess.list2cmdline(args))
                    transcript.append(body.rstrip() + f'\nHTTP {status}\n')
                return body

            request('GET', '/status')
            for name in ['Alejandro,Perez,Castro,8.3', 'Arturo,Carmona,Trujillo,5.9',
                         'Mariana,Cuellar,Paz,7.6']:
                request('POST', '/alumnos', 201, name)
            assert 'Alejandro' in request('GET', '/alumnos/1')
            assert '5.9' in request('GET', '/alumnos/2')
            request('PUT', '/alumnos/2', data='Arturo,Carmona,Trujillo,8.2')
            assert '8.2' in request('GET', '/alumnos/2')
            request('DELETE', '/alumnos/1')
            request('GET', '/alumnos/1', 404)
            # Verificacion adicional: rutas, metodo, UTF-8 e IDs concurrentes.
            request('GET', '/task', 404, record=False)
            request('GET', '/alumnosXYZ', 404, record=False)
            request('PATCH', '/alumnos/2', 405, record=False)
            request('POST', '/alumnos', 400, 'incompleto', record=False)
            assert 'Pe\u00f1a' in request('POST', '/alumnos', 201,
                                           'Jos\u00e9,Pe\u00f1a,L\u00f3pez,9.1', record=False)
            request('PUT', '/alumnos/999', 404, 'Ana,Perez,Lopez,8', record=False)
            with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
                bodies = list(pool.map(lambda i: request('POST', '/alumnos', 201,
                                  f'Alumno{i},Perez,Lopez,8', record=False), range(12)))
            ids = [int(body.split()[1]) for body in bodies]
            assert len(set(ids)) == 12, 'Identificadores repetidos'
            assert min(ids) > 4, 'Se reutilizo un identificador'
        finally:
            server.terminate()
            server.wait(timeout=10)
    transcript.append('RESULTADO: pruebas CRUD, /status, UTF-8, errores e IDs concurrentes OK.')
    (evidence / 'pruebas_curl.txt').write_text('\n'.join(transcript) + '\n', encoding='utf-8')
    print('\n'.join(transcript))


if __name__ == '__main__':
    main()
