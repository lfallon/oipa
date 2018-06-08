SHELL:=/bin/bash
SOURCE_PATH ?= $(CURDIR)
OIPA_PATH ?= $(SOURCE_PATH)/staging

OWNER=jeromebridge
IMAGE_NAME=oipa

# export GIT_EMAIL=jeromebridge@gmail.com && export GIT_PASS=Equisoft001 && export GIT_NAME="Jerome Bridge" && make upgrade
# export DOCKER_USER=jeromebridge && export DOCKER_PASS=XXX && make build push

# docker run -v $(SOURCE_PATH):/workspace -e USER=$(GIT_EMAIL) -e PASS=$(GIT_PASS) oipa/github commit --author="$(GIT_NAME) <$(GIT_EMAIL)>" -m "Upgrading to version $(shell $(call version))"
# git commit --author="Jerome Bridge <jeromebridge@gmail.com>" -m "Upgrading to version 10.2.0.25"
# git tag 10.2.0.25
# git push origin 10.2.0.25

# ~/.netrc
# machine github.com login jeromebridge password Equisoft001


# X upload artifacts
# x update .env
# X Validate environment variables are set
# commit changes
# create tag
# push tag
# create github release
# merge to master

# build and push image(s)
# push tag to docker registry


define version
	source .env; \
	echo "$${OIPA_VERSION}";
endef

define tag
	source .env; \
	classifier=$(1); \
	echo "$(OWNER)/$(IMAGE_NAME):$${OIPA_VERSION}-$${classifier}";
endef

define args
	cat .env | grep -v ^# | grep -v -e '^[[:space:]]*$$' | awk '$$0="--build-arg "$$0' | xargs;
endef

build: build-support
	docker build $(shell $(call args)) -t $(shell $(call tag,sqlserver)) .

push: login
	@echo 'publish $(OIPA_VERSION) to $(OWNER)'
	docker push $(shell $(call tag,sqlserver))

login: guard-DOCKER_USER guard-DOCKER_PASS
	@docker login -u "$(DOCKER_USER)" -p "$(DOCKER_PASS)"

build-support:
	cd support && docker build -t oipa/upload-builder -f Dockerfile.upload.builder .
	cd support && docker run -v $(SOURCE_PATH)/support/.tmp/target/upload:/target oipa/upload-builder copy
	cd support && docker build -t oipa/upload -f Dockerfile.upload .
	cd support && docker build -t oipa/github -f Dockerfile.github .

upgrade: build-support upload

upload: build-support guard-GIT_EMAIL guard-GIT_NAME guard-GIT_PASS
	docker run -v $(SOURCE_PATH)/env/upload:/src oipa/upload upload

guard-%:
	@ if [ "${${*}}" = "" ]; then \
	    echo "Environment variable $* not set"; \
	    exit 1; \
	fi

temp: build-support
	docker run -w /opt/working -v $(SOURCE_PATH):/opt/working ubuntu bash support/update-env.sh .env OIPA_VERSION $(shell docker run -v $(SOURCE_PATH)/staging:/src oipa/upload version)
	docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github add .env
	docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github diff --quiet --exit-code --cached || docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github commit -m "Upgrading to version $(shell $(call version))"
	docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github show-ref --tags --quiet --verify -- "refs/tags/$(shell $(call version))" && docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github tag -d $(shell $(call version))
	docker run -v $(SOURCE_PATH):/workspace -e EMAIL=$(GIT_EMAIL) -e NAME="${GIT_NAME}" -e PASS=$(GIT_PASS) oipa/github tag $(shell $(call version))
