# author: milton galo patricio inostroza aguilera minoztro@gmail.com
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

import os
import sys

def obtener_ubicacion():
	path=os.path.dirname(sys.argv[0])
	path=os.path.abspath(path)
	return path

def modificar_path():
	path=obtener_ubicacion()
	if "win" in sys.platform:
		path=path.split("\\")
		for i in range(1):
			path.remove(path[-1])
		return(formar_path(path,1))
	else:
		path=path.split("/")
		for i in range(1):
			path.remove(path[-1])
		return(formar_path(path))

def formar_path(path,opcion=None):
	if (opcion==None):
		path="/".join(["%s"%(i) for i in path])
	else:
		path="\\".join(["%s"%(i) for i in path])
	return(path)
