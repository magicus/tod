#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <vector>


#include "utils.h"

void fatal_error(char* message)
{
	printf(message);
	exit(-1);
}

void fatal_ioerror(char* message)
{
	perror(message);
	exit(-1);
}
