#------------------------

from win32 import win32file
import numpy as np
import time
#------------------------

comport: str = "COM5"
byte_size: int = 8
bound_rate: int = 9600

#------------------------

hFile = win32file.CreateFile(   comport, 
                                win32file.GENERIC_READ | win32file.GENERIC_WRITE, 
                                0,
                                None,
                                win32file.OPEN_EXISTING, 
                                0,
                                None)

comDCB = win32file.DCB()

comDCB.ByteSize     = byte_size
comDCB.Parity       = win32file.NOPARITY
comDCB.StopBits     = win32file.ONESTOPBIT
comDCB.BaudRate     = bound_rate

win32file.SetCommState(hFile, comDCB)
win32file.SetupComm(hFile, 4096, 4096)
# for j in range(2):
#     #4 байтовый int
#     for h in range(2):
#         buffer = np.array([0]*10, dtype=np.int32)
#
#     #color mem test
#         for i in range(8):

buffer = np.array([0], dtype=np.uint32)
for i in range(256):
    buffer[0] = 0
    # buffer[0] |= (255 & 0xFF) << 24
    buffer[0] |= (120 & 0xFF) << 16
    buffer[0] |= (44 & 0xFF) << 8
    buffer[0] |= (4 & 0xFF)
    data = buffer.tobytes()
    print(buffer, data, len(data))
    win32file.WriteFile(hFile, data, None)
    # time.sleep(0.02)

#------------------------
