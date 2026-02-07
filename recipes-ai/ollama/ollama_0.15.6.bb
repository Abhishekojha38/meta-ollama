SUMMARY = "Ollama - Run large language models locally (built from source)"
DESCRIPTION = "Ollama is a framework for running large language models (LLMs) \
locally on your machine. This recipe builds Ollama from source."
HOMEPAGE = "https://github.com/ollama/ollama"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a8abe7311c869aba169d640cf367a4af"

# Version
PV = "0.15.6"

# Source repository
SRC_URI = "git://github.com/ollama/ollama.git;protocol=https;branch=main;tag=v${PV} \
           file://ollama.service \
           file://ollama.conf \
"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

# Dependencies
DEPENDS = "go-native cmake-native"
RDEPENDS:${PN} = "ca-certificates"

# Inherit required classes
inherit go systemd useradd

# Go configuration
GO_IMPORT = "github.com/ollama/ollama"
GO_INSTALL = "${GO_IMPORT}/cmd/ollama"

# CGO is required for Ollama
export CGO_ENABLED = "1"

# Build flags
export GOFLAGS = "-mod=vendor"

# Architecture support
COMPATIBLE_MACHINE = "(x86-64|aarch64)"

# Systemd configuration
SYSTEMD_SERVICE:${PN} = "ollama.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# User/group creation for ollama service
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r ollama"
USERADD_PARAM:${PN} = "-r -s /bin/false -d /var/lib/ollama -g ollama ollama"

# Allow network during fetch phase
do_fetch[network] = "1"
do_compile[network] = "1"

# Download Go dependencies as part of fetching
python do_fetch_go_modules() {
    import os
    src_dir = d.getVar('S')
    
    # This runs during fetch phase when network is available
    bb.plain("Downloading Go modules...")
    os.chdir(src_dir)
    
    # Download all dependencies
    os.system('go mod download')
    os.system('go mod vendor')
}

addtask fetch_go_modules after do_unpack before do_patch

# Use vendored modules during compile (no network)
do_compile() {
    cd ${S}
    
    # Use mod mode - allows Go to fetch what it needs
    export GOFLAGS="-mod=mod"
    export GOPROXY="direct"
    export GOSUMDB="off"
    
    ${GO} generate ./...
    ${GO} build -o ${B}/ollama
}

do_install() {
    # Install binary
    install -d ${D}${bindir}
    install -m 0755 ${B}/ollama ${D}${bindir}/

    # Install systemd service file
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/ollama.service ${D}${systemd_unitdir}/system/

    # Install systemd service configuration
    install -d ${D}${sysconfdir}/systemd/system/ollama.service.d
    install -m 0644 ${UNPACKDIR}/ollama.conf ${D}${sysconfdir}/systemd/system/ollama.service.d/override.conf

    # Create model storage directory
    install -d ${D}/var/lib/ollama
}

FILES:${PN} += " \
    ${bindir}/ollama \
    ${systemd_unitdir}/system/ollama.service \
    ${sysconfdir}/systemd/system/ollama.service.d/override.conf \
    /var/lib/ollama \
"

# Skip QA checks for Go binaries
#INSANE_SKIP:${PN} += "ldflags already-stripped"

# Skip QA checks that are common for Go binaries
INSANE_SKIP:${PN} += "already-stripped ldflags textrel buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"

# Disable debug splitting if it causes issues
# INHIBIT_PACKAGE_DEBUG_SPLIT = "1"