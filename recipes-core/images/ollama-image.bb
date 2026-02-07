SUMMARY = "A development image with Ollama for running LLMs locally"
DESCRIPTION = "Minimal image with Ollama support for testing and development. \
Based on core-image-base with networking and systemd support."

# Base image to extend
require recipes-core/images/core-image-base.bb

# Image features
IMAGE_FEATURES += "ssh-server-dropbear"

# Additional packages for Ollama
IMAGE_INSTALL:append = " \
    ollama \
    curl \
    ca-certificates \
    vim \
    htop \
    ncurses \
    procps \
"

# Ensure systemd is used
IMAGE_INIT_MANAGER = "systemd"

# Extra space for models (approximately 5GB)
# Adjust this based on which models you plan to use
IMAGE_ROOTFS_EXTRA_SPACE = "5242880"

# Network configuration
IMAGE_INSTALL:append = " \
    iproute2 \
    iptables \
"

# Optional: Add tools for debugging and monitoring
IMAGE_INSTALL:append = " \
    strace \
    lsof \
    wget \
"

# License
LICENSE = "MIT"
