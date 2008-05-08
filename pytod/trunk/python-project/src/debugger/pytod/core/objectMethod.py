#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Method']

from Diccionario import Diccionario

class Method(object):

    def __init__(self, hT, id, code, lnotab, idClass, args):
        self.hT = hT
        self.locals = Diccionario(self.hT)
        self.argument = ()
        self.lnotab = lnotab
        self.code = code
        self.name = code.co_name
        self.idClass = idClass
        self.id = id
        self.__updateArgument__(args)

    def __getId__(self):
        return self.id

    def __getLnotab__(self):
        return self.lnotab

    def __getLocals__(self):
        return self.locals

    def __getTarget__(self):
        return self.idClass

    def __getArgs__(self):
        return self.argument
    
    def __getArgsValues__(self, locals):
        argValues = ()
        for name in self.argument:
            if locals.has_key(name):
                argValues = argValues + (locals[name],)
        #TODO: analizar caso para cuando sean tuple, list, dict
        return argValues

    def __updateArgument__(self, args):
        self.argument = self.argument + args
        parentId = self.id
        for i in range(len(args)):           
            self.hT.packer.reset()
            print self.hT.events['register'],
            self.hT.packer.pack_int(self.hT.events['register'])
            print self.hT.objects['local'],
            self.hT.packer.pack_int(self.hT.objects['local'])
            #print 'id =',v,
            print i,
            self.hT.packer.pack_int(i)
            #print ',parent id=',parentId,
            print parentId,
            self.hT.packer.pack_int(parentId)
            #print ',name =',k
            print args[i]
            self.hT.packer.pack_string(args[i])
        
    def __registerLocals__(self, local):
        self.locals.__update__(local,self.id)