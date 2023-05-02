#!/bin/bash

# Name : Mike
# Date : 21 Aug 2022
# File : CuckooXporter.sh 

declare -r  ARCHITECTURE_32_BIT="32"
declare -r  ARCHITECTURE_64_BIT="64"

declare -ir EXIT_SUCCESS=0
declare -ir EXIT_FAILURE=1

declare -r  GHIDRA_INSTALL_DIR="/"
declare -r  GHIDRA_HEADLESS_ANALYZER_PATH="$GHIDRA_INSTALL_DIR/support/analyzeHeadless"
declare -r  GHIDRA_PROJECT_DIR="/tmp/"

declare -r  POSTSCRIPT_FILENAME="Exporter.java"

printf "[SYS] CuckooXporter by Mike\n"
printf "\n"

# Ensure that the correct number of arugments are present
if [ $# -ne 3 ]; then
	printf "[SYS] Usage: ./CuckooXporter.sh inputFilepath 32|64 outputFilepath\n"
	printf "\n"
	printf "[SYS] Exports an executable into C code via Ghidra.\n"
	exit $EXIT_FAILURE
fi

# Ensure that Ghidra exists in the specified directory
if [ ! -f $GHIDRA_HEADLESS_ANALYZER_PATH ]; then
	printf "[SYS] Ghidra does not exist in the directory '$GHIDRA_INSTALL_DIR' !\n"
	exit $EXIT_FAILURE
fi

# Ensure that the input file exists
if [ ! -f $1 ]; then
	printf "[ARG] The input file '$1' does not exist !\n"
	exit $EXIT_FAILURE
fi

# Ensure that the architecture is valid
if [ $2 != $ARCHITECTURE_32_BIT ] && [ $2 != $ARCHITECTURE_64_BIT ]; then
	printf "[ARG] The architecture of the executable must be either '$ARCHITECTURE_32_BIT'-bit or '$ARCHITECTURE_64_BIT'-bit !\n"
	exit $EXIT_FAILURE
fi

# Ensure that the output file does not exist
if [ -f $3 ]; then
	printf "[ARG] The output file '$3' already exists !\n"
	exit $EXIT_FAILURE
fi

# Ensure that the 'postScript' file exists in the current directory
if [ ! -f "./$POSTSCRIPT_FILENAME" ]; then
	printf "[ARG] The 'postScript' file '$POSTSCRIPT_FILENAME' does not exist in the current directory !\n"
	exit $EXIT_FAILURE
fi

./analyzeHeadless $GHIDRA_PROJECT_DIR $1 -import $1 -processor "x86:LE:$2:default" -scriptPath $(pwd) -postScript $POSTSCRIPT_FILENAME $3 -deleteProject
