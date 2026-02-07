# meta-ollama

> [!IMPORTANT]
> **Work in progress:** This layer currently provides basic functionality and cleanup is required. Additional options and features will be added soon.

A Yocto/OpenEmbedded layer for adding Ollama support to embedded Linux systems.

## Description

This layer provides recipes to integrate Ollama, a framework for running large language models (LLMs) locally, into Yocto-based embedded Linux distributions.

## Features

- **Core Recipes**:`ollama` (source build) for advanced customization.
- **Architecture Support**: x86_64 and aarch64 (ARM64).
- **Yocto Compatibility**: Supports `walnascar`, `scarthgap`, `nanbield`, and `mickledore`.
- **Image Support**: Provides `ollama-image.bb` for a complete, ready-to-use image.

## Quick Start

### 1. Add the Layer

```bash
cd /path/to/yocto-project
# Assuming meta-ollama is in your sources directory
bitbake-layers add-layer sources/meta-ollama
```

### 2. Update Configuration

In your `local.conf` (or `build.conf`):

```bitbake
# Enable systemd (required)
INIT_MANAGER = "systemd"

# Add Ollama to your image
IMAGE_INSTALL:append = " ollama-bin"

# Ensure adequate storage (models are large!)
IMAGE_ROOTFS_EXTRA_SPACE = "5242880"  # ~5GB extra space
```

### 3. Build

```bash
bitbake playground-image
# OR build the dedicated Ollama image
bitbake ollama-image
```

## Usage

After booting your system:

### 1. Verify Service

```bash
systemctl status ollama
ollama --version
```

### 2. Pull and Run a Model

```bash
# Pull a small model for testing
ollama pull llama3.2:1b

# Run inference
ollama run llama3.2:1b "Explain Yocto Project"
```

### 3. API Usage

```bash
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.2:1b",
  "prompt": "What is embedded Linux?"
}'
```

## System Requirements

| Model Size | Min RAM | Min Storage | CPU Cores |
|------------|---------|-------------|-----------|
| 1-3B       | 4 GB    | 2 GB        | 2+        |
| 7B         | 8 GB    | 12 GB       | 4+        |
| 13B+       | 16 GB+  | 20 GB+      | 8+        |

## Layer Structure

```text
meta-ollama/
├── conf/
│   └── layer.conf                    # Layer configuration
├── recipes-ai/ollama/
│   ├── ollama_0.5.7.bb              # Source recipe (Advanced)
│   └── files/
│       ├── ollama.service            # Systemd service
│       └── ollama.conf               # Service config
├── recipes-core/
│   ├── images/
│   │   └── ollama-image.bb          # Complete image
│   └── packagegroups/
│       └── packagegroup-ollama.bb    # Package group
├── docs/                             # Detailed guides
├── examples/                         # Sample configs
└── scripts/                          # Utility scripts
```

## Configuration & Customization

### Changing Ollama Version

Edit `recipes-ai/ollama/ollama_0.15.6.bb` to update `PV` and `SRC_SHA256` for your target architectures.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails | `bitbake-layers show-layers` to check path |
| Service fails | `journalctl -u ollama -n 50` |
| Out of memory | Use a smaller model (e.g., 1b) |
| No network | `ping ollama.com` |

## Contributing

Contributions are welcome! Please submit issues and pull requests.

## License

MIT License - See [COPYING.MIT](file:///Users/abhishekojha/Desktop/meta-ollama/COPYING.MIT)

## References

- [Ollama Official](https://ollama.com)
- [Yocto Project](https://docs.yoctoproject.org)
