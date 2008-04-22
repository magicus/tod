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
from threading import settrace


class Diccionario(dict):

    def __setitem__(self,k,v):
        dict.__setitem__(self,k,v)

    def __update__(self,d):
        for k,v in d.items():
            #se debe registrar argumento self?
            if not self.has_key(k) or k == 'self':
                self[k] = v
                print 'register',k,'=',v

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
        id = hT.Id.__get__()
        key = type(self).__name__
        key = hT.__getClassKey__(key)
        if key == None:
            return
        hT._class[key].attribute.__update__({name:id})
        hT.Id.__next__()
        print 'set',name,'=',value,'id =',id
        object.__setattr__(self, name, value)

class Class(object):

    def __init__(self, id, code, lnotab):
        self.attribute = Diccionario()
        self.method = Diccionario()
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.id = id

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __addMethod__(self,code,locals):
        for k,v in locals.iteritems():
            if inspect.isfunction(v):
                if not (k == '__module__'):
                    id = hT.Id.__get__()
                    self.method.__update__({k:id})
                    hT.Id.__next__()

class Function(object):

    def __init__(self, id, code, lnotab,args):
        self.local_var = Diccionario()
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

    def __getLocalVar__(self):
        return self.local_var

    def __getArgs__(self):
        return self.argument

    def __updateLocalVar__(self, local):
        self.local_var.__update__(local)

    def __updateArgument__(self, args):
        self.argument.__update__(args)


class Method(object):

    def __init__(self, id, code, lnotab, idClass, args):
        self.local_var = Diccionario()
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

    def __getLocalVar__(self):
        return self.local_var

    def __getTarget__(self):
        return self.idClass

    def __getArgs__(self):
        return self.argument

    def __updateLocalVar__(self, local):
        self.local_var.__update__(local)

    def __updateArgument__(self, args):
        self.argument.__update__(args)


class hunterTrace(object):

    def __init__(self, Id, probeId):
        self._class = {}
        self._function = {}
        self._method = {}
        self.Id = Id
        self.probeId = probeId
        self.methodPattern = "\A__.*(__)$"

    def __addClass__(self, id, lnotab, code):
        objClass = Class(id,code,lnotab)
        self._class.update({code:objClass})
        return objClass

    def __addFunction__(self, id, lnotab, code, args):
        self._function.update({code:Function(id,code,lnotab,args)})

    def __addMethod__(self, id, lnotab, code, idClass, args):
        self._method.update({code:Method(id,code,lnotab,idClass,args)})

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

    def __getClassKey__(self, nameClass):
        for k,v in self._class.iteritems():
            if k.co_name == nameClass:
                return k
        return None

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
    
    def __printchangevar__(self, code, local, locals, obj, f_lasti, depth, parentTimeStampFrame):
        attr = obj.__getLocalVar__()
        objId = obj.__getId__()
        for i in local.iterkeys():
            probeId = self.probeId.__get__()
            self.probeId.__next__()
            if not attr.has_key(i) or not locals.has_key(i):
                return
            print 'set',i,'=',locals[i],", id=", attr[i],
            print ',probe(id =',probeId,', f_lasti =',f_lasti,', id =',objId,')',
            print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
            print ',current depth =',depth,', current time stamp = %11.9f'%(time.time()),
            print ',current thread =',thread.get_ident()

    def __printCallMethod__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame):
        obj = self.__getObject__(code)
        id = obj.__getId__()
        target = obj.__getTarget__()
        args = obj.__getArgs__()
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        f_id = self.__getObjectId__(f_code)
        probeId = self.probeId.__get__()
        self.probeId.__next__()
        current_lasti = frame.f_lasti
        print "call",code.co_name,", id =",id,", target =",target, ", args =",
        print args,
        print ',llamado desde (',f_id,',f_lasti =',f_lasti,')',
        print ',probe(id =',probeId,', f_lasti =',current_lasti,', id =',id,')',
        print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
        print ',current depth =',depth,', current time stamp = %11.9f'%(currentTimeStamp),      
        print ',current thread =',thread.get_ident()

        
    def __printCallFunction__(self, code, frame, depth, currentTimeStamp, parentTimeStampFrame):
        obj = self.__getObject__(code)
        id = obj.__getId__()
        args = obj.__getArgs__()
        f_back = frame.f_back
        f_lasti = f_back.f_lasti
        f_code = f_back.f_code
        f_id = self.__getObjectId__(f_code)
        probeId = self.probeId.__get__()
        self.probeId.__next__()
        current_lasti = frame.f_lasti
        print "call",code.co_name,", id =",id, ", args =",args,
        print ',llamado desde (',f_id,',f_lasti =',f_lasti,')',
        print ',probe(id =',probeId,', f_lasti =',current_lasti,', id =',id,')',
        print ',parent time stamp = %11.9f'%(parentTimeStampFrame),
        print ',current depth =',depth,', current time stamp = %11.9f'%(currentTimeStamp),
        print ',current thread =',thread.get_ident()

        
    def __register__(self, obj, local):
        obj.local_var.__update__(local)

    def __registerClass__(self, code, locals):
        id = self.Id.__get__()
        print 'register',code.co_name,', id =',id,', code = ',code
        objClass = self.__addClass__(id,self.__createlnotab__(code),code)
        self.Id.__next__()
        #se deben registrar los metodos asociados
        objClass.__addMethod__(code,locals)

    def __registerMethod__(self, code, id, idClass, args):
        print 'register',code.co_name,', id =',id,', idClass = ',idClass,', args=',args
        self.__addMethod__(id,self.__createlnotab__(code),code,idClass,args)

    def __registerFunction__(self, code):
        id = self.Id.__get__()
        args = self.__getargs__(code)
        print 'register',code.co_name,', id =',id,', args=',args
        self.__addFunction__(id,self.__createlnotab__(code),code,args)
        self.Id.__next__()

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
        parentTimeStampFrame = self.__getTimeStampParentFrame__(frame, currentTimeStamp)
        if event == "call":
            if re.search(self.methodPattern,code.co_name) and not code.co_name == '__init__':
                return
            if code.co_name == '__init__':
                id = self.Id.__get__()
                locals['self'].__dict__.update({'__pyTOD__':id})
                self.Id.__next__()
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
                self.__printCallMethod__(code,frame,depth,currentTimeStamp,parentTimeStampFrame)
            else:
                #verificamos si es una funcion
                if globals.has_key(code.co_name):
                    if inspect.isfunction(globals[code.co_name]):
                        if not self.__inFunction__(code):
                            self.__registerFunction__(code)
                    self.__printCallFunction__(code,frame,depth,currentTimeStamp,parentTimeStampFrame)       
            return self.__trace__
        elif event == "line":
            if re.search(self.methodPattern,code.co_name) and not code.co_name == '__init__':
                return
            obj = self.__getObject__(code)
            if obj == None:
                return
            lnotab = obj.__getLnotab__()
            if lnotab.has_key(frame.f_lasti):
                local = self.__getpartcode__(code,lnotab[frame.f_lasti])
                self.__register__(obj,local)
                #imprimiendo los cambios de valores, con su respectivo id
                self.__printchangevar__(code,local,locals,obj,frame.f_lasti,depth,parentTimeStampFrame)
            return self.__trace__
        elif event == "return":
            if re.search(self.methodPattern,code.co_name) and not code.co_name == '__init__':
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
                    self.__printchangevar__(code,local,locals,obj,frame.f_lasti,depth,parentTimeStampFrame)
            print "return"

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

hT = hunterTrace(IdGenerator(),IdGenerator())
sys.settrace(hT.__trace__)    
