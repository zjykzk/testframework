# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-15 16:21
import sys
sys.path.append("..")

import logger
import scheduler
import testcase

class EchoTest(testcase.TestCase):
    def __init__(self, name):
        super(EchoTest, self).__init__()
        self.name = name

    def setup(self):
        resp = yield self.request("setup")
        logger.debug(resp)

    def test(self):
        resp = yield self.request("test")
        logger.debug(self.name, "recv ", resp)
        resp = yield self.request("test1")
        logger.debug(self.name, "recv ", resp)
        resp = yield self.request("test2")
        logger.debug(self.name, "recv ", resp)

if __name__ == "__main__":
    sch = scheduler.Scheduler()
    e = EchoTest('')
    e.a = 1
    sch.new(testcase.driveTest(EchoTest("echo1"), True))
    sch.new(testcase.driveTest(EchoTest("echo2"), True))
    sch.new(testcase.driveTest(EchoTest("echo3"), True))
    sch.mainloop()
