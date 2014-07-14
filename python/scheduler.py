from Queue import Queue
import select
import types

from schedule_event import EventCall
from task import Task

class Scheduler(object):
    def __init__(self):
        self.ready   = Queue()   
        self.taskmap = {}        

        # Tasks waiting for other tasks to exit
        self.exit_waiting = {}

        # I/O waiting
        self.read_waiting = {}
        self.write_waiting = {}
        
    def new(self,target):
        newtask = Task(target)
        self.taskmap[newtask.tid] = newtask
        self.schedule(newtask)
        return newtask.tid

    def exit(self,task):
        print "Task %d terminated" % task.tid
        del self.taskmap[task.tid]
        # Notify other tasks waiting for exit
        for task in self.exit_waiting.pop(task.tid,[]):
            self.schedule(task)

    def waitforexit(self,task,waittid):
        if waittid in self.taskmap:
            self.exit_waiting.setdefault(waittid,[]).append(task)
            return True
        else:
            return False

    # I/O waiting
    def waitforread(self, task, fd):
        self.read_waiting[fd] = task

    def waitforwrite(self,task,fd):
        self.write_waiting[fd] = task

    def iopoll(self, timeout):
        if self.read_waiting or self.write_waiting:
           r,w,e = select.select(self.read_waiting,
                                 self.write_waiting, [], timeout)
           for fd in r: self.schedule(self.read_waiting.pop(fd))
           for fd in w: self.schedule(self.write_waiting.pop(fd))

    def iotask(self):
        while True:
            if self.ready.empty():
                self.iopoll(None)
            else:
                self.iopoll(0)
            yield

    def schedule(self,task):
        self.ready.put(task)

    def mainloop(self):
         self.new(self.iotask())
         while self.taskmap:
             task = self.ready.get()
             try:
                 result = task.run()
                 if isinstance(result, EventCall):
                     result.task  = task
                     result.sched = self
                     result.handle()
                     continue
             except StopIteration:
                 self.exit(task)
                 continue
             self.schedule(task)
