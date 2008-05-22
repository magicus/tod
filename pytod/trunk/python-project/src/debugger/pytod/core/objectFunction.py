#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Function']

from Diccionario import Diccionario

class Function(object):

    def __init__(self, aHt, aId, aCode, aLnotab, aArgs):
        self.hT = aHt
        self.locals = Diccionario(self.hT)
        self.argument = ()
        self.lnotab = aLnotab
        self.code = aCode
        self.name = aCode.co_name
        self.Id = aId
        self.__updateArgument__(aArgs)

    def __getId__(self):
        return self.Id

    def __getLnotab__(self):
        return self.lnotab

    def __getLocals__(self):
        return self.locals

    def __getArgs__(self):
        return self.argument
    
    def __getArgsValues__(self, aLocals):
        argValues = ()
        for name in self.argument:
            if aLocals.has_key(name):
                argValues = argValues + (aLocals[name],)
        #TODO: analizar caso para cuando sean tuple, list, dict
        return argValues

    def __updateArgument__(self, aArgs):
        self.argument = self.argument + aArgs
        parentId = self.Id
        for i in range(len(aArgs)):            
            self.hT.packer.reset()
            self.hT.packer.pack_int(self.hT.events['register'])
            self.hT.packer.pack_int(self.hT.objects['local'])
            self.hT.packer.pack_int(i)
            self.hT.packer.pack_int(parentId)
            self.hT.packer.pack_string(aArgs[i])  
            if self.hT.FLAG_DEBUGG:
                print self.hT.events['register'],
                print self.hT.objects['local'],          
                print i,
                print parentId,
                print aArgs[i]
                raw_input()
                
    def __registerLocals__(self, aLocal):
        self.locals.__update__(aLocal,self.Id,self.argument)