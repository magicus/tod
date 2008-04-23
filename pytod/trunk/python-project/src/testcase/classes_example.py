#! /usr/bin/python
# -*- coding: utf-8 -*-

from debugger.pytod.core.hunterTrace import *

class clase1(Descriptor):
    
    def __init__(self):
        self.w = 7
        h = 5
        return

    def metodo(self):
        z=3
        self.a = 0
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
a = clase1()
a.metodo()
hT.__printHunter__()