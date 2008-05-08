#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Class']

import inspect
from Diccionario import Diccionario

class Class(object):

    def __init__(self, hT, classId, code, lnotab):
        self.hT = hT
        self.attributes = Diccionario(self.hT)
        self.method = Diccionario(self.hT)
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.id = classId

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __addMethod__(self,code,locals):
        for k,v in locals.iteritems():
            if inspect.isfunction(v):
                if not (k == '__module__'):
                    id = self.hT.Id.__get__()
                    self.method.update({k:id})
                    self.hT.Id.__next__()