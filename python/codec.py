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
            return None
        (dataLen,) = self.lenUnpacker.unpack(data[:PbCodec.packetLengthSize])

        if dataLen > length:
            return None

        (msgId,) = self.idUnpacker.unpack(data[PbCodec.packetLengthSize:PbCodec.headerSize])
        return _pbref.ParseMessage(self.messageDescs[msgId], data[PbCodec.headerSize:]), dataLen
