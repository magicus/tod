/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <vector>

#include "workingset.h"

bool startsWith(const char* aString, const char* aPrefix)
{
	int len = strlen(aPrefix);
	return strncmp(aPrefix, aString, len) == 0;
}

void test(CompoundClassSet* set, char* name)
{
	printf("%s -> %d\n", name, set->accept(name));
}

int main(int, char**)
{
	printf("Let's go\n");
	CompoundClassSet* set;
	
	set = parseWorkingSet("[-java/io/** +java/io/yes/* -tod/agent]");
	test(set, "java/io/Tata");
	test(set, "java/io/blip/Titi");
	test(set, "java/io/yes/Tata");
	test(set, "java/io/yes/no/Tata");
	
	set = parseWorkingSet("[+tod.impl.evdbng.db.IndexSet +tod.impl.evdbng.db.SimpleIndexSet +zz.utils.cache.MRUBuffer +tod.tools.ConcurrentMRUBuffer +tod.impl.evdbng.db.IndexSet$IndexManager +tod.impl.evdbng.db.IndexSet$BTreeWrapper]");
	test(set, "tod/impl/evdbng/db/Indexes");
	test(set, "tod/impl/evdbng/db/IndexSet$IndexManager");
	test(set, "tod/impl/evdbng/db/IndexSet");
}
