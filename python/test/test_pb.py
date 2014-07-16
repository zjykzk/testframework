# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-16 15:23
import sys
sys.path.append("..")

import codec
import logger
from proto import test_pb2
import scheduler
import testcase

class EchoTest(testcase.TestCase):
    def __init__(self, name):
        super(EchoTest, self).__init__(None,codec=codec.PbCodec(
          { 1 : test_pb2._ECHO, },
          { test_pb2._ECHO : 1, }))
        self.name = name

    def setup(self):
        echo = test_pb2.Echo()
        echo.msg = "setup"
        resp = yield self.request(echo)
        logger.debug(self.name, "setup recv ", resp.msg)

    def test(self):
        echo = test_pb2.Echo()
        echo.msg = "test pb"
        resp = yield self.request(echo)
        logger.debug(self.name, "recv ", resp.msg)
        echo.msg = "test pb1"
        resp = yield self.request(echo)
        logger.debug(self.name, "recv ", resp.msg)
        echo.msg = "test pb2"
        resp = yield self.request(echo)
        logger.debug(self.name, "recv ", resp.msg)

if __name__ == "__main__":
    sch = scheduler.Scheduler()
    e = EchoTest('')
    e.a = 1
    sch.new(testcase.driveTest(EchoTest("echo1"), True))
    sch.new(testcase.driveTest(EchoTest("echo2"), False))
    sch.new(testcase.driveTest(EchoTest("echo3"), False))
    sch.mainloop()
