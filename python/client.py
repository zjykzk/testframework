# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 09:37
import errno
import os
import socket

import logger
import schedule_event

class Client(object):
    def __init__(self, addr, codec):
        self.readBuf = ''
        self.writeBuf = ''
        self.codec = codec
        self.addr = addr

        self.connect()

    def isConnect(self):
        return True if self.sock else False

    def connect(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(self.addr)
        s.setblocking(0)
        self.sock = s

    def reconnect(self):
        yield schedule_event.ConnectWait(self.sock)
        self.connect()

    # TODO set sock to non-block
    def request(self, obj):
        self.writeBuf += self.codec.encode(obj)
        while True:
            yield schedule_event.WriteWait(self.sock)
            try:
                len = self.sock.send(self.writeBuf)
                break
            except socket.error as e:
                if e.errno in [errno.EINTR, errno.EAGAIN, errno.EWOULDBLOCK]:
                    continue
                else:
                    raise e

        self.writeBuf = self.writeBuf[len:]

        # waiting the response
        while True:
            yield schedule_event.ReadWait(self.sock)
            try:
                buf = self.sock.recv(65536)
            except socket.error as e:
                if e.errno in [errno.EINTR, errno.EAGAIN, errno.EWOULDBLOCK]:
                    continue
                else:
                    raise e

            if buf:
                self.readBuf += buf
            resp, consumeLen = self.codec.decode(self.readBuf)
            if resp:
                self.readBuf = self.readBuf[consumeLen:]
                yield resp
                break
