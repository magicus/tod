#! /usr/bin/python
# -*- coding: utf-8 -*-
import sys
sys.path.append('/media/WD Passport/eclipse/workspace/python-project/src')
from debugger.pytod.core.hunterTrace import *

class clase1(Descriptor):
    
    def __init__(self, y):
        self.x = 1
        self.c = 2
        self.z = 3
        return
    
    def metodo(self, h, i, j, k):
        self.casa = 1
        k = i + j
        return
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