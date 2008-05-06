#! /usr/bin/python
# -*- coding: utf-8 -*-

__author__ = "Milton Inostroza Aguilera"
__email__ = "minoztro@gmail.com"
__all__ = ['Descriptor']

class Descriptor(object):

    def __setattr__(self, name, value):
        frame = sys._getframe()
        currentLasti = frame.f_lasti
        currentDepth = hT.__getDepthFrame__(frame)
        currentTimeStamp = time.time() 
        parentTimeStamp = hT.__getTimeStampParentFrame__(frame, currentTimeStamp)
        threadId = hT.__getThreadId__(thread.get_ident())
        id = hT.Id.__get__()
        key = type(self).__name__
        key = hT.__getClassKey__(key)
        if key == None:
            return
        obj = hT._class[key] 
        objId = obj.__getId__()
        obj.attribute.__updateAttr__({name:id},objId)
        hT.Id.__next__()
        #registramos un nuevo probe
        if not hT._probe.has_key((currentLasti,objId)):
            probeId = hT.__registerProbe__(currentLasti,objId)
        else:
            probeId = hT._probe[(currentLasti,objId)]
        hT.packer.reset()
        print hT.events['set'],
        hT.packer.pack_int(hT.events['set'])
        print hT.objects['attribute'],
        hT.packer.pack_int(hT.objects['attribute'])
        #print 'id =',id,
        print id,
        hT.packer.pack_int(id)
        #print 'target =',objId,
        print objId,
        hT.packer.pack_int(objId)
        #print 'value =',value,
        #TODO: ver caso cuando value es del tipo tuple, list, dict
        print value,
        if type(value) is (dict or tuple or list):
            hT.packer.pack_int(value)
        print probeId,
        hT.packer.pack_int(probeId)
        #print 'parent time stamp = %11.9f'%(parentTimeStamp),
        print '%11.9f'%(parentTimeStamp),
        hT.packer.pack_double(parentTimeStamp)        
        #print 'current depth =',currentDepth,
        print currentDepth,
        hT.packer.pack_int(currentDepth)
        #print 'current time stamp = %11.9f'%(currentTimeStamp),
        print '%11.9f'%(currentTimeStamp),
        hT.packer.pack_double(currentTimeStamp)
        #print 'current thread =',threadId
        print threadId,
        hT.packer.pack_int(threadId)
        object.__setattr__(self, name, value)