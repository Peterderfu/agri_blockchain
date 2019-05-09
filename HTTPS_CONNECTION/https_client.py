import socket, ssl,requests
hostname = '192.168.157.129'
# PROTOCOL_TLS_CLIENT requires valid cert chain and hostname
# context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
# context.options |= ssl.OP_NO_TLSv1
# context.options |= ssl.OP_NO_TLSv1_1
# context.load_verify_locations('path/to/cabundle.pem')
# 
# with socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0) as sock:
#     with context.wrap_socket(sock, server_hostname=hostname) as ssock:
#         print(ssock.version())
# url = 'https://192.168.157.129:8443'
url = 'http://18.219.71.129:5566'

multiple_files = {'file': ('pic.jpg', open('files/pic.jpg', 'rb'), 'image/jpeg')}

r = requests.post(url, files=multiple_files, verify=False)
