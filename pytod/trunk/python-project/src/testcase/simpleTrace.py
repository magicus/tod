#! /usr/bin/python
# -*- coding: utf-8 -*-

import sys
import dis

def __createlnotab__(code):
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

def __getargs__(code):
    code.co_varnames

def trace(frame, event, arg):
    code = frame.f_code
    if event == 'call':
        print code.co_name
        print frame.f_locals
        print frame.f_exc_value
        print frame.f_exc_traceback
        print frame.f_exc_value
        raw_input()
        return trace
    elif event == 'line':
        print frame.f_exc_value
        print frame.f_exc_traceback
        print frame.f_exc_value
        return trace
    elif event == 'return':
        print code.co_name
        print frame.f_exc_value
        print frame.f_exc_traceback
        print frame.f_exc_value        
        print arg
        raw_input()
        
class Descriptor(object):
    
    def __setattr__(self, name, value):
        print 'estoy dentro de setattr',sys._getframe()
        

class prueba(Descriptor):
    def __init__(self):
        self.x = 0
        self.y = 1
        
sys.settrace(trace)
#a = prueba()

def algo(a,b,c):
    a = [2, 3, 4]
    print id(a)
    a.append(5)
    1/0
    
algo(3,2,5)