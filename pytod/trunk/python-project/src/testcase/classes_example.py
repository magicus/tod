#! /usr/bin/python
# -*- coding: utf-8 -*-

from debugger.pytod.core.hunterTrace import *

class clase1(Descriptor):
    def __init__(self):
        x=0

    def metodo(self):
        z=3

class clase2(Descriptor):
    def __init__(self):
        y = 1
        
class clase3(Descriptor):
    def __init__(self):
        y = 1
    def metodo(self):
        v=6
        
a = clase1()
b = clase2()
c = clase3()
hT.__printHunter__()