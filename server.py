import select
import socket


def handle(sock, addr):
    try:
        data = sock.recv(1024)  # Should be ready
    except ConnectionError:
        print(f"Client suddenly closed while receiving")
        return False
    print(f"Received {data.decode('utf-8')} from: {addr}")
    if not data:
        print("Disconnected by", addr)
        return False
    data = input()
    print(f"Send: {data} to: {addr}")
    try:
        sock.send(bytes(data, encoding='utf-8'))  # Hope it won't block
    except ConnectionError:
        print(f"Client suddenly closed, cannot send")
        return False
    return True


HOST, PORT = "26.38.211.168", 5000
if __name__ == "__main__":
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as serv_sock:
        serv_sock.bind((HOST, PORT))
        serv_sock.listen(1)
        # serv_sock.setblocking(False)
        inputs = [serv_sock]
        outputs = []
        while True:
            print("Waiting for connections or data...")
            readable, writeable, exceptional = select.select(inputs, outputs, inputs)
            for sock in readable:
                if sock == serv_sock:
                    sock, addr = serv_sock.accept()  # Should be ready
                    print("Connected by", addr)
                    # sock.setblocking(False)
                    inputs.append(sock)
                else:
                    addr = sock.getpeername()
                    if not handle(sock, addr):
                        # Disconnected
                        inputs.remove(sock)
                        if sock in outputs:
                            outputs.remove(sock)
                        sock.close()
