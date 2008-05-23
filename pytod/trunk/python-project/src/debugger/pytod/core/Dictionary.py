#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Dictionary']

import xdrlib

class Dictionary(dict):
    
    def __init__(self, hT):
        self.hT = hT
        dict.__init__(self)

    def __setitem__(self, aKey, aValue):
        dict.__setitem__(self,aKey,aValue)

    def __update__(self, aDictionary, aParentId, aArgument):
        for theKey, theValue in aDictionary.items():
            if theKey in aArgument:
                #variable local ya registrada ya que es un argumento
                self[theKey] = theValue
                return
            #se debe registrar argumento self?
            if not self.has_key(theKey):
                if not theKey == 'self':
                    self[theKey] = theValue
                    self.hT.packer.reset()
                    self.hT.packer.pack_int(self.hT.events['register'])
                    self.hT.packer.pack_int(self.hT.objects['local'])
                    self.hT.packer.pack_int(theValue)
                    self.hT.packer.pack_int(aParentId)
                    self.hT.packer.pack_string(theKey)
                    if self.hT.FLAG_DEBUGG:
                        print self.hT.events['register'],
                        print self.hT.objects['local'],
                        print theValue,
                        print aParentId,
                        print theKey
                        raw_input()
                    try:
                        self.hT._socket.sendall(self.hT.packer.get_buffer())
                    except:
                        print 'TOD está durmiendo :-('
                    

    def __updateAttr__(self, aDictionary, aParentId): 
        for theKey, theValue in aDictionary.items():
            #se debe registrar argumento self?
            if not self.has_key(theKey):
                if not theKey == 'self':
                    theValue = self.hT.Id.__get__()
                    self.hT.Id.__next__()
                    self[theKey] = theValue
                    self.hT.packer.reset()
                    self.hT.packer.pack_int(self.hT.events['register'])
                    self.hT.packer.pack_int(self.hT.objects['attribute'])
                    self.hT.packer.pack_int(theValue)
                    self.hT.packer.pack_int(aParentId)
                    self.hT.packer.pack_string(theKey)
                    if self.hT.FLAG_DEBUGG:
                        print self.hT.events['register'],
                        print self.hT.objects['attribute'],
                        print theValue,
                        print aParentId,
                        print theKey 
                        raw_input()                          
                    try:       
                        self.hT._socket.sendall(self.hT.packer.get_buffer())
                    except:
                        print 'TOD está durmiendo :-('
                        