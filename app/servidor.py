import socket

mi_socket = socket.socket()  //generamos un socket con los valores por defecto
mi_socket.bind(('localhost', 8000))
mi_socket.listen(5)  //numero de conexiones en cola

while True:
    conexion, addr = mi_socket.accept()
    print "Nueva conexion establecida"
    print addr

    conexion.send("hola")
    conexion.close()
