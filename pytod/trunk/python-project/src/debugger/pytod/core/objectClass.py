#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Class']

import inspect
from Dictionary import Dictionary

class Class(object):

    def __init__(self, aHt, aClassId, aCode, aLnotab):
        self.hT = aHt
        self.staticField = Dictionary(self.hT)
        self.attributes = Dictionary(self.hT)
        self.method = Dictionary(self.hT)
        self.lnotab = aLnotab
        self.code = aCode
        self.name = aCode.co_name
        self.Id = aClassId

    def __getId__(self):
        return self.Id

    def __getLnotab__(self):
        return self.lnotab

    def __addMethod__(self, aCode, aLocals):
        for k,v in aLocals.iteritems():
            if inspect.isfunction(v):
                if not (k == '__module__'):
                    id = self.hT.itsId.__get__()
                    self.method.update({k:id})
                    self.hT.itsId.__next__()
    
    def __addStaticField__(self, aLocals):
        self.staticField.__updateStaticField__(aLocals, self.Id)
    
    def __addAttribute__(self, aName, aObjectId):
        self.attributes.__updateAttr__({aName:-1}, aObjectId)       