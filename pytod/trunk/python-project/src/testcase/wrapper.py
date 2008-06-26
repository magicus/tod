#! /usr/bin/python
# -*- coding: utf-8 -*-
import sys
sys.path.append('/media/WD Passport/eclipse/workspace/python-project/src')
from debugger.pytod.core.hunterTrace import hT
if __name__ == '__main__':
    print sys.argv
    execfile('%s'%(sys.argv[1]),locals(),globals())