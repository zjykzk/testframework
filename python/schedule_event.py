# -*- coding: utf-8 -*-
# Author : zenk
# 2014-07-14 17:19

class EventCall(object):
    def handle(self):
        pass

# Return a task's ID number
class GetTid(EventCall):
    def handle(self):
        self.task.sendval = self.task.tid
        self.sched.schedule(self.task)

# Create a new task
class NewTask(EventCall):
    def __init__(self, target):
        self.target = target
    def handle(self):
        tid = self.sched.new(self.target)
        self.task.sendval = tid
        self.sched.schedule(self.task)

# Kill a task
class KillTask(EventCall):
    def __init__(self, tid):
        self.tid = tid
    def handle(self):
        task = self.sched.taskmap.get(self.tid,None)
        if task:
            task.target.close() 
            self.task.sendval = True
        else:
            self.task.sendval = False
        self.sched.schedule(self.task)

# Wait for a task to exit
class WaitTask(EventCall):
    def __init__(self, tid):
        self.tid = tid
    def handle(self):
        result = self.sched.waitforexit(self.task,self.tid)
        self.task.sendval = result
        # If waiting for a non-existent task,
        # return immediately without waiting
        if not result:
            self.sched.schedule(self.task)

# Wait for reading
class ReadWait(EventCall):
    def __init__(self, f):
        self.f = f
    def handle(self):
        fd = self.f.fileno()
        self.sched.waitforread(self.task,fd)

# Wait for writing
class WriteWait(EventCall):
    def __init__(self, f):
        self.f = f
    def handle(self):
        fd = self.f.fileno()
        self.sched.waitforwrite(self.task, fd)

def Accept(sock):
    yield ReadWait(sock)
    yield sock.accept()

def Send(sock, buffer):
    while buffer:
        yield WriteWait(sock)
        len = sock.send(buffer)
        buffer = buffer[len:]

def Recv(sock,maxbytes):
    yield ReadWait(sock)
    yield sock.recv(maxbytes)
