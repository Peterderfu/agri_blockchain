from http.server import HTTPServer, BaseHTTPRequestHandler
import ssl
hostname = 'localhost'
hostport = 8443
PATH_TO_CERTCHAIN = r'PATH\TO\CERTCHAIN'
PATH_TO_PRIVATEKEY = r'PATH\TO\PRIVATEKEY'

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'Hello, world!')
    def do_POST(self):
        pass
    
httpd = HTTPServer((hostname, hostport), SimpleHTTPRequestHandler)

httpd.socket = ssl.wrap_socket (httpd.socket, 
        keyfile=PATH_TO_PRIVATEKEY, 
        certfile=PATH_TO_CERTCHAIN, server_side=True)

httpd.serve_forever()