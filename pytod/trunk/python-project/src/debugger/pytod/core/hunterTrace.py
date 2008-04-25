#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['hT', 'Descriptor']

import sys
import dis
import re
import time
import inspect
import thread
import xdrlib
from threading import settrace

objects = {
           'class':0,
           'method':1,
           'attribute':2,
           'function':3,
           'local':4,
           'probe':5,
           'thread':6
           }

dataTypes = {
             int.__name__:0,
             str.__name__:1,
             float.__name__:2,
             long.__name__:3,
             bool.__name__:4,
             tuple.__name__:5,
             list.__name__:6,
             dict.__name__:7
             }

events = {
          'register':0,
          'call':1,
          'set':2,
          'return':3
          }

class Diccionario(dict):

    def __setitem__(self, k, v):
        dict.__setitem__(self,k,v)

    def __update__(self, d, parentId):
        for k,v in d.items():
            #se debe registrar argumento self?
            if not self.has_key(k):
                if not k == 'self':
                    self[k] = v
                    hT.packer.reset()
                    print hT.events['register'],
                    hT.packer.pack_int(hT.events['register'])
                    print hT.objects['local'],
                    hT.packer.pack_int(hT.objects['local'])
                    #print 'id =',v,
                    print v,
                    hT.packer.pack_int(v)
                    #print ',parent id=',parentId,
                    print parentId,
                    hT.packer.pack_int(v)
                    #print ',name =',k
                    print k
                    hT.packer.pack_string(k)
                    

    def __updateAttr__(self, d, parentId):
        for k,v in d.items():
            #se debe registrar argumento self?
            if not self.has_key(k):
                if not k == 'self':
                    self[k] = v
                    print hT.events['register'],
                    print hT.objects['attribute'],
                    print 'id =',v,
                    print ',name =',k,
                    print ',parent id=',parentId

class IdGenerator(object):
    
    def __init__(self):
        self.id = 1

    def __get__(self):
        return self.id

    def __next__(self):
        self.id = self.id + 1
        return


class Descriptor(object):

    def __setattr__(self, name, value):
        frame = sys._getframe()
        currentDepth = hT.__getDepthFrame__(frame)
        currentTimeStamp = hT.__timeStampFrame__(frame)
        parentTimeStamp = hT.__getTimeStampParentFrame__(frame, currentTimeStamp)
        threadId = hT.__getThreadId__(thread.get_ident())
        id = hT.Id.__get__()
        key = type(self).__name__
        key = hT.__getClassKey__(key)
        if key == None:
            return
        obj = hT._class[key] 
        objId = obj.__getId__()
        obj.attribute.__updateAttr__({name:id},objId)
        hT.Id.__next__()
        print hT.events['set'],
        print hT.objects['attribute'],
        print 'value =',value,
        print 'id =',id,
        print 'target =',objId,
        print 'current depth =',currentDepth,
        print 'current time stamp = %11.9f'%(currentTimeStamp),
        print 'parent time stamp = %11.9f'%(parentTimeStamp),
        print 'current thread =',threadId
        #falta agregar el probeId, currentTimeStamp, etc...
        object.__setattr__(self, name, value)

class Class(object):

    def __init__(self, classId, code, lnotab):
        self.attribute = Diccionario()
        self.method = Diccionario()
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.id = classId

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __addMethod__(self,code,locals):
        for k,v in locals.iteritems():
            if inspect.isfunction(v):
                if not (k == '__module__'):
                    id = hT.Id.__get__()
                    self.method.update({k:id})
                    hT.Id.__next__()

class Function(object):

    def __init__(self, id, code, lnotab,args):
        self.locals = Diccionario()
        self.argument = Diccionario()
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.id = id
        self.__updateArgument__(args)

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __getLocals__(self):
        return self.locals

    def __getArgs__(self):
        return self.argument
    
    def __getArgsValues__(self, args, locals):
        argValues = {}
        for k in args.iterkeys():
            if locals.has_key(k):
                argValues[args[k]] = locals[k]
        #TODO: analizar caso para cuando sean tuple, list, dict
        return argValues

    def __updateArgument__(self, args):
        self.argument.__update__(args,self.id)
        
    def __registerLocals__(self, local):
        self.locals.__update__(local,self.id)



class Method(object):

    def __init__(self, id, code, lnotab, idClass, args):
        self.locals = Diccionario()
        self.argument = Diccionario()
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.idClass = idClass
        self.id = id
        self.__updateArgument__(args)

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __getLocals__(self):
        return self.locals

    def __getTarget__(self):
        return self.idClass

    def __getArgs__(self):
        return self.argument
    
    def __getArgsValues__(self, args, locals):
        argValues = {}
        for k in args.iterkeys():
            if locals.has_key(k):
                argValues[args[k]] = locals[k]
        #TODO: analizar caso para cuando sean tuple, list, dict
        return argValues

    def __updateArgument__(self, args):
        self.argument.__update__(args,self.id)
        
    def __registerLocals__(self, local):
        self.locals.__update__(local,self.id)
        
    

class hunterTrace(object):

    def __init__(self, Id, probeId, threadId, packer):
        self._class = {}
        self._function = {}
        self._method = {}
        self._probe = {}
        self._thread = {}
        self.objects = objects
        self.dataTypes = dataTypes
        self.events = events
        self.Id = Id
        self.probeId = probeId
        self.threadId = threadId
        self.packer = packer
        self.methodPattern = "\A__.*(__)$"

    def __addClass__(self, id, lnotab, code):
        objClass = Class(id,code,lnotab)
        self._class.update({code:objClass})
        return objClass

    def __addFunction__(self, id, lnotab, code, args):
        self._function.update({code:Function(id,code,lnotab,args)})

    def __addMethod__(self, id, lnotab, code, idClass, args):
        self._method.update({code:Method(id,code,lnotab,idClass,args)})

    def __addProbe__(self, probeId, currentLasti, parentId):
        self._probe.update({(currentLasti,parentId):probeId})

    def __addThread__(self, threadId, threadSysId):
        self._thread.update({threadSysId:threadId})
        
    def __createlnotab__(self, code):
        lnotab = {}
        if hasattr(code, 'co_lnotab'):
            table = code.co_lnotab
            index = 0
            last_index = None
            for i in range(0, len(table), 2):
                index = index + ord(table[i])
                if last_index == None:
                    last_index = index
                else:
                    lnotab.update({index:tuple([last_index,index-1])})                
                    last_index = index
            lnotab.update({len(code.co_code)-1:tuple([last_index,len(code.co_code)-1])})                
        return lnotab        

    def __inClass__(self, _class):
        if self._class.has_key(_class):
            return True
        return False

    def __inFunction__(self, _function):
        if self._function.has_key(_function):
            return True
        return False

    def __inMethod__(self, _method):
        if self._method.has_key(_method):
            return True
        return False

    def __isClassKey__(self, codeClass):
        for k in self._class.iterkeys():
            if k == codeClass:
                return self._class[k]
        return None

    def __isFunctionKey__(self, codeFunction):
        for k in self._function.iterkeys():
            if k == codeFunction:
                return self._function[k]
        return None

    def __isMethodKey__(self, codeMethod):
        for k in self._method.iterkeys():
            if k == codeMethod:
                return self._method[k]
        return None
    
    def __getClassKey__(self, nameClass):
        for k,v in self._class.iteritems():
            if k.co_name == nameClass:
                return k
        return None
    
    def __getObjectId__(self, code):
        if self.__isClassKey__(code):
            return self._class[code].__getId__()
        elif self.__isFunctionKey__(code):
            return self._function[code].__getId__()
        elif self.__isMethodKey__(code):
            return self._method[code].__getId__()
        return -1

    def __getObject__(self, code):
        if self.__isFunctionKey__(code):
            return self._function[code]
        elif self.__isMethodKey__(code):
            return self._method[code]
        return None

    def __getThreadId__(self, threadSysId):
        if not hT._thread.has_key(threadSysId):
            threadId = self.__registerThread__(threadSysId)
        else:
            threadId = self._thread[threadSysId]
        return threadId


    def __getargs__(self, code):
        args = {}
        for i in range(code.co_argcount):
            args.update({code.co_varnames[i]:i})
        return args

    def __getpartcode__(self,code, bound):
        i = bound[0]
        n = bound[1]
        co_code = code.co_code
        store_fast = {}    
        while i < n:
            op = ord(co_code[i])
            opname = dis.opname[op]
            i = i + 1
            if op >= dis.HAVE_ARGUMENT:
                value = ord(co_code[i]) + ord(co_code[i+1])*256
                i = i + 2
                if op in dis.haslocal and opname == 'STORE_FAST':
                    i_arg_value = code.co_varnames[value]
                    store_fast.update({i_arg_value:value})
        return store_fast   

    def __getDepthFrame__(self, frame):
        return frame.f_locals['__depthFrame__']

    def __depthFrame__(self, frame):
        frameBack = frame.f_back
        if frameBack.f_locals.has_key('__depthFrame__'):
            currentDepth = frameBack.f_locals['__depthFrame__']
            frame.f_locals['__depthFrame__'] = currentDepth + 1
        else:
            frame.f_locals['__depthFrame__'] = 0
        return frame.f_locals['__depthFrame__']
    
    def __timeStampFrame__(self, frame):
        if not frame.f_locals.has_key('__timeStampFrame__'): 
            frame.f_locals['__timeStampFrame__'] = time.time()
        return frame.f_locals['__timeStampFrame__']
        
    def __getTimeStampParentFrame__(self, frame, currentTimeStamp):
        frameBack = frame.f_back 
        if frameBack.f_locals.has_key('__timeStampFrame__'):
            return frameBack.f_locals['__timeStampFrame__']
        return currentTimeStamp
    
    def __printchangevar__(self, code, local, locals, obj, currentLasti, depth, parentTimeStampFrame, threadId):
        attr = obj.__getLocals__()
        objId = obj.__getId__()
        for i in local.iterkeys():
            if not attr.has_key(i) or not locals.has_key(i):
                return
            #registramos un nuevo probe o utilizamos el que existe
            if not self._probe.has_key((currentLasti,objId)):
                probeId = self.__registerProbe__(currentLasti,objId)
            else:
                probeId = self._probe[(currentLasti,objId)]
            #preguntar el tipo de la variable para poder
            #"socketear"
            print self.events['set'],
            print self.objects['local'],
            print 'id =',attr[i],
            print ', value =',locals[i],
            print ',probe id =',probeId,
            #print ',probe(id =',probeId,', f_lasti =',f_lasti,', id =',objId,')',
            print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
            print ',current depth =',depth,
            print ', current time stamp = %11.9f'%(time.time()),
            print ',current thread =',threadId

    def __printCallMethod__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame, threadId):
        obj = self.__getObject__(code)
        methodId = obj.__getId__()
        classId = obj.__getTarget__()
        args = obj.__getArgs__()
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti        
        #registramos un nuevo probe
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        print self.events['call'],
        print self.objects['method'],
        print 'id =',methodId,
        print 'parent id =',parentId,
        print 'target =',classId,
        print args,', args =',
        #print ',llamado desde (',parentId,',f_lasti =',f_lasti,')',
        print ',probe id =',probeId,
        #print ',probe(id =',probeId,', f_lasti =',current_lasti,', id =',methodId,')',
        print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
        print ',current depth =',depth,
        print ', current time stamp = %11.9f'%(currentTimeStamp),      
        print ',current thread =',threadId

        
    def __printCallFunction__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame, threadId):
        obj = self.__getObject__(code)
        functionId = obj.__getId__()
        args = obj.__getArgs__()
        argsValue = obj.__getArgsValues__(args,frame.f_locals)
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti
        #registramos un nuevo probe o lo rescatamos
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        self.packer.reset()
        print self.events['call'],
        self.packer.pack_int(self.events['call'])
        print self.objects['function'],
        self.packer.pack_int(self.objects['function'])
        #print ' id =',functionId,
        print functionId,
        self.packer.pack_int(functionId)
        #print ', parent id =',parentId,
        print parentId,
        self.packer.pack_int(parentId)
        #print ',args =',args,
        print len(argsValue),
        for k,v in argsValue.iteritems():
            print k,
            self.packer.pack_int(k)
            print v,
            #TODO: en estos momentos asumimos todos enteros
            self.packer.pack_int(v)
            
        #print ',probe id =',probeId,
        print probeId,
        self.packer.pack_int(probeId)
        #print ',probe(id =',probeId,', f_lasti =',current_lasti,', id =',id,')',
        #print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
        print '%11.9f'%(parentTimeStampFrame),
        self.packer.pack_double(parentTimeStampFrame)        
        #print ',current depth =',depth,
        print depth,
        self.packer.pack_int(depth)
        #print', current time stamp = %11.9f'%(currentTimeStamp),
        print '%11.9f'%(currentTimeStamp),
        self.packer.pack_double(currentTimeStamp)
        #print ',current thread =',threadId
        print threadId
        self.packer.pack_int(threadId)

    def __printReturn__(self, frame, arg):
        f_back = frame.f_back
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti
        #registramos un nuevo probe o lo rescatamos
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        print self.events['return'],
        print ' value',arg,
        print ',probe id =',probeId,
        print ',hasThrown =', True
    
    def __register__(self, obj, local):
        objId = obj.__getId__()
        obj.__registerLocals__(local)

    def __registerClass__(self, code, locals):
        classId = self.Id.__get__()
        className = code.co_name
        #HINT: ver como recuperar las herencias de esta clase 
        classBases = None
        self.packer.reset()
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['class'],
        self.packer.pack_int(self.objects['class'])
        #print ', id =',classId,
        print classId,
        self.packer.pack_int(classId)
        #print 'name',className,
        print className,
        self.packer.pack_string(className)
        #print ', superclass = ',classBases
        print classBases
        self.packer.pack_int(0)
        objClass = self.__addClass__(classId,self.__createlnotab__(code),code)
        self.Id.__next__()
        #se deben registrar los metodos asociados 
        #como atributos de la clase
        objClass.__addMethod__(code,locals)

    def __registerMethod__(self, code, methodId, classId, args):
        self.packer.reset()
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['method'],
        self.packer.pack_int(self.objects['method'])
        #print 'id=',methodId,
        print methodId,
        self.packer.pack_int(methodId)
        #print ',class id=',classId,
        print classId,
        self.packer.pack_int(classId)
        #print ',name=',code.co_name,
        print code.co_name,
        self.packer.pack_string(code.co_name)
        print len(args),
        self.packer.pack_int(len(args))
        for k,v in args.iteritems():
            print k,
            self.packer.pack_string(k)
            print v,
            self.packer.pack_int(v)
        print self.packer.get_buffer()
        self.__addMethod__(
                           methodId,
                           self.__createlnotab__(code),
                           code,
                           classId,
                           args)

    def __registerFunction__(self, code):
        functionId = self.Id.__get__()
        args = self.__getargs__(code)
        self.packer.reset()
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['function'],        
        self.packer.pack_int(self.objects['function'])
        #print 'id =',functionId,
        print functionId,
        self.packer.pack_int(functionId)
        #print ',name =',code.co_name,
        print code.co_name,
        self.packer.pack_string(code.co_name)
        print len(args),
        self.packer.pack_int(len(args))
        for k,v in args.iteritems():
            print k,
            self.packer.pack_string(k)
            print v,
            self.packer.pack_int(v)
        #print ', args =',args
        print self.packer.get_buffer()
        self.__addFunction__(
                             functionId,
                             self.__createlnotab__(code),
                             code,
                             args)
        self.Id.__next__()

    def __registerProbe__(self, currentLasti, parentId):
        probeId = self.probeId.__get__()
        self.__addProbe__(probeId,currentLasti,parentId)
        self.packer.reset()
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['probe'],
        self.packer.pack_int(self.objects['probe'])
        #print ',probe id=',probeId,
        print probeId,
        self.packer.pack_int(probeId)
        #print ',current lasti =',currentLasti,
        print currentLasti,
        self.packer.pack_int(currentLasti)
        #print ',parent id =', parentId
        print parentId
        self.packer.pack_int(parentId)
        
        self.probeId.__next__()
        return probeId
    
    def __registerThread__(self, threadSysId):
        threadId = self.threadId.__get__()
        self.__addThread__(threadId,threadSysId)
        self.packer.reset()
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['thread'],
        self.packer.pack_int(self.objects['thread'])
        #print 'thread id =',threadId,
        print threadId,
        self.packer.pack_int(threadId)
        #print 'thread sys id =',threadSysId
        print threadSysId
        self.packer.pack_int(threadSysId)
        self.threadId.__next__()
        return threadId

    def __trace__(self, frame, event, arg):
        if frame.f_back == None:
            return
        lineno = frame.f_lineno
        code = frame.f_code
        locals = frame.f_locals
        globals = frame.f_globals
        #profundidad del frame
        depth = self.__depthFrame__(frame)
        #se marca frame con timestamp
        currentTimeStamp = self.__timeStampFrame__(frame)
        #se obtiene timestamp de frame padre
        parentTimeStampFrame = self.__getTimeStampParentFrame__(
                                                    frame, 
                                                    currentTimeStamp)
        threadId = self.__getThreadId__(thread.get_ident())
        if event == "call":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            #se registra el thread si es que no existe
            if code.co_name == '__init__':
                id = self.Id.__get__()
                locals['self'].__dict__.update({'__pyTOD__':id})
                self.Id.__next__()
                #aca se sacan las bases de la clase la cual
                #se ha instanciado
                #TODO: encontrar una mejor forma de hacerlo
                #ineficiente!!..quizas interviniendo la llamada
                #de la super clase?
                print type(locals['self']).__bases__
            #si self esta en locals estamos en un metodo
            if locals.has_key('self'):
                if not self.__inMethod__(code):
                    key = type(locals['self']).__name__
                    key = hT.__getClassKey__(key)
                    if key == None:
                        return
                    if not hT._class.has_key(key):
                        return
                    idClass = hT._class[key].__getId__()
                    if not hT._class[key].method.has_key(code.co_name):
                        return
                    id = hT._class[key].method[code.co_name]
                    args = self.__getargs__(code)
                    self.__registerMethod__(code,id,idClass,args)
                self.__printCallMethod__(
                                         code,
                                         frame,
                                         depth,
                                         currentTimeStamp,
                                         parentTimeStampFrame,
                                         threadId)
            else:
                #verificamos si es una funcion
                if globals.has_key(code.co_name):
                    if inspect.isfunction(globals[code.co_name]):
                        if not self.__inFunction__(code):
                            self.__registerFunction__(code)
                    self.__printCallFunction__(
                                               code,
                                               frame,
                                               depth,
                                               currentTimeStamp,
                                               parentTimeStampFrame,
                                               threadId)       
            return self.__trace__
        elif event == "line":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            obj = self.__getObject__(code)
            if obj == None:
                return
            lnotab = obj.__getLnotab__()
            if lnotab.has_key(frame.f_lasti):
                local = self.__getpartcode__(code,lnotab[frame.f_lasti])
                self.__register__(obj,local)
                #imprimiendo los cambios de valores, con su respectivo id
                self.__printchangevar__(
                                        code,
                                        local,
                                        locals,
                                        obj,
                                        frame.f_lasti,
                                        depth,
                                        parentTimeStampFrame,
                                        threadId)
            return self.__trace__
        elif event == "return":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            if locals.has_key('__init__'):
                #registramos la definicion de la clase
                if not self.__inClass__(code):
                    self.__registerClass__(code,locals)
            else:
                obj = self.__getObject__(code)
                if obj == None:
                    return
                lnotab = obj.__getLnotab__()
                if lnotab.has_key(frame.f_lasti):
                    local = self.__getpartcode__(code,lnotab[frame.f_lasti])
                    self. __register__(obj,local)
                    #imprimiendo los cambios de valores, con su respectivo id
                    self.__printchangevar__(
                                            code,
                                            local,
                                            locals,
                                            obj,
                                            frame.f_lasti,
                                            depth,
                                            parentTimeStampFrame,
                                            threadId)
            #registrar salida de return
            self.__printReturn__(frame, arg)

    def __printHunter__(self):
        print
        print 'clases'
        for k,v in hT._class.iteritems():
            print v.__dict__
            print
        print '======='
        
        print 'metodos'
        for k,v in hT._method.iteritems():
            print v.__dict__
            print
        print '======='
        
        print 'funcion'
        for k,v in hT._function.iteritems():
            print v.__dict__
            print
        print '======='

hT = hunterTrace(IdGenerator(),IdGenerator(),IdGenerator(),xdrlib.Packer())
#a cada nuevo thread se le define settrace
settrace(hT.__trace__)  
#asignamos settrace para nuestro espacio de trabajo
sys.settrace(hT.__trace__)

