import os
import sys
import shutil
import urllib

AGENT_LIBS_URL = "http://pleiad.dcc.uchile.cl/files/tod/tod-agent/"

def release(version):
	zzuMod = useSVN('zz.utils', 'http://stgo.dyndns.org/svn/gpothier/devel/zz.utils')
	tpMod = useSVN('tod.plugin', 'http://pleiad.dcc.uchile.cl/svn/tod/tod.plugin/trunk/')
	tpaMod = useSVN('tod.plugin.ajdt', 'http://pleiad.dcc.uchile.cl/svn/tod/tod.plugin.ajdt/')
	todMod = useSVN('TOD', 'http://pleiad.dcc.uchile.cl/svn/tod/core/trunk/')
	taMod = useSVN('TOD-agent', 'http://pleiad.dcc.uchile.cl/svn/tod/agent/trunk/')
	toddbgridMod = useSVN('TOD-dbgrid', 'http://pleiad.dcc.uchile.cl/svn/tod/dbgrid/trunk/')
	#zzcMod = useSVN('zz.csg', 'http://stgo.dyndns.org/svn/gpothier/devel/zz.csg/trunk')
	zzeuMod = useSVN('zz.eclipse.utils', 'http://stgo.dyndns.org/svn/gpothier/devel/zz.eclipse.utils/')
	#reflexMod = useSVN('reflex', 'http://reflex.dcc.uchile.cl/svn/base/trunk/')
	#pomMod = useSVN('pom', 'http://reflex.dcc.uchile.cl/svn/plugins/pom/')
	
	
	print 'Checking and downloading native library builds...\n'
	cwd = os.getcwd()
	os.chdir(taMod.path + '/src/native')

	libs = ["libtod-agent.so", "libtod-agent_x64.so", "libtod-agent.dylib", "tod-agent.dll"]

	for lib in libs:
		# Get local signature
		lsig = os.popen('./svn-sig.sh').read()

		# Get remote signature
		rsig = urllib.urlopen(AGENT_LIBS_URL + lib + "-sig.txt").read()

		if lsig != rsig:
			print "SVN revisions do not match for " + lib
			sys.exit(-1)
	
		urllib.urlretrieve(AGENT_LIBS_URL + lib, "../../" + lib)

	os.chdir(cwd)
	print "Done."
	#sys.exit(0)
	
	shutil.copy('ant.settings', tpMod.path)

	print 'Cleaning...\n'
	antBuild('TOD', 'build.xml', 'clean')
	antBuild('TOD-agent', 'build.xml', 'clean')
	antBuild('TOD-dbgrid', 'build.xml', 'clean')
	antBuild('zz.utils', 'build.xml', 'clean')
	antBuild('zz.eclipse.utils', 'build-plugin.xml', 'clean')
	antBuild('tod.plugin', 'build-plugin.xml', 'clean')
	antBuild('tod.plugin.ajdt', 'build-plugin.xml', 'clean')
	
	print 'Building plugin and dependencies...\n'
	setEclipsePluginVersion('tod.plugin', version)
	antBuild('tod.plugin', 'build-plugin.xml', 'plugin')
	
	setEclipsePluginVersion('tod.plugin.ajdt', version)
	antBuild('tod.plugin.ajdt', 'build-plugin.xml', 'plugin')
	
	print 'Packaging plugins...\n'
	os.mkdir('release/plugins')
	shutil.copytree(tpMod.path + '/build/tod.plugin', 'release/plugins/tod.plugin_' + version)
	shutil.copytree(tpaMod.path + '/build/tod.plugin.ajdt', 'release/plugins/tod.plugin.ajdt_' + version)
	shutil.copytree(zzeuMod.path + '/build/zz.eclipse.utils', 'release/plugins/zz.eclipse.utils_1.0.0')

	os.chdir('release')
	ret = os.system('zip -rm tod.plugin_' + version + '.zip plugins')
	os.chdir(cwd)
	
	print 'Packaging standalone database...\n'
	antBuild('TOD-dbgrid', 'build.xml', 'release-db')
	shutil.copy(toddbgridMod.path + '/build/tod-db.zip', 'release/tod-db_' + version + '.zip')
	shutil.copy(toddbgridMod.path + '/build/tod-db.tar.gz', 'release/tod-db_' + version + '.tar.gz')
	
	
