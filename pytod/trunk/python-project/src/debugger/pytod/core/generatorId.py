#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['generatorId']

class generatorId(object):
    
    def __init__(self):
        self.id = 1

    def __get__(self):
        return self.id

    def __next__(self):
        self.id = self.id + 1
        return 