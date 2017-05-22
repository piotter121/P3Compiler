LLVMVERSION=3.6
CURRENT_DIR=$(shell pwd)
P3COMPILER=$(CURRENT_DIR)/target/Projekt3-1.0-SNAPSHOT-jar-with-dependencies.jar
WORKINGDIR=$(CURRENT_DIR)/src/test/resources
SOURCE=przyklad.p3
OUTPUTNAME=program
CC=clang
JAVA=java
LLVM-COMPILER=llc

all: $(WORKINGDIR)/$(SOURCE:.p3=.s)
	$(CC) $(WORKINGDIR)/$(SOURCE:.p3=.s) -o $(OUTPUTNAME)

$(WORKINGDIR)/$(SOURCE:.p3=.s): $(WORKINGDIR)/$(SOURCE:.p3=.ll)
	$(LLVM-COMPILER)-$(LLVMVERSION) $(WORKINGDIR)/$(SOURCE:.p3=.ll)

$(WORKINGDIR)/$(SOURCE:.p3=.ll): $(WORKINGDIR)/$(SOURCE)
	$(JAVA) -Duser.dir=$(WORKINGDIR) -jar $(P3COMPILER) $(SOURCE) $(SOURCE:.p3=.ll)

clean:
	rm $(WORKINGDIR)/*.ll $(WORKINGDIR)/*.s
