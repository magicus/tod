#! /usr/bin/python
# -*- coding: utf-8 -*-
import sys
sys.path.append('/media/WD Passport/eclipse/workspace/python-project/src')
from debugger.pytod.core.hunterTrace import hT

class clase1(object):
    
    def __init__(self, y):
        self.x = 1
        self.c = 2
        self.z = 1
        self.metodo(self.z, 1, 2, 3)
        return
    
    def metodo(self, h, i, j, k):
        self.casa = 1 + h
        h = 0
        print self.z
        k = i + j
        self.x = k
        try:
            o = self.foo()
        except:
            o = 1
        """
        self.foo()
        """
        return k
    
    def foo(self):
        y = 1/0
    
"""    
class clase2(object):
    def __init__(self):
        y = 1
        
        
class clase3(object):
    def __init__(self):
        y = 1
    def metodo(self):
        v=6
"""
        
a = clase1(1)
a.metodo(10,20,30,40)
#b = clase2()
#c = clase3()
#hT.__printHunter__()