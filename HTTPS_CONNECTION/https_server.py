# -*- coding: utf-8 -*-
from http.server import HTTPServer, BaseHTTPRequestHandler
import ssl, os,re,posixpath,urllib,time,logging
from datetime import datetime

hostname = 'localhost'
hostport = 80
SITE_ROOT = 'https://' + hostname + ':' + str(hostport)
PATH_TO_CERTCHAIN = r'files/cert.pem'
PATH_TO_PRIVATEKEY = r'files/key.pem'
#openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -nodes
JPEG_MAGIC = bytearray(b'\xff\xd8\xff')

FORMAT = '%(asctime)-15s %(clientip)s %(user)-8s %(message)s'
logging.basicConfig(format=FORMAT,datefmt='%Y-%m-%d %H:%M:%S',filename='log.txt')


class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'Hello')
        
    def do_POST(self):
        result= self.deal_post_data()
        if result == True:
            self.send_response(200)
        else:
            self.send_error(400, "Image upload failed")
        self.end_headers()
                
    def deal_post_data(self):
        content_type = self.headers['content-type']
        if not content_type:
            self.log_message("Content-Type header doesn't contain boundary")
            return False
        boundary = content_type.split("=")[1].encode()
        self.log_message("Boundary found: %s", boundary)
        remainbytes = int(self.headers['content-length'])
        self.log_message("Content-length : %s", str(remainbytes))
        line = self.rfile.readline()
        remainbytes -= len(line)
        if not boundary in line:
            self.log_message("Content NOT begin with boundary")
            return False
        
        line = self.rfile.readline()
        remainbytes -= len(line)
        fn = re.findall(r'Content-Disposition.*name="file"; filename="(.*)"', line.decode())
        if not fn:
            self.log_message("Uploaded file name : %s", fn)
            return False
        
        date_time = datetime.fromtimestamp(time.time())
        fn = "".join([date_time.strftime("%Y_%d_%m_%H_%M_%S"),".jpg"])
        path = self.translate_path(self.path)
        fn = os.path.join(path, fn)
        line = self.rfile.readline() #read line of "Content-Type:"
        remainbytes -= len(line)
        line = self.rfile.readline() #read next line
        remainbytes -= len(line)
        
        while line.startswith(b'\r'):#skip empty line(consisting only \r\n)
            line = self.rfile.readline()
            remainbytes -= len(line)
        
        try:
            out = open(fn, 'wb')
        except IOError:
            self.log_message("Can't create file to write, do you have permission to write?")
            return False
                
        preline  = line
        while remainbytes > 0:
            line = self.rfile.readline()
            remainbytes -= len(line)
            if boundary in line:
                preline = preline[0:-1]
                if preline.endswith(b'\r'):
                    preline = preline[0:-1]
                out.write(preline)
                out.close()
                self.log_message("File '%s' upload success!" , fn)
                return True
            else:
                if (bytearray(preline)!=bytearray(b'\x0d\x0a')):
                    out.write(preline)
                preline = line
        self.log_message("Unexpect Ends of data.")
        return False
    
    def translate_path(self, path):
        """Translate a /-separated PATH to the local filename syntax.
        Components that mean special things to the local file system
        (e.g. drive or directory names) are ignored.  (XXX They should
        probably be diagnosed.)
        """
        # abandon query parameters
        path = path.split('?',1)[0]
        path = path.split('#',1)[0]
        path = posixpath.normpath(urllib.parse.unquote(path))
        words = path.split('/')
        words = [_f for _f in words if _f]
        path = os.getcwd()
        for word in words:
            drive, word = os.path.splitdrive(word)
            head, word = os.path.split(word)
            if word in (os.curdir, os.pardir): continue
            path = os.path.join(path, word)
        return path
httpd = HTTPServer((hostname, hostport), SimpleHTTPRequestHandler)
# httpd.socket = ssl.wrap_socket (httpd.socket, 
#         keyfile=PATH_TO_PRIVATEKEY, 
#         certfile=PATH_TO_CERTCHAIN, server_side=True)
httpd.serve_forever()
