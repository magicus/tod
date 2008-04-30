#! /usr/bin/python
# -*- coding: utf-8 -*-

from debugger.pytod.core.hunterTrace import *

class clase1(Descriptor):
    
    def __init__(self, y):
        x = 1
        x = 3
        #self.c = 2
        pass
    
    def metodo(self, h, i, j, k):
        pass
"""
class clase2(Descriptor):
    def __init__(self):
        y = 1
        
class clase3(Descriptor):
    def __init__(self):
        y = 1
    def metodo(self):
        v=6
"""        
a = clase1(1)
a.metodo(10,20,30,40)
#hT.__printHunter__()