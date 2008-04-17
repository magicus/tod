#! /usr/bin/python
# -*- coding: utf-8 -*-
import sys
#from RutaDirectorio import modificar_path
#print modificar_path()
#sys.path.append(modificar_path())
print sys.path
from src.debugger.pytod.core import hunterTrace

def prueba():
    x = 10
    a = 0
    x = 15
    y = x = a = 1
    prueba2()

def prueba2(e=3):
    print 'hola'
    
print 'clases'
for k,v in hT._class.iteritems():
    print v.__dict__
    print
print '======='

print 'metodos'
for k,v in hT._method.iteritems():
    print v.__dict__
    print
print '======='

print 'funcion'
for k,v in hT._function.iteritems():
    print v.__dict__
    print
print '======='