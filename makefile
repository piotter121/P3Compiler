P3COMPILER=P3LangCompiler.jar
LLVMVERSION=3.6
SOURCE=przyklad.p3
OUTPUTNAME=program
CC=clang

all: $(SOURCE:.p3=.s)
	$(CC) $(SOURCE:.p3=.s) -o $(OUTPUTNAME)

$(SOURCE:.p3=.s): $(SOURCE:.p3=.ll)
	llc-$(LLVMVERSION) $(SOURCE:.p3=.ll)

$(SOURCE:.p3=.ll): $(SOURCE)
	java -jar $(P3COMPILER) $(SOURCE) $(SOURCE:.p3=.ll)

clean:
	rm *.ll *.s
