#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['hT', 'Descriptor']

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
from Descriptor import Descriptor
from objectClass import Class
from objectMethod import Method
from objectFunction import Function
th = False
if th:
    from threading import settrace
      

class hunterTrace(object):

    def __init__(self, Id, probeId, threadId, packer):
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
        self.methodPattern = "\A__.*(__)$"
        self.__socketConnect__()
        
    def __socketConnect__(self):
        self._socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self._socket.connect(('127.0.0.1', 8058))
        except socket.gaierror:
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
    
    def __printChangeVar__(self, code, local, locals, obj, currentLasti, depth, parentTimeStampFrame, threadId):
        attr = obj.__getLocals__()
        objId = obj.__getId__()
        for i in local.iterkeys():
            if not attr.has_key(i) or not locals.has_key(i):
                return
            if not self._probe.has_key((currentLasti,objId)):
                probeId = self.__registerProbe__(currentLasti,objId)
            else:
                probeId = self._probe[(currentLasti,objId)]
            #TODO: ver cuando el valor es una tuple, list, dict, etc                
            #preguntar el tipo de la variable para poder
            #"socketear"
            self.packer.reset()
            print self.events['set'],
            self.packer.pack_int(self.events['set'])
            print self.objects['local'],
            self.packer.pack_int(self.objects['local'])
            print attr[i],
            self.packer.pack_int(attr[i])
            print objId,
            self.packer.pack_int(objId)
            print locals[i],
            #TODO: ver el asunto de los tipos de datos
            #tomar el tipo y consultar en dataTypes el id
            dataType = 8
            if self.dataTypes.has_key(locals[i].__class__.__name__):
                dataType = self.dataTypes[locals[i].__class__.__name__]
            self.packer.pack_int(dataType)
            print dataType
            #se crea una estructura la cual retorna
            #que metodo debe ser llamado
            if self.packXDRLib.has_key(dataType):
                methodName = self.packXDRLib[dataType]
                getattr(self.packer,'pack_%s'%methodName)(locals[i])
                print locals[i]
            else:
                #en estos momentos envíamos el tipo de dato
                #TODO: debieramos envíar el id del objeto
                self.packer.pack_int(dataType)
                print dataType
            print probeId,
            self.packer.pack_int(probeId)
            print '%11.9f'%(parentTimeStampFrame),
            self.packer.pack_double(parentTimeStampFrame)
            print depth,
            self.packer.pack_int(depth)
            currentTimeStamp = time.time() 
            print '%11.9f'%(currentTimeStamp),
            self.packer.pack_double(currentTimeStamp)
            print threadId
            self.packer.pack_int(threadId)
            self._socket.sendall(self.packer.get_buffer())

    def __printCallMethod__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame, threadId):
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
        print self.events['call'],
        self.packer.pack_int(self.events['call'])
        print self.objects['method'],
        self.packer.pack_int(self.objects['method'])
        print methodId,
        self.packer.pack_int(methodId)
        print parentId,
        self.packer.pack_int(parentId)
        print classId,
        self.packer.pack_int(classId)
        print len(argsValue),
        self.packer.pack_int(len(argsValue))
        for v in argsValue:
            print v,
            #TODO: en estos momentos asumimos todos enteros
            self.packer.pack_int(1)
        print probeId,
        self.packer.pack_int(probeId)
        print '%11.9f'%(parentTimeStampFrame),
        self.packer.pack_double(parentTimeStampFrame)
        print depth,
        self.packer.pack_int(depth)    
        print '%11.9f'%(currentTimeStamp),
        self.packer.pack_double(currentTimeStamp)
        print threadId
        self.packer.pack_int(threadId)

        
    def __printCallFunction__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame, threadId):
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
        print self.events['call'],
        self.packer.pack_int(self.events['call'])
        print self.objects['function'],
        self.packer.pack_int(self.objects['function'])
        print functionId,
        self.packer.pack_int(functionId)
        print parentId,
        self.packer.pack_int(parentId)
        print len(argsValue),
        self.packer.pack_int(len(argsValue))
        for v in argsValue:
            print v,
            #TODO: en estos momentos asumimos todos enteros
            self.packer.pack_int(1)
        print probeId,
        self.packer.pack_int(probeId)
        print '%11.9f'%(parentTimeStampFrame),
        self.packer.pack_double(parentTimeStampFrame)        
        print depth,
        self.packer.pack_int(depth)
        print '%11.9f'%(currentTimeStamp),
        self.packer.pack_double(currentTimeStamp)
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
        self.packer.reset()
        print self.events['return'],
        self.packer.pack_int(self.events['return'])
        #TODO: ver tipos de datos y la manera de enviarlos
        print arg,
        self.packer.pack_int(1)
        print probeId,
        self.packer.pack_int(probeId)
        print False
        self.packer.pack_bool(False)

    
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
        print classId,
        self.packer.pack_int(classId)
        print className,
        self.packer.pack_string(className)
        print classBases
        self.packer.pack_int(0)
        raw_input()
        self._socket.sendall(self.packer.get_buffer())
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
        print methodId,
        self.packer.pack_int(methodId)
        print classId,
        self.packer.pack_int(classId)
        print code.co_name,
        self.packer.pack_string(code.co_name)
        print len(args),
        self.packer.pack_int(len(args))
        for i in range(len(args)):
            print args[i],
            self.packer.pack_string(args[i])
            print i,
            self.packer.pack_int(i)
        raw_input()
        self._socket.sendall(self.packer.get_buffer())
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
        print self.events['register'],
        self.packer.pack_int(self.events['register'])
        print self.objects['function'],        
        self.packer.pack_int(self.objects['function'])
        print functionId,
        self.packer.pack_int(functionId)
        print code.co_name,
        self.packer.pack_string(code.co_name)
        print len(args),
        self.packer.pack_int(len(args))
        for i in range(len(args)):
            print args[i],
            self.packer.pack_string(args[i])
            print i,
            self.packer.pack_int(i)
        raw_input()
        self._socket.sendall(self.packer.get_buffer())
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
        print probeId,
        self.packer.pack_int(probeId)
        print parentId,
        self.packer.pack_int(parentId)
        print currentLasti
        self.packer.pack_int(currentLasti)
        raw_input()
        self._socket.sendall(self.packer.get_buffer())
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
        print threadId,
        self.packer.pack_int(threadId)
        print threadSysId
        self.packer.pack_int(threadSysId)
        raw_input()
        self._socket.sendall(self.packer.get_buffer())
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
                    args = self.__getArgs__(code)
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
                self.__printChangeVar__(
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
                    self.__printChangeVar__(
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

hT = hunterTrace(generatorId(),generatorId(),generatorId(),xdrlib.Packer())
if th:
    #a cada nuevo thread se le define settrace
    settrace(hT.__trace__)  
#asignamos settrace para nuestro espacio de trabajo
sys.settrace(hT.__trace__)

