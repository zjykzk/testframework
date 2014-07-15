# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 09:37
import os
import socket

import logger
import schedule_event

class Client(object):
    def __init__(self, addr, codec):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(addr)
        self.sock = s
        self.readBuf = ''
        self.writeBuf = ''
        self.codec = codec

    def isConnect(self):
        return True if self.sock else False

    # TODO set sock to non-block
    def request(self, obj):
        self.writeBuf += self.codec.encode(obj)
        yield schedule_event.WriteWait(self.sock)
        len = self.sock.send(self.writeBuf)
        self.writeBuf = self.writeBuf[len:]

        # waiting the response
        while True:
            yield schedule_event.ReadWait(self.sock)
            buf = self.sock.recv(65536)
            if buf:
                self.readBuf += buf
            resp, consumeLen = self.codec.decode(self.readBuf)
            self.readBuf = self.readBuf[consumeLen:]
            if resp:
                yield resp
                break