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

    def __init__(self, aId, aProbeId, aThreadId, aPacker, aHost, aPort):
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
        self.Id = aId
        self.probeId = aProbeId
        self.threadId = aThreadId
        self.packer = aPacker
        self.host = aHost
        self.port = aPort
        self.FLAG_DEBUGG = True
        self.methodPattern = "\A__.*(__)$"
        self.__socketConnect__()
        
    def __socketConnect__(self):
        self._socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self._socket.connect((self.host, self.port))
        except:
            print "TOD, esta durmiendo :("


    def __addClass__(self, aId, aLnotab, aCode):
        objectClass = Class(self,aId,aCode,aLnotab)
        self._class.update({aCode:objectClass})
        return objectClass

    def __addFunction__(self, aId, aLnotab, aCode, aArgs):
        self._function.update({aCode:Function(self,aId,aCode,aLnotab,aArgs)})

    def __addMethod__(self, aId, aLnotab, aCode, idClass, aArgs):
        self._method.update({aCode:Method(self,aId,aCode,aLnotab,idClass,aArgs)})

    def __addProbe__(self, aProbeId, currentLasti, parentId):
        self._probe.update({(currentLasti,parentId):aProbeId})

    def __addThread__(self, aThreadId, threadSysId):
        self._thread.update({threadSysId:aThreadId})
        
    def __createlnotab__(self, aCode):
        theLnotab = {}
        if hasattr(aCode, 'co_lnotab'):
            table = aCode.co_lnotab
            index = 0
            last_index = None
            for i in range(0, len(table), 2):
                index = index + ord(table[i])
                if last_index == None:
                    last_index = index
                else:
                    theLnotab.update({index:tuple([last_index,index-1])})                
                    last_index = index
            theLnotab.update({len(aCode.co_code)-1:tuple([last_index,len(aCode.co_code)-1])})                
        return theLnotab        

    def __convertTimestamp__(self,aTimestamp):
        #the timestamp is converted to long
        return long(aTimestamp*1000000000)

    def __depthFrame__(self, aFrame):
        theBackFrame = aFrame.f_back
        if theBackFrame.f_locals.has_key('__depthFrame__'):
            theCurrentDepth = theBackFrame.f_locals['__depthFrame__']
            aFrame.f_locals['__depthFrame__'] = theCurrentDepth + 1
        else:
            aFrame.f_locals['__depthFrame__'] = 1
        return aFrame.f_locals['__depthFrame__']

    def __inClass__(self, aClass):
        if self._class.has_key(aClass):
            return True
        return False

    def __inFunction__(self, aFunction):
        if self._function.has_key(aFunction):
            return True
        return False

    def __inMethod__(self, aMethod):
        if self._method.has_key(aMethod):
            return True
        return False

    def __isClassKey__(self, aClassCode):
        for theKey in self._class.iterkeys():
            if theKey == aClassCode:
                return self._class[theKey]
        return None

    def __isFunctionKey__(self, aFunctionCode):
        for theKey in self._function.iterkeys():
            if theKey == aFunctionCode:
                return self._function[theKey]
        return None

    def __isMethodKey__(self, aMethodCode):
        for theKey in self._method.iterkeys():
            if theKey == aMethodCode:
                return self._method[theKey]
        return None

    def __instantiation__(self, 
                          aCode, 
                          aFrame, 
                          aInstantiationId, 
                          aDepth, 
                          aCurrentTimestamp, 
                          aParentTimestampFrame, 
                          aThreadId):
        theBehavior = self.__getObject__(aCode)
        theBehaviorId = theBehavior.__getId__()
        theClassId = theBehavior.__getTarget__()
        theArgsValue = theBehavior.__getArgsValues__(aFrame.f_locals)
        theBackFrame = aFrame.f_back
        theFrameLasti = theBackFrame.f_lasti
        theBackFrameCode = theBackFrame.f_code
        theParentId = self.__getObjectId__(theBackFrameCode)
        theCurrentLasti = aFrame.f_lasti        
        if not self._probe.has_key((theCurrentLasti,theParentId)):
            theProbeId = self.__registerProbe__(theCurrentLasti,theParentId)
        else:
            theProbeId = self._probe[(theCurrentLasti,theParentId)]
        self.packer.reset()       
        self.packer.pack_int(self.events['instantiation'])
        self.packer.pack_int(theBehaviorId)
        self.packer.pack_int(aInstantiationId)
        self.packer.pack_int(len(theArgsValue))
        thePrintArg = " "
        for theValue in theArgsValue:
            theDataType = self.__getDataType__(theValue)
            self.packer.pack_int(theDataType)
            thePrintArg += str(theDataType)
            thePrintArg += " "
            thePrintArg += str(self.__packValue__(theDataType, theValue))
            thePrintArg += " "
        self.packer.pack_int(theProbeId)
        self.packer.pack_hyper(aParentTimestampFrame)
        self.packer.pack_int(aDepth)    
        self.packer.pack_hyper(aCurrentTimestamp)
        self.packer.pack_int(aThreadId)
        if self.FLAG_DEBUGG:
            print self.events['instantiation'],
            print aInstantiationId,
            print len(theArgsValue), 
            print thePrintArg,        
            print theProbeId,
            print aParentTimestampFrame,
            print aDepth,
            print aCurrentTimestamp,
            print aThreadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('

    def __getArgs__(self, aCode):
        return aCode.co_varnames[:aCode.co_argcount]

    def __getClassKey__(self, aNameClass):
        for theKey, theValue in self._class.iteritems():
            if theKey.co_name == aNameClass:
                return theKey
        return None
    
    def __getObjectId__(self, aCode):
        if self.__isClassKey__(aCode):
            return self._class[aCode].__getId__()
        elif self.__isFunctionKey__(aCode):
            return self._function[aCode].__getId__()
        elif self.__isMethodKey__(aCode):
            return self._method[aCode].__getId__()
        return -1

    def __getObject__(self, aCode):
        if self.__isFunctionKey__(aCode):
            return self._function[aCode]
        elif self.__isMethodKey__(aCode):
            return self._method[aCode]
        return None

    def __getThreadId__(self, aThreadSysId):
        if not hT._thread.has_key(aThreadSysId):
            theThreadId = self.__registerThread__(aThreadSysId)
        else:
            theThreadId = self._thread[aThreadSysId]
        return theThreadId

    def __getpartcode__(self, aCode, aLimits):
        theLower = aLimits[0]
        theUpper = aLimits[1]
        theCode = aCode.co_code
        theStoreFast = {}    
        while theLower < theUpper:
            theOp = ord(theCode[theLower])
            theNameOp = dis.opname[theOp]
            theLower = theLower + 1
            if theOp >= dis.HAVE_ARGUMENT:
                theValue = ord(theCode[theLower]) + ord(theCode[theLower+1])*256
                theLower = theLower + 2
                if theOp in dis.haslocal and theNameOp == 'STORE_FAST':
                    theArgumentValue = aCode.co_varnames[theValue]
                    theStoreFast.update({theArgumentValue:theValue})
        return theStoreFast

    def __getDepthFrame__(self, aFrame):
        try:
            return aFrame.f_locals['__depthFrame__']
        except:
            return -1
    
    def __getDataType__(self, aValue):
        theDataType = 8
        try:
            if self.dataTypes.has_key(aValue.__class__.__name__):
                theDataType = self.dataTypes[aValue.__class__.__name__]
        except:
            return theDataType
        finally:
            return theDataType

    def __getTimestampFrame__(self, aFrame):
        if aFrame.f_locals.has_key('__timestampFrame__'):
            return aFrame.f_locals['__timestampFrame__']
        return 0

    def __getTimestampParentFrame__(self, aFrame):
        theBackFrame = aFrame.f_back 
        if theBackFrame.f_locals.has_key('__timestampFrame__'):
            return theBackFrame.f_locals['__timestampFrame__']
        return 0
    
    def __markTimestampFrame__(self, aFrame):
        if not aFrame.f_locals.has_key('__timestampFrame__'): 
            aFrame.f_locals['__timestampFrame__'] = self.__convertTimestamp__(
                                                                  time.time())
        return
    
    def __packValue__(self, aDataType, aValue):
        if self.packXDRLib.has_key(aDataType):
            theMethodName = self.packXDRLib[aDataType]
            getattr(self.packer,'pack_%s'%theMethodName)(aValue)
            return aValue            
        else:
            #en estos momentos envíamos el tipo de dato
            #TODO: debieramos envíar el id del objeto
            self.packer.pack_int(aDataType)
            return aDataType
    
    def __localWrite__(
                       self,
                       aCode,
                       aBytecodeLocal,
                       aLocals,
                       aObject,
                       aCurrentLasti,
                       aDepth,
                       aParentTimestampFrame, 
                       aThreadId):
        theLocalVariables = aObject.__getLocals__()
        theBehaviorId = self.__getObjectId__(aCode)
        theDepth = aDepth + 1
        for theValue in aBytecodeLocal.iterkeys():
            if not theLocalVariables.has_key(theValue) or \
               not aLocals.has_key(theValue):
                return
            if not self._probe.has_key((aCurrentLasti,theBehaviorId)):
                theProbeId = self.__registerProbe__(aCurrentLasti,
                                                    theBehaviorId)
            else:
                theProbeId = self._probe[(aCurrentLasti,theBehaviorId)]
            self.packer.reset()
            self.packer.pack_int(self.events['set'])
            self.packer.pack_int(self.objects['local'])
            self.packer.pack_int(theLocalVariables[theValue])
            self.packer.pack_int(theBehaviorId)
            theDataType = self.__getDataType__(aLocals[theValue])
            self.packer.pack_int(theDataType)
            thePackValue = self.__packValue__(theDataType, aLocals[theValue])
            self.packer.pack_int(theProbeId)
            self.packer.pack_hyper(aParentTimestampFrame)
            self.packer.pack_int(theDepth)
            theCurrentTimestamp = self.__convertTimestamp__(time.time()) 
            self.packer.pack_hyper(theCurrentTimestamp)
            self.packer.pack_int(aThreadId)
            if self.FLAG_DEBUGG:            
                print self.events['set'],
                print self.objects['local'],
                print theLocalVariables[theValue],
                print theBehaviorId,
                print theDataType,
                print thePackValue,
                print theProbeId,
                print aParentTimestampFrame,
                print theDepth,
                print theCurrentTimestamp,
                print aThreadId
                raw_input()
            try:
                self._socket.sendall(self.packer.get_buffer())
            except:
                print 'TOD está durmiendo :-('            

    def __methodCall__(self,
                       aCode,
                       aFrame, 
                       aTargetId,
                       aDepth,
                       aCurrentTimestamp,
                       aParentTimestampFrame,
                       aThreadId):
        theObject = self.__getObject__(aCode)
        theMethodId = theObject.__getId__()
        #classId = theObject.__getTarget__()
        theArgsValue = theObject.__getArgsValues__(aFrame.f_locals)
        theBackFrame = aFrame.f_back
        theBackFrameLasti = theBackFrame.f_lasti
        theBackFrameCode = theBackFrame.f_code
        theParentId = self.__getObjectId__(theBackFrameCode)
        theCurrentLasti = aFrame.f_lasti        
        if not self._probe.has_key((theCurrentLasti,theParentId)):
            theProbeId = self.__registerProbe__(theCurrentLasti,theParentId)
        else:
            theProbeId = self._probe[(theCurrentLasti,theParentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['call'])
        self.packer.pack_int(self.objects['method'])
        self.packer.pack_int(theMethodId)
        self.packer.pack_int(aTargetId)
        self.packer.pack_int(len(theArgsValue))
        thePrintArg = " "
        for theValue in theArgsValue:
            theDataType = self.__getDataType__(theValue)
            self.packer.pack_int(theDataType)
            thePrintArg += str(theDataType)
            thePrintArg += " "            
            thePrintArg += str(self.__packValue__(theDataType, theValue))
            thePrintArg += " "
        self.packer.pack_int(theProbeId)
        self.packer.pack_hyper(aParentTimestampFrame)
        self.packer.pack_int(aDepth)    
        self.packer.pack_hyper(aCurrentTimestamp)
        self.packer.pack_int(aThreadId)
        if self.FLAG_DEBUGG:
            print self.events['call'],
            print self.objects['method'],
            print theMethodId,
            print aTargetId,
            print len(theArgsValue),
            print thePrintArg,
            print theProbeId,
            print aParentTimestampFrame,
            print aDepth,
            print aCurrentTimestamp,
            print aThreadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        
    def __functionCall__(self, 
                         aCode, 
                         aFrame,
                         aDepth,
                         aCurrentTimestamp,
                         aParentTimestampFrame,
                         aThreadId):
        theObject = self.__getObject__(aCode)
        theFunctionId = theObject.__getId__()
        theArgsValue = theObject.__getArgsValues__(aFrame.f_locals)
        theBackFrame = aFrame.f_back
        theBackFrameLasti = theBackFrame.f_lasti
        theBackFrameCode = theBackFrame.f_code
        theParentId = self.__getObjectId__(theBackFrameCode)
        theCurrentLasti = aFrame.f_lasti
        if not self._probe.has_key((theCurrentLasti,theParentId)):
            theProbeId = self.__registerProbe__(theCurrentLasti,theParentId)
        else:
            theProbeId = self._probe[(theCurrentLasti,theParentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['call'])
        self.packer.pack_int(self.objects['function'])
        self.packer.pack_int(theFunctionId)
        self.packer.pack_int(len(theArgsValue)-1)
        thePrintArg = " "
        for theValue in theArgsValue:
            theDataType = self.__getDataType__(theValue)
            self.packer.pack_int(theDataType)
            thePrintArg += str(theDataType)
            thePrintArg += " "            
            thePrintArg += str(self.__packValue__(theDataType, theValue))
            thePrintArg += " "     
        self.packer.pack_int(theProbeId)
        self.packer.pack_hyper(aParentTimestampFrame)        
        self.packer.pack_int(aDepth)
        self.packer.pack_hyper(aCurrentTimestamp)
        self.packer.pack_int(aThreadId)
        if self.FLAG_DEBUGG:
            print self.events['call'],
            print self.objects['function'],
            print theFunctionId,
            print len(theArgsValue)-1,
            print thePrintArg,
            print theProbeId,
            print aParentTimestampFrame,
            print aDepth,
            print aCurrentTimestamp,
            print aThreadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('        

    def __behaviorExit__(self,
                         aFrame,
                         arg,
                         aDepth,
                         aParentTimestampFrame,
                         aThreadId,
                         aHasTrown):
        theBackFrame = aFrame.f_back
        theBackFrameCode = theBackFrame.f_code
        theParentId = self.__getObjectId__(theBackFrameCode)
        behaviorId = self.__getObjectId__(aFrame.f_code)
        theCurrentLasti = aFrame.f_lasti
        theDepth = aDepth + 1
        if not self._probe.has_key((theCurrentLasti,theParentId)):
            theProbeId = self.__registerProbe__(theCurrentLasti,theParentId)
        else:
            theProbeId = self._probe[(theCurrentLasti,theParentId)]
        self.packer.reset()
        self.packer.pack_int(self.events['return'])
        self.packer.pack_int(behaviorId)
        theDataType = self.__getDataType__(arg)
        self.packer.pack_int(theDataType)
        thePackValue = self.__packValue__(theDataType, arg)
        if aHasTrown:       
            self.packer.pack_int(1)
        else:
            self.packer.pack_int(0)
        self.packer.pack_int(theProbeId)
        self.packer.pack_hyper(aParentTimestampFrame)        
        self.packer.pack_int(theDepth)
        theCurrentTimestamp = self.__convertTimestamp__(time.time()) 
        self.packer.pack_hyper(theCurrentTimestamp)
        self.packer.pack_int(aThreadId)
        if self.FLAG_DEBUGG:
            print self.events['return'],
            print behaviorId,
            print theDataType,
            print thePackValue,
            print aHasTrown,
            print theProbeId,
            print aParentTimestampFrame,
            print theDepth,
            print theCurrentTimestamp,
            print aThreadId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('

    def __register__(self, aObject, aLocals):
        aObject.__registerLocals__(aLocals)

    def __registerClass__(self, aCode, aLocals):
        theClassId = self.Id.__get__()
        theClassName = aCode.co_name
        #HINT: ver como recuperar las herencias de esta clase 
        theClassBases = None
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['class'])
        self.packer.pack_int(theClassId)
        self.packer.pack_string(theClassName)
        self.packer.pack_int(0)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['class'],
            print theClassId,
            print theClassName,
            print theClassBases
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        theObjectClass = self.__addClass__(
                                           theClassId,
                                           self.__createlnotab__(aCode),
                                           aCode)
        self.Id.__next__()
        theObjectClass.__addMethod__(aCode,aLocals)

    def __registerMethod__(self, aCode, aMethodId, aClassId, aArgs):
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['method'])
        self.packer.pack_int(aMethodId)
        self.packer.pack_int(aClassId)
        self.packer.pack_string(aCode.co_name)
        #argumento viene con self, se le debe restar uno a la cantidad de
        #elementos
        self.packer.pack_int(len(aArgs)-1)
        thePrintArg = " "
        for theValue in range(len(aArgs)):
            if not aArgs[theValue] == 'self':
                thePrintArg += str(aArgs[theValue])
                thePrintArg += " "
                self.packer.pack_string(aArgs[theValue])
                thePrintArg += str(theValue)
                thePrintArg += " "
                self.packer.pack_int(theValue)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['method'],
            print aMethodId,
            print aClassId,
            print aCode.co_name,
            print len(aArgs)-1,
            print thePrintArg
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.__addMethod__(
                           aMethodId,
                           self.__createlnotab__(aCode),
                           aCode,
                           aClassId,
                           aArgs)

    def __registerFunction__(self, aCode):
        theFunctionId = self.Id.__get__()
        aArgs = self.__getArgs__(aCode)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['function'])
        self.packer.pack_int(theFunctionId)
        self.packer.pack_string(aCode.co_name)
        self.packer.pack_int(len(aArgs))
        thePrintArg = " " 
        for theValue in range(len(aArgs)):
            if not aArgs[theValue] == 'self':
                thePrintArg += str(aArgs[theValue])
                thePrintArg += " "
                self.packer.pack_string(aArgs[theValue])
                thePrintArg += str(theValue)
                thePrintArg += " "
                self.packer.pack_int(theValue)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['function'],
            print theFunctionId,
            print aCode.co_name,
            print len(aArgs)-1,
            print thePrintArg
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('            
        self.__addFunction__(
                             theFunctionId,
                             self.__createlnotab__(aCode),
                             aCode,
                             aArgs)
        self.Id.__next__()

    def __registerProbe__(self, aCurrentLasti, aBehaviorId):
        theProbeId = self.probeId.__get__()
        self.__addProbe__(theProbeId,aCurrentLasti,aBehaviorId)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['probe'])
        self.packer.pack_int(theProbeId)
        self.packer.pack_int(aBehaviorId)
        self.packer.pack_int(aCurrentLasti)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['probe'],
            print theProbeId,
            print aBehaviorId,
            print aCurrentLasti            
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.probeId.__next__()
        return theProbeId
    
    def __registerThread__(self, aThreadSysId):
        theThreadId = self.threadId.__get__()
        self.__addThread__(theThreadId,aThreadSysId)
        self.packer.reset()
        self.packer.pack_int(self.events['register'])
        self.packer.pack_int(self.objects['thread'])
        self.packer.pack_int(theThreadId)
        self.packer.pack_int(aThreadSysId)
        if self.FLAG_DEBUGG:
            print self.events['register'],
            print self.objects['thread'],
            print theThreadId,
            print aThreadSysId
            raw_input()
        try:
            self._socket.sendall(self.packer.get_buffer())
        except:
            print 'TOD está durmiendo :-('
        self.threadId.__next__()
        return theThreadId

    def __trace__(self, aFrame, aEvent, aArg):
        if aFrame.f_back == None:
            return
        theCode = aFrame.f_code
        theLocals = aFrame.f_locals
        theGlobals = aFrame.f_globals
        theDepth = self.__depthFrame__(aFrame)
        self.__markTimestampFrame__(aFrame)
        theThreadId = self.__getThreadId__(thread.get_ident())
        if aEvent == "call":
            if re.search(self.methodPattern,theCode.co_name):
                if not theCode.co_name == '__init__':
                    return
            #supuesto manejo de error
            #if aFrame.f_code.co_name == 'apport_excepthook':
            #    print aFrame.f_locals
            #    raw_input()
            theParentTimestampFrame = self.__getTimestampParentFrame__(aFrame)
            if theCode.co_name == '__init__':
                #TODO: cambio experimental, revizar!!!!!!
                """
                id = self.Id.__get__()
                if not hasattr(theLocals['self'],'__dict__'):
                    return
                theLocals['self'].__dict__.update({'__pyTOD__':id})
                self.Id.__next__()
                """
                #aca se sacan las bases de la clase la cual
                #se ha instanciado
                #TODO: encontrar una mejor forma de hacerlo
                #ineficiente!!..quizas interviniendo la llamada
                #de la super clase?
                print type(theLocals['self']).__bases__
            #si self esta en theLocals estamos en un metodo
            if theLocals.has_key('self'):
                if not self.__inMethod__(theCode):
                    theKey = type(theLocals['self']).__name__
                    theKey = hT.__getClassKey__(theKey)
                    if theKey == None:
                        return
                    if not hT._class.has_key(theKey):
                        return
                    theClassId = hT._class[theKey].__getId__()
                    if not hT._class[theKey].method.has_key(theCode.co_name):
                        return
                    theMethodId = hT._class[theKey].method[theCode.co_name]
                    theArgs = self.__getArgs__(theCode)
                    self.__registerMethod__(theCode,theMethodId,theClassId,theArgs)
                theCurrentTimestamp = aFrame.f_locals['__timestampFrame__']
                if theCode.co_name == '__init__':
                    Id = self.Id.__get__()
                    if not hasattr(theLocals['self'],'__dict__'):
                        return
                    theLocals['self'].__dict__.update({'__pyTOD__':Id})
                    self.Id.__next__()
                    self.__instantiation__(theCode,
                                           aFrame,
                                           theLocals['self'].__pyTOD__,
                                           theDepth,
                                           theCurrentTimestamp,
                                           theParentTimestampFrame,
                                           theThreadId)

                else:
                    self.__methodCall__(theCode,
                                        aFrame,
                                        theLocals['self'].__pyTOD__,
                                        theDepth,
                                        theCurrentTimestamp,
                                        theParentTimestampFrame,
                                        theThreadId)
            else:
                #verificamos si es una funcion
                if theGlobals.has_key(theCode.co_name):
                    if inspect.isfunction(theGlobals[theCode.co_name]):
                        if not self.__inFunction__(theCode):
                            self.__registerFunction__(theCode)
                    theCurrentTimestamp = aFrame.f_locals['__timestampFrame__']
                    self.__functionCall__(theCode,
                                          aFrame,
                                          theDepth,
                                          theCurrentTimestamp,
                                          theParentTimestampFrame,
                                          theThreadId)   
            return self.__trace__
        elif aEvent == "line":
            if re.search(self.methodPattern,theCode.co_name):
                if not theCode.co_name == '__init__':
                    return
            theParentTimestampFrame = self.__getTimestampFrame__(aFrame)
            theObject = self.__getObject__(theCode)
            if theObject == None:
                return
            theLnotab = theObject.__getLnotab__()
            if theLnotab.has_key(aFrame.f_lasti):
                theBytecodeLocals = self.__getpartcode__(theCode,theLnotab[aFrame.f_lasti])
                self.__register__(theObject,theBytecodeLocals)
                self.__localWrite__(theCode,
                                    theBytecodeLocals,
                                    theLocals,
                                    theObject,
                                    aFrame.f_lasti,
                                    theDepth,
                                    theParentTimestampFrame,
                                    theThreadId)
            return self.__trace__
        elif aEvent == "return":
            if re.search(self.methodPattern,theCode.co_name):
                if not theCode.co_name == '__init__':
                    return
            theParentTimestampFrame = self.__getTimestampFrame__(aFrame)
            if theLocals.has_key('__init__'):
                #registramos la definicion de la clase
                if not self.__inClass__(theCode):
                    self.__registerClass__(theCode,theLocals)
            else:
                theObject = self.__getObject__(theCode)
                if theObject == None:
                    return
                theLnotab = theObject.__getLnotab__()
                if theLnotab.has_key(aFrame.f_lasti):
                    theBytecodeLocals = self.__getpartcode__(theCode,theLnotab[aFrame.f_lasti])
                    self. __register__(theObject,theBytecodeLocals)
                    self.__localWrite__(theCode,
                                        theBytecodeLocals,
                                        theLocals,
                                        theObject,
                                        aFrame.f_lasti,
                                        theDepth,
                                        theParentTimestampFrame,
                                        theThreadId)
                self.__behaviorExit__(aFrame,
                                     aArg,
                                     theDepth,
                                     theParentTimestampFrame,
                                     theThreadId,
                                     False)
        elif aEvent == 'exception':
            theParentTimestampFrame = self.__getTimestampFrame__(aFrame)
            #print f_traceback.tb_next.tb_frame.f_code.co_name
            #print f_traceback.tb_next.tb_lineno
            #print f_traceback.tb_next.tb_lasti
            self.__behaviorExit__(aFrame,
                                     aArg[1],
                                     theDepth,
                                     theParentTimestampFrame,
                                     theThreadId,
                                     True)
            sys.settrace(None)
            #print '[trace]', aEvent, aFrame.f_code.co_name, aFrame.f_lineno, aArg
            #raw_input()

    def __printHunter__(self):
        #cerrar socket
        #TODO: encontrar una manera mejor de hacer esto
        self._socket.close()
        print
        print 'clases'
        for theKey, theValue in hT._class.iteritems():
            print theValue.__dict__
            print
        print '======='
        
        print 'metodos'
        for theKey, theValue in hT._method.iteritems():
            print v.__dict__
            print
        print '======='
        
        print 'funcion'
        for theKey, theValue in hT._function.iteritems():
            print theValue.__dict__
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

    def __setattr__(self, aName, aValue):
        import sys
        theFrame = sys._getframe()
        theCode = theFrame.f_back.f_code
        theCurrentLasti = theFrame.f_back.f_lasti
        theCurrentDepth = hT.__getDepthFrame__(theFrame.f_back) + 1
        theCurrentTimestamp = hT.__convertTimestamp__(time.time()) 
        theParentTimestamp = hT.__getTimestampParentFrame__(theFrame)
        theThreadId = hT.__getThreadId__(thread.get_ident())
        theKey = type(self).__name__
        theKey = hT.__getClassKey__(theKey)
        if theKey == None:
            return
        theObject = hT._class[theKey] 
        theObjectId = theObject.__getId__()
        theBehaviorId = hT.__getObjectId__(theCode)
        #comportamiento extraño
        #se debe deshabilitar settrace
        #revizar comportamiento de xdrlib
        sys.settrace(None)
        theObject.attributes.__updateAttr__({aName:-1},theObjectId)
        Id = theObject.attributes[aName]
        if not hT._probe.has_key((theCurrentLasti,theBehaviorId)):
            theProbeId = hT.__registerProbe__(theCurrentLasti,theBehaviorId)
        else:
            theProbeId = hT._probe[(theCurrentLasti,theBehaviorId)]          
        hT.packer.reset()
        hT.packer.pack_int(hT.events['set'])
        hT.packer.pack_int(hT.objects['attribute'])
        hT.packer.pack_int(Id)
        #hT.packer.pack_int(theBehaviorId)
        hT.packer.pack_int(self.__pyTOD__)
        theDataType = hT.__getDataType__(aValue)
        hT.packer.pack_int(theDataType)
        thePackValue = hT.__packValue__(theDataType, aValue)
        hT.packer.pack_int(theProbeId)
        hT.packer.pack_hyper(theParentTimestamp)        
        hT.packer.pack_int(theCurrentDepth)
        hT.packer.pack_hyper(theCurrentTimestamp)
        hT.packer.pack_int(theThreadId)
        object.__setattr__(self, aName, aValue)
        if hT.FLAG_DEBUGG:
            print hT.events['set'],
            print hT.objects['attribute'],
            print Id,
            print theBehaviorId,
            print theDataType,
            print thePackValue,
            print theProbeId,
            print theParentTimestamp,
            print theCurrentDepth,
            print theCurrentTimestamp,
            print theThreadId
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

