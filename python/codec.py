# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 16:18
import struct

import google.protobuf.reflection as _pbref

class DefaultCodec(object):
    '''
    just return the data
    '''
    def encode(self, obj):
        return obj

    def decode(self, data):
        return data, len(data)

class HttpCodec(object):
    '''
    http
    '''
    CRLF = '\x0d\x0a'
    HeaderContentSep = CRLF * 2;
    ContentLengthHeader = 'Content-Length'
    def __init__(self, host, url):
        self.host = host
        self.header = HttpCodec.CRLF.join(['GET %s HTTP/1.1',
                'Host:%s' % host])
        self.url = url
        self.header = self.header + HttpCodec.CRLF * 2

    def encode(self, obj):
        return self.header % (self.url + '?' + obj)

    def __calcEatLength(self, obj, contentLen):
        obj.find(HttpCodec.HeaderContentSep) + 1 + len(HttpCodec.HeaderContentSep) + contentLen

    def decode(self, obj):
        if HttpCodec.CRLF not in obj:
            return (None, 0)

        crlf2idx = obj.find(HttpCodec.HeaderContentSep)
        if crlf2idx < 0:
            return (None, 0)

        datas = obj.split(HttpCodec.CRLF)
        for data in datas:
            if data.startswith('Content-Length'):
                contentLen = int(data.split(' ')[-1])
                content = obj[crlf2idx + len(HttpCodec.HeaderContentSep):] 
                if len(content) < contentLen:
                    return (None, 0)
                statusCode = datas[0].split(' ')[-2]
                return (statusCode, content), self.__calcEatLength(obj, contentLen)
        return (None, 0)

class PbCodec(object):
    '''
     packet format:
     +--------------+------------+-------------------+
     | 2 bytes      | 2 bytes    |      ...          |
     +--------------+------------+-------------------+
     |packet length | message id | message bytes     |
     +--------------+------------+-------------------+
    '''
    packetLengthSize = 2
    messageIdSize = 2
    headerSize = packetLengthSize + messageIdSize
    def __init__(self, messageDescs, descMessageIds):
        self.messageDescs = messageDescs
        self.descMessageIds = descMessageIds
        self.headerPacker = struct.Struct(">hh")
        self.lenUnpacker = struct.Struct(">h")
        self.idUnpacker = self.lenUnpacker

    def encode(self, obj):
        bytes = obj.SerializeToString()
        byteCount = len(bytes)
        length = PbCodec.headerSize + byteCount
        return self.headerPacker.pack(length, self.descMessageIds[obj.DESCRIPTOR]) + bytes

    def decode(self, data):
        length = len(data)
        if length < PbCodec.headerSize:
            return (None, 0)
        (dataLen,) = self.lenUnpacker.unpack(data[:PbCodec.packetLengthSize])

        if dataLen > length:
            return (None, 0)

        (msgId,) = self.idUnpacker.unpack(data[PbCodec.packetLengthSize:PbCodec.headerSize])
        return _pbref.ParseMessage(self.messageDescs[msgId], data[PbCodec.headerSize:]), dataLen
