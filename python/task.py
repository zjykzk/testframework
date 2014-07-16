# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-14 17:11
import types

from schedule_event import EventCall

class Task(object):
    taskid = 0
    def __init__(self, target):
        Task.taskid += 1
        # Task ID
        self.tid = Task.taskid

        # Target coroutine
        self.target = target        

        # Value to send
        self.sendval = None          

        # Call stack
        self.stack = []            

    def __str__(self):
        return "tid:%d,target:%s,sendval:%s,stack:%s" % (self.tid, self.target, 
            self.sendval, self.stack)

    # Run a task until it hits the next yield statement
    def run(self):
        while True:
            try:
                result = self.target.send(self.sendval)
                if isinstance(result, EventCall): return result
                if isinstance(result, types.GeneratorType):
                    self.stack.append(self.target)
                    self.sendval = None
                    self.target  = result
                else:
                    if not self.stack: return
                    self.sendval = result
                    self.target  = self.stack.pop()
            except StopIteration:
                if not self.stack: raise
                self.sendval = None
                self.target = self.stack.pop()

