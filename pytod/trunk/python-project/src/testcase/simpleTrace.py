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

def trace(frame, event, arg):
    code = frame.f_code
    if event == 'call':
        #dis.dis(code)
        #print frame.f_exc_type
        #print __createlnotab__(code)
        #print frame.f_locals
        return trace
    elif event == 'line':
        return trace
    elif event == 'return':
        print code.co_name
        print arg
        raw_input()
        
sys.settrace(trace)