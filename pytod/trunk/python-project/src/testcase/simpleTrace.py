#! /usr/bin/python
# -*- coding: utf-8 -*-

import sys
import dis

def __createlnotab__(aCode):
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

"""
def trace(frame, event, arg):
    print event
    code = frame.f_code
    if event == 'exception':
        fBack = frame.f_back
        if frame.f_code.co_name == '<module>':
            print 'para la cuestioncita'
            #sys.settrace(None)
        print fBack, fBack.f_code.co_name, code.co_name
        raw_input()
        for theIndex in dis.findlinestarts(code):
            if frame.f_lineno in theIndex:
                theValue = theIndex[0]
        print frame.f_lineno, frame.f_lasti
        print dis.opname[ord(code.co_code[theValue-3])]
        dis.disassemble(frame.f_code,frame.f_lasti)
        #print event, code.co_name
        #print arg[0],arg[1],arg[2]
        raw_input()
        return None
    if event == 'call':
        #print code.co_name
        #raw_input()
        return trace
    elif event == 'line':
        #print code.co_name
        return trace
    elif event == 'return':
        print code.co_name
        #print arg
        #raw_input()
        pass
"""

class Dict(dict):
    
    def __setitem__(self, aKey, aValue):
        print 'estas asignanto',aValue,'a la variable',aKey
        dict.__setitem__(self,aKey,aValue)

def trace(aFrame, aEvent, aArg):
    dis.disassemble(aFrame.f_code)
    if aEvent == 'call':
        print aFrame.f_code.co_name, aEvent
        #print aFrame.f_locals
        #raw_input()
        if aFrame.f_code.co_name == '__init__':
            aFrame.f_locals['self'].__dict__ = Dict()
            type(aFrame.f_locals['self'].__dict__)
            raw_input()
        return trace
    elif aEvent == 'line':
        print aFrame.f_code.co_name, aEvent
        #print aFrame.f_locals
        if aFrame.f_locals.has_key('self'):
            print aFrame.f_locals['self'].__dict__
        #raw_input()
        return trace
    elif aEvent == 'return':
        print aFrame.f_code.co_name, aEvent
        print aFrame.f_locals
        #print aFrame.f_globals
        #raw_input()
    
#sys.settrace(trace)
"""
def m0():
    m1()

def m1():
    m2()

def m2():
    try:
        m3()
    except:
        print 'hola'
        
def m2():
    m3()

def m3():
    y = 1/0
    
m0()
"""
"""
class algo(object):
    a = 0

    def __init__(self):
        self.b = 52
        self.z =10
        print self.a
    
    def imprimir(self):
        print self.b, self.z
        
    def __setattr__(self, aName, aValue):
        print aName, aValue
        object.__setattr__(self, aName, aValue)
   
b = algo()
algo.imprimir()
b.e=52
print b.b
"""



class MyDict(dict):
    def __setitem__(self, aKey, aValue):
        print 'asignando'
        dict.__setitem__(self,aKey,aValue)
        
    def __update__(self, aDictionary):
        print 'a'

dicc = MyDict()
dicc.update({'hola': lambda self: 'hola metamundo!'})
Saludo = type('Saludo', (), dicc)
s = Saludo()
s.hola()