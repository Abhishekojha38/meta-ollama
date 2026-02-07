SUMMARY = "Ollama package group"
DESCRIPTION = "Package group for Ollama and related utilities for LLM deployment"
LICENSE = "MIT"

inherit packagegroup

PROVIDES = "${PACKAGES}"

PACKAGES = " \
    packagegroup-ollama \
    packagegroup-ollama-core \
    packagegroup-ollama-utils \
    packagegroup-ollama-networking \
"

RDEPENDS:packagegroup-ollama = " \
    packagegroup-ollama-core \
    packagegroup-ollama-utils \
    packagegroup-ollama-networking \
"

# Core Ollama packages
RDEPENDS:packagegroup-ollama-core = " \
    ollama-bin \
    ca-certificates \
"

# Utility packages for managing and monitoring Ollama
RDEPENDS:packagegroup-ollama-utils = " \
    curl \
    wget \
    jq \
    htop \
    vim \
    procps \
    lsof \
"

# Network utilities for API access
RDEPENDS:packagegroup-ollama-networking = " \
    iproute2 \
    iptables \
    openssh-sftp-server \
"
