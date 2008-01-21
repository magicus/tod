from tod.gui.formatter import *;

class TODObject:
    "A reconstituted TOD object"
    robj = None
    
    def __init__(self, robj):
        self.robj = robj
    
    def __getattr__(self, name):
        return self.robj.get(name)
    
class PyObjectFormatter(IPyObjectFormatter):
    
    "A function that takes a TODObject and returns a string"
    func = None
    
    def __init__(self, func):
        self.func = func
    
    def format(self, robj):
        o = TODObject(robj)
        return self.func(o)
    
class PyFormatterFactory(IPyFormatterFactory):
    def create(self, func):
        return PyObjectFormatter(func)
    
factory = PyFormatterFactory()
