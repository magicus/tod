#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Diccionario']

import xdrlib

class Diccionario(dict):
    
    def __init__(self, hT):
        self.hT = hT
        dict.__init__(self)

    def __setitem__(self, aKey, aValue):
        dict.__setitem__(self,aKey,aValue)

    def __update__(self, aDictionary, aParentId, aArgument):
        for k,v in aDictionary.items():
            if k in aArgument:
                #variable local ya registrada ya que es un argumento
                self[k] = v
                return
            #se debe registrar argumento self?
            if not self.has_key(k):
                if not k == 'self':
                    self[k] = v
                    self.hT.packer.reset()
                    self.hT.packer.pack_int(self.hT.events['register'])
                    self.hT.packer.pack_int(self.hT.objects['local'])
                    self.hT.packer.pack_int(v)
                    self.hT.packer.pack_int(aParentId)
                    self.hT.packer.pack_string(k)
                    if self.hT.FLAG_DEBUGG:
                        print self.hT.events['register'],
                        print self.hT.objects['local'],
                        print v,
                        print aParentId,
                        print k
                        raw_input()
                    try:
                        self.hT._socket.sendall(self.hT.packer.get_buffer())
                    except:
                        print 'TOD está durmiendo :-('
                    

    def __updateAttr__(self, aDictionary, aParentId): 
        for k,v in aDictionary.items():
            #se debe registrar argumento self?
            if not self.has_key(k):
                if not k == 'self':
                    v = self.hT.Id.__get__()
                    self.hT.Id.__next__()
                    self[k] = v
                    self.hT.packer.reset()
                    self.hT.packer.pack_int(self.hT.events['register'])
                    self.hT.packer.pack_int(self.hT.objects['attribute'])
                    self.hT.packer.pack_int(v)
                    self.hT.packer.pack_int(aParentId)
                    self.hT.packer.pack_string(k)
                    if self.hT.FLAG_DEBUGG:
                        print self.hT.events['register'],
                        print self.hT.objects['attribute'],
                        print v,
                        print aParentId,
                        print k 
                        raw_input()                          
                    try:       
                        self.hT._socket.sendall(self.hT.packer.get_buffer())
                    except:
                        print 'TOD está durmiendo :-('
                        