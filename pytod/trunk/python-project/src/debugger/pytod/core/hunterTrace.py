#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['hT', 'Descriptor']
th = False
import sys
import dis
import re
import time
import thread
import xdrlib
import socket
import inspect
from constantsObjects import events, objects, dataTypes, packXDRLib
from generatorId import generatorId
from objectClass import Class
from objectMethod import Method
from objectFunction import Function
if th:
    from threading import settrace
      

class hunterTrace(object):

    def __init__(self, Id, probeId, threadId, packer, host, port):
        self._class = {}
        self._function = {}
        self._method = {}
        self._probe = {}
        self._thread = {}
        self._socket = None
        self.events = events
        self.objects = objects
        self.dataTypes = dataTypes
        self.packXDRLib = packXDRLib
        self.Id = Id
        self.probeId = probeId
        self.threadId = threadId
        self.packer = packer
        self.host = host
        self.port = port
        self.FLAG_DEBUGG = False
        #self.isInitialTime = True
        self.methodPattern = "\A__.*(__)$"
        self.__socketConnect__()
        
    def __socketConnect__(self):
        self._socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self._socket.connect((self.host, self.port))
        except:
            print "TOD, esta durmiendo :("


    def __addClass__(self, id, lnotab, code):
        objClass = Class(self,id,code,lnotab)
        self._class.update({code:objClass})
        return objClass

    def __addFunction__(self, id, lnotab, code, args):
        self._function.update({code:Function(self,id,code,lnotab,args)})

    def __addMethod__(self, id, lnotab, code, idClass, args):
        self._method.update({code:Method(self,id,code,lnotab,idClass,args)})

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

    def __convertTimestamp__(self,timestamp):
        #the timestamp is converted to long
        return long(timestamp*1000000000)

    def __depthFrame__(self, frame):
        frameBack = frame.f_back
        if frameBack.f_locals.has_key('__depthFrame__'):
            currentDepth = frameBack.f_locals['__depthFrame__']
            frame.f_locals['__depthFrame__'] = currentDepth + 1
        else:
            frame.f_locals['__depthFrame__'] = 1
        return frame.f_locals['__depthFrame__']

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

    def __instantiation__(self, code, frame, depth, currentTimestamp, parentTimestampFrame, threadId):
        obj = self.__getObject__(code)
        instantiationId = obj.__getId__()
        classId = obj.__getTarget__()
        argsValue = obj.__getArgsValues__(frame.f_locals)
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti        
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        self.packer.reset()       
        self.packer.pack_int(self.events['instantiation'])
        self.packer.pack_int(instantiationId)
        self.packer.pack_int(len(argsValue))
        printArg = " "
        for value in argsValue:
            dataType = self.__getDataType__(value)
            self.packer.pack_int(dataType)
            printArg += str(dataType)
            printArg += " "
            printArg += str(self.__packValue__(dataType, value))
            printArg += " "
        self.packer.pack_int(probeId)
        self.packer.pack_hyper(parentTimestampFrame)
        self.packer.pack_int(depth)    
        self.packer.pack_hyper(currentTimestamp)
        self.packer.pack_int(threadId)
        if self.FLAG_DEBUGG:
            print self.events['instantiation'],
            print instantiationId,
            print len(argsValue), 
            print printArg,        
            print probeId,
            print parentTimestampFrame,
            print depth,
            print currentTimestamp,
            print threadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('


    
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

    def __getArgs__(self, code):
        return code.co_varnames[:code.co_argcount]

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
        try:
            return frame.f_locals['__depthFrame__']
        except:
            return -1
    
    def __getDataType__(self, value):
        dataType = 8
        try:
            valueType = value.__class__.__name__
            if self.dataTypes.has_key(value.__class__.__name__):
                dataType = self.dataTypes[value.__class__.__name__]
        except:
            return dataType
        finally:
            return dataType

    def __getTimestampFrame__(self, frame):
        if frame.f_locals.has_key('__timestampFrame__'):
            return frame.f_locals['__timestampFrame__']
        return 0

    def __getTimestampParentFrame__(self, frame):
        frameBack = frame.f_back 
        if frameBack.f_locals.has_key('__timestampFrame__'):
            return frameBack.f_locals['__timestampFrame__']
        return 0
    
    def __markTimestampFrame__(self, frame):
        if not frame.f_locals.has_key('__timestampFrame__'): 
            frame.f_locals['__timestampFrame__'] = self.__convertTimestamp__(time.time())
        return
    
    def __packValue__(self, dataType, value):
        if self.packXDRLib.has_key(dataType):
            methodName = self.packXDRLib[dataType]
            getattr(self.packer,'pack_%s'%methodName)(value)
            return value            
        else:
            #en estos momentos envíamos el tipo de dato
            #TODO: debieramos envíar el id del objeto
            self.packer.pack_int(dataType)
            return dataType
    
    def __localWrite__(self, code, local, locals, obj, currentLasti, depth, parentTimestampFrame, threadId):
        attr = obj.__getLocals__()
        behaviorId = self.__getObjectId__(code)
        depth = depth + 1
        for i in local.iterkeys():
            if not attr.has_key(i) or not locals.has_key(i):
                return
            if not self._probe.has_key((currentLasti,behaviorId)):
                probeId = self.__registerProbe__(currentLasti,behaviorId)
            else:
                probeId = self._probe[(currentLasti,behaviorId)]
            self.packer.reset()
            self.packer.pack_int(self.events['set'])
            self.packer.pack_int(self.objects['local'])
            self.packer.pack_int(attr[i])
            self.packer.pack_int(behaviorId)
            dataType = self.__getDataType__(locals[i])
            self.packer.pack_int(dataType)
            value = self.__packValue__(dataType, locals[i])
            self.packer.pack_int(probeId)
            self.packer.pack_hyper(parentTimestampFrame)
            self.packer.pack_int(depth)
            currentTimestamp = self.__convertTimestamp__(time.time()) 
            self.packer.pack_hyper(currentTimestamp)
            self.packer.pack_int(threadId)
            if self.FLAG_DEBUGG:            
                print self.events['set'],
                print self.objects['local'],
                print attr[i],
                print behaviorId,
                print dataType,
                print value,
                print probeId,
                print parentTimestampFrame,
                print depth,
                print currentTimestamp,
                print threadId
                raw_input()
            try:
                self._socket.sendall(self.packer.get_buffer())
            except:
                print 'TOD está durmiendo :-('            

    def __methodCall__(self, code, frame, depth, currentTimestamp, parentTimestampFrame, threadId):
        obj = self.__getObject__(code)
        methodId = obj.__getId__()
        classId = obj.__getTarget__()
        argsValue = obj.__getArgsValues__(frame.f_locals)
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti        
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['call'])
        self.packer.pack_int(self.objects['method'])
        self.packer.pack_int(methodId)
        self.packer.pack_int(classId)
        self.packer.pack_int(len(argsValue))
        printArg = " "
        for value in argsValue:
            dataType = self.__getDataType__(value)
            self.packer.pack_int(dataType)
            printArg += str(dataType)
            printArg += " "            
            printArg += str(self.__packValue__(dataType, value))
            printArg += " "
        self.packer.pack_int(probeId)
        self.packer.pack_hyper(parentTimestampFrame)
        self.packer.pack_int(depth)    
        self.packer.pack_hyper(currentTimestamp)
        self.packer.pack_int(threadId)
        if self.FLAG_DEBUGG:
            print self.events['call'],
            print self.objects['method'],
            print methodId,
            print classId,
            print len(argsValue),
            print printArg,
            print probeId,
            print parentTimestampFrame,
            print depth,
            print currentTimestamp,
            print threadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        
    def __functionCall__(self, code, frame, depth, currentTimestamp, parentTimestampFrame, threadId):
        obj = self.__getObject__(code)
        functionId = obj.__getId__()
        argsValue = obj.__getArgsValues__(frame.f_locals)
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        currentLasti = frame.f_lasti
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['call'])
        self.packer.pack_int(self.objects['function'])
        self.packer.pack_int(functionId)
        self.packer.pack_int(len(argsValue))
        printArg = " "
        for value in argsValue:
            dataType = self.__getDataType__(value)
            self.packer.pack_int(dataType)
            printArg += str(dataType)
            printArg += " "            
            printArg += str(self.__packValue__(dataType, value))
            printArg += " "     
        self.packer.pack_int(probeId)
        self.packer.pack_hyper(parentTimestampFrame)        
        self.packer.pack_int(depth)
        self.packer.pack_hyper(currentTimestamp)
        self.packer.pack_int(threadId)
        if self.FLAG_DEBUGG:
            print self.events['call'],
            print self.objects['function'],
            print functionId,
            print len(argsValue),
            print printArg,
            print probeId,
            print parentTimestampFrame,
            print depth,
            print currentTimestamp,
            print threadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('        

    def __behaviorExit__(self, frame, arg, depth, parentTimestampFrame, threadId):
        f_back = frame.f_back
        f_code = f_back.f_code
        parentId = self.__getObjectId__(f_code)
        behaviorId = self.__getObjectId__(frame.f_code)
        currentLasti = frame.f_lasti
        depth = depth + 1
        if not self._probe.has_key((currentLasti,parentId)):
            probeId = self.__registerProbe__(currentLasti,parentId)
        else:
            probeId = self._probe[(currentLasti,parentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['return'])
        self.packer.pack_int(behaviorId)
        dataType = self.__getDataType__(arg)
        self.packer.pack_int(dataType)
        value = self.__packValue__(dataType, arg)        
        self.packer.pack_int(0)
        self.packer.pack_int(probeId)
        self.packer.pack_hyper(parentTimestampFrame)        
        self.packer.pack_int(depth)
        currentTimestamp = self.__convertTimestamp__(time.time()) 
        self.packer.pack_hyper(currentTimestamp)
        self.packer.pack_int(threadId)
        if self.FLAG_DEBUGG:
            print self.events['return'],
            print behaviorId,
            print dataType,
            print value,
            print False,
            print probeId,
            print parentTimestampFrame,
            print depth,
            print currentTimestamp,
            print threadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('

    def __register__(self, obj, local):
        objId = obj.__getId__()
        obj.__registerLocals__(local)

    def __registerClass__(self, code, locals):
        classId = self.Id.__get__()
        className = code.co_name
        #HINT: ver como recuperar las herencias de esta clase 
        classBases = None
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['class'])
        self.packer.pack_int(classId)
        self.packer.pack_string(className)
        self.packer.pack_int(0)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['class'],
            print classId,
            print className,
            print classBases
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        objClass = self.__addClass__(classId,self.__createlnotab__(code),code)
        self.Id.__next__()
        objClass.__addMethod__(code,locals)

    def __registerMethod__(self, code, methodId, classId, args):
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['method'])
        self.packer.pack_int(methodId)
        self.packer.pack_int(classId)
        self.packer.pack_string(code.co_name)
        self.packer.pack_int(len(args))
        printArg = " "
        for i in range(len(args)):
            printArg += str(args[i])
            printArg += " "
            self.packer.pack_string(args[i])
            printArg += str(i)
            printArg += " "
            self.packer.pack_int(i)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['method'],
            print methodId,
            print classId,
            print code.co_name,
            print len(args),
            print printArg
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.__addMethod__(
                           methodId,
                           self.__createlnotab__(code),
                           code,
                           classId,
                           args)

    def __registerFunction__(self, code):
        functionId = self.Id.__get__()
        args = self.__getArgs__(code)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['function'])
        self.packer.pack_int(functionId)
        self.packer.pack_string(code.co_name)
        self.packer.pack_int(len(args))
        printArg = " " 
        for i in range(len(args)):
            printArg += str(args[i])
            printArg += " "
            self.packer.pack_string(args[i])
            printArg += str(i)
            printArg += " "
            self.packer.pack_int(i)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['function'],
            print functionId,
            print code.co_name,
            print len(args),
            print printArg
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('            
        self.__addFunction__(
                             functionId,
                             self.__createlnotab__(code),
                             code,
                             args)
        self.Id.__next__()

    def __registerProbe__(self, currentLasti, behaviorId):
        probeId = self.probeId.__get__()
        self.__addProbe__(probeId,currentLasti,behaviorId)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['probe'])
        self.packer.pack_int(probeId)
        self.packer.pack_int(behaviorId)
        self.packer.pack_int(currentLasti)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['probe'],
            print probeId,
            print behaviorId,
            print currentLasti            
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.probeId.__next__()
        return probeId
    
    def __registerThread__(self, threadSysId):
        threadId = self.threadId.__get__()
        self.__addThread__(threadId,threadSysId)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['thread'])
        self.packer.pack_int(threadId)
        self.packer.pack_int(threadSysId)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['thread'],
            print threadId,
            print threadSysId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.threadId.__next__()
        return threadId

    def __trace__(self, frame, event, arg):
        if frame.f_back == None:
            return
        lineno = frame.f_lineno
        code = frame.f_code
        locals = frame.f_locals
        globals = frame.f_globals
        depth = self.__depthFrame__(frame)
        self.__markTimestampFrame__(frame)
        threadId = self.__getThreadId__(thread.get_ident())
        if event == "call":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            parentTimestampFrame = self.__getTimestampParentFrame__(frame)
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
                    args = self.__getArgs__(code)
                    self.__registerMethod__(code,id,idClass,args)
                currentTimestamp = frame.f_locals['__timestampFrame__']
                if code.co_name == '__init__':
                    self.__instantiation__(
                                             code,
                                             frame,
                                             depth,
                                             currentTimestamp,
                                             parentTimestampFrame,
                                             threadId)

                else:
                    self.__methodCall__(
                                             code,
                                             frame,
                                             depth,
                                             currentTimestamp,
                                             parentTimestampFrame,
                                             threadId)
            else:
                #verificamos si es una funcion
                if globals.has_key(code.co_name):
                    if inspect.isfunction(globals[code.co_name]):
                        if not self.__inFunction__(code):
                            self.__registerFunction__(code)
                    currentTimestamp = frame.f_locals['__timestampFrame__']
                    self.__functionCall__(
                                               code,
                                               frame,
                                               depth,
                                               currentTimestamp,
                                               parentTimestampFrame,
                                               threadId)   
            return self.__trace__
        elif event == "line":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            parentTimestampFrame = self.__getTimestampFrame__(frame)
            obj = self.__getObject__(code)
            if obj == None:
                return
            lnotab = obj.__getLnotab__()
            if lnotab.has_key(frame.f_lasti):
                local = self.__getpartcode__(code,lnotab[frame.f_lasti])
                self.__register__(obj,local)
                self.__localWrite__(
                                        code,
                                        local,
                                        locals,
                                        obj,
                                        frame.f_lasti,
                                        depth,
                                        parentTimestampFrame,
                                        threadId)
            return self.__trace__
        elif event == "return":
            if re.search(self.methodPattern,code.co_name):
                if not code.co_name == '__init__':
                    return
            parentTimestampFrame = self.__getTimestampFrame__(frame)
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
                    self.__localWrite__(code,
                                            local,
                                            locals,
                                            obj,
                                            frame.f_lasti,
                                            depth,
                                            parentTimestampFrame,
                                            threadId)
                self.__behaviorExit__(frame,
                                     arg,
                                     depth,
                                     parentTimestampFrame,
                                     threadId)

    def __printHunter__(self):
        #cerrar socket
        #TODO: encontrar una manera mejor de hacer esto
        self._socket.close()
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

hT = hunterTrace(
                 generatorId(),
                 generatorId(),
                 generatorId(),
                 xdrlib.Packer(),
                 '127.0.0.1',
                 8058)

class Descriptor(object):

    def __setattr__(self, name, value):
        import sys
        frame = sys._getframe()
        code = frame.f_back.f_code
        currentLasti = frame.f_back.f_lasti
        currentDepth = hT.__getDepthFrame__(frame.f_back)
        currentDepth = currentDepth + 1
        currentTimestamp = hT.__convertTimestamp__(time.time()) 
        parentTimestamp = hT.__getTimestampParentFrame__(frame)
        threadId = hT.__getThreadId__(thread.get_ident())
        key = type(self).__name__
        key = hT.__getClassKey__(key)
        if key == None:
            return
        obj = hT._class[key] 
        objId = obj.__getId__()
        behaviorId = hT.__getObjectId__(code)
        #comportamiento extraño
        #se debe deshabilitar settrace
        #revizar comportamiento de xdrlib
        import sys
        sys.settrace(None)
        obj.attributes.__updateAttr__({name:-1},objId)
        Id = obj.attributes[name]
        if not hT._probe.has_key((currentLasti,behaviorId)):
            probeId = hT.__registerProbe__(currentLasti,behaviorId)
        else:
            probeId = hT._probe[(currentLasti,behaviorId)]          
        hT.packer.reset()
        hT.packer.pack_int(hT.events['set'])
        hT.packer.pack_int(hT.objects['attribute'])
        hT.packer.pack_int(Id)
        hT.packer.pack_int(behaviorId)
        dataType = hT.__getDataType__(value)
        hT.packer.pack_int(dataType)
        value = hT.__packValue__(dataType, value)
        hT.packer.pack_int(probeId)
        hT.packer.pack_hyper(parentTimestamp)        
        hT.packer.pack_int(currentDepth)
        hT.packer.pack_hyper(currentTimestamp)
        hT.packer.pack_int(threadId)
        object.__setattr__(self, name, value) 
        if hT.FLAG_DEBUGG:
            print hT.events['set'],
            print hT.objects['attribute'],
            print Id,
            print behaviorId,
            print dataType,
            print value,
            print probeId,
            print parentTimestamp,
            print currentDepth,
            print currentTimestamp,
            print threadId
            raw_input()
        try:
            hT._socket.sendall(hT.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        #se habilita nuevamente settrace    
        sys.settrace(hT.__trace__)


if th:
    #a cada nuevo thread se le define settrace
    settrace(hT.__trace__)  
#asignamos settrace para nuestro espacio de trabajo
sys.settrace(hT.__trace__)

