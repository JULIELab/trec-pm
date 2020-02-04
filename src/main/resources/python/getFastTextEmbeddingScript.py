import fasttext

import sys
from struct import *


def decodeString(buffer):
    lengthBuffer = bytearray(4)
    buffer.readinto(lengthBuffer)
    length = int.from_bytes(lengthBuffer, 'big')
    content = bytearray(length)
    buffer.readinto(content)
    return content.decode("utf-8")

model = fasttext.load_model(sys.argv[1])

stdbuffer = sys.stdin.buffer
print("Script is ready")
while True:
    line = decodeString(stdbuffer)
    line = line.replace("\n", " ")
    if line.strip() == "exit":
        sys.exit(0)
    docVector = model.get_sentence_vector(line)
    bytes = pack('>' + 'd' * len(docVector), *docVector)
    sys.stdout.buffer.write(pack('>i', len(bytes)))
    sys.stdout.buffer.write(bytes)
    print(end='')
