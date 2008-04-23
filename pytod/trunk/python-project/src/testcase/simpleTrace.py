#! /usr/bin/python
# -*- coding: utf-8 -*-

import sys
import dis

def trace(frame, event, arg):
    print frame.f_code.co_name
    if event == 'call':
        print frame.f_locals
        return trace
    elif event == 'line':
        return trace
    elif event == 'return':
        print frame.f_locals
        try:
            print type(eval('prueba')).__bases__
        except:
            pass
        dis.dis(frame.f_code)
        #print frame.f_locals

sys.settrace(trace)