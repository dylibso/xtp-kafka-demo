ROOT_DIRECTORY=.
PLUGINS := ${shell find ${ROOT_DIRECTORY} -type d -print -maxdepth 1 -mindepth 1}

.PHONY: all build test

all: build test

build: $(PLUGINS)
	$(foreach file, $(PLUGINS), cd $(file) && xtp plugin build && cd ..;)

test:  
	$(foreach file, $(PLUGINS), cd $(file) && xtp plugin test && cd ..;)

clean: $(PLUGINS)
	$(foreach file, $(PLUGINS), rm $(file)/dist/*.wasm;)
