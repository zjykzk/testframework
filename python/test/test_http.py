# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-30 15:00
import sys
sys.path.append("..")

import codec
import logger
import scheduler
import testcase

class Test(testcase.TestCase):
    def __init__(self, name):
        super(Test, self).__init__(None, 
                codec=codec.HttpCodec('localhost:8091', '/gm_tool/login'))
        self.name = name

    def setup(self):
        statusCode, resp = yield self.request('__game_area_id=-1&name=test1&pwd=test1')
        logger.debug('status code %s, content %s, len %s' % (statusCode, resp, len(resp)))
    
    def test(self):
        pass

if __name__ == '__main__':
    sch = scheduler.Scheduler()
    sch.new(testcase.driveTest(Test('login')))
    sch.mainloop()
