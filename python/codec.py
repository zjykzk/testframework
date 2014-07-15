# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 16:18

class DefaultCodec(object):
    '''
    no operation of the data
    '''
    def encode(self, obj):
        return obj

    def decode(self, obj):
        return obj, len(obj)
