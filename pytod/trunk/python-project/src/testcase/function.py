#! /usr/bin/python
# -*- coding: utf-8 -*-

from debugger.pytod.core.hunterTrace import *
from debugger.pytod.core.hunterTrace import __print__

def prueba():
    x = 10
    a = 0
    x = 15
    y = x = a = 1
    prueba2()

def prueba2(e=3):
    print 'hola'


prueba()
prueba2()

__print__()