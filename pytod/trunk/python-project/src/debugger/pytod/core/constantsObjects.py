#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['objects', 'dataTypes','events']

objects = {
           'class':0,
           'method':1,
           'attribute':2,
           'function':3,
           'local':4,
           'probe':5,
           'thread':6
           }

dataTypes = {
             int.__name__:0,
             str.__name__:1,
             float.__name__:2,
             long.__name__:3,
             bool.__name__:4,
             tuple.__name__:5,
             list.__name__:6,
             dict.__name__:7
             }

events = {
          'register':0,
          'call':1,
          'set':2,
          'return':3
          }