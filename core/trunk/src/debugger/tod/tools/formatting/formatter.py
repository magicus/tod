from tod.tools.formatting import *;

class TODObject:
    "A reconstituted TOD object"
    robj = None
    
    def __init__(self, robj):
        self.robj = robj

    def __str__(self):
        return str(self.robj)
    
    def __coerce__(self, other):
        return None
    
    def __radd__(self, other):
        return other+str(self)
    
    def __getattr__(self, name):
        #print "get: "+name
        return self.robj.get(name)
    
class PyObjectFormatter(IPyObjectFormatter):
    "A function that takes a TODObject and returns a string"
    func = None
    
    def __init__(self, func):
        self.func = func
    
    def format(self, robj):
        o = TODObject(robj)
        return str(self.func(o))
    
class PyFormatterFactory(IPyFormatterFactory):
    def createFormatter(self, func):
        return PyObjectFormatter(func)
    
    def createTODObject(self, o):
        return TODObject(o)
    
factory = PyFormatterFactory()
