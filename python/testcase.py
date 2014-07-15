# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 16:08
import client
import config

class TestCase(client.Client):
    def __init__(self):
        super(TestCase, self).__init__(config.addr, config.codec)

    def test(self):
        pass

    def setup(self):
        pass

def driveTest(testcase, isPress=False):
    print "setup....................."
    yield testcase.setup()

    if isPress:
        while True:
            print "testing.................."
            yield testcase.test()
    else:
        print "testing.................."
        yield testcase.test()
