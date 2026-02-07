# Ollama Integration Guide for yocto-playground

This guide explains how to integrate the meta-ollama layer into your yocto-playground project.

## Overview

The meta-ollama layer adds support for running Ollama, a framework for local LLM deployment, on your Yocto-based embedded Linux system.

## Installation Steps

### 1. Clone the meta-ollama layer

Place the meta-ollama layer in your `sources` directory:

```bash
cd /path/to/yocto-playground/sources
git clone <meta-ollama-repository-url> meta-ollama
```

Or if you created it manually, copy it:
```bash
cp -r /path/to/meta-ollama /path/to/yocto-playground/sources/
```

### 2. Update layers.conf

Add meta-ollama to your `layers.conf` file:

```
# Add to layers.conf
${TOPDIR}/../sources/meta-ollama
```

### 3. Configure build.conf

Update your `build.conf` to ensure systemd is enabled:

```bash
# In build.conf
INIT_MANAGER = "systemd"
```

### 4. Update local.conf or image recipe

Add Ollama to your image. Choose one of these methods:

#### Method A: Using local.conf
```bash
# Add to conf/local.conf
IMAGE_INSTALL:append = " ollama-bin"
IMAGE_ROOTFS_EXTRA_SPACE = "5242880"  # ~5GB for models
```

#### Method B: Build the Ollama image
```bash
cqfd run ./build.sh -- bitbake ollama-image
```

#### Method C: Add to your existing image recipe
```bash
# In your playground-image recipe
IMAGE_INSTALL:append = " packagegroup-ollama"
```

## Architecture Support

The meta-ollama layer supports:

- **x86_64 (amd64)**: Full support for Intel/AMD processors
- **aarch64 (arm64)**: Full support for ARM64 processors

Verify your machine architecture in `build.conf`:
```bash
MACHINE ?= "playground-x86"     # or
MACHINE ?= "playground-arm64"
```

## Building the Image

### Option 1: Using build.sh with CQFD

```bash
# Build with ollama included
cqfd run ./build.sh -- bitbake playground-image

# Or build the dedicated ollama image
cqfd run ./build.sh -- bitbake ollama-image
```

### Option 2: Manual build

```bash
# Initialize environment
cqfd shell
source sources/poky/oe-init-build-env

# Add layer if not already added
bitbake-layers add-layer ../sources/meta-ollama

# Build
bitbake ollama-image
```

## Post-Installation Configuration

After booting your image:

### 1. Verify Ollama service

```bash
systemctl status ollama
```

### 2. Enable Ollama service (if not auto-enabled)

```bash
systemctl enable ollama
systemctl start ollama
```

### 3. Pull a model

```bash
# Pull a small model for testing
ollama pull llama3.2:1b

# Or a larger, more capable model
ollama pull llama3.2:3b
```

### 4. Run a model

```bash
ollama run llama3.2:1b
```

### 5. Use the API

```bash
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.2:1b",
  "prompt": "Explain quantum computing in simple terms."
}'
```

## Storage Considerations

Models require significant storage:

- **1B models**: ~1-2 GB
- **3B models**: ~2-4 GB  
- **7B models**: ~4-8 GB
- **13B models**: ~8-16 GB

Ensure your image has adequate storage:

```bash
# In local.conf or image recipe
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"  # 10GB
```

## Memory Requirements

Minimum RAM requirements by model size:

- **1-3B models**: 4-8 GB RAM
- **7B models**: 8 GB RAM
- **13B models**: 16 GB RAM
- **33B models**: 32 GB RAM

## Network Configuration

Ollama listens on port 11434 by default. To access from external machines:

```bash
# The service file already sets OLLAMA_HOST=0.0.0.0:11434
# Verify with:
netstat -tlnp | grep 11434
```

## GPU Acceleration (Optional)

### NVIDIA CUDA Support

To enable CUDA acceleration, you'll need to:

1. Add NVIDIA GPU drivers to your image
2. Install CUDA toolkit
3. Add the ollama-cuda package:

```bash
IMAGE_INSTALL:append = " ollama-bin ollama-cuda"
```

### AMD ROCm Support

For AMD GPUs, similar steps apply with ROCm drivers.

## Troubleshooting

### Issue: Service fails to start

```bash
# Check logs
journalctl -u ollama -f

# Verify binary
which ollama
ollama --version
```

### Issue: Out of memory

Reduce model size or increase system RAM. Try smaller models:
- llama3.2:1b
- phi3:mini

### Issue: Cannot download models

Ensure network connectivity:
```bash
# Test connectivity
ping -c 3 ollama.com
curl -I https://ollama.com
```

### Issue: Slow inference

1. Use GPU acceleration if available
2. Use quantized models (smaller, faster)
3. Reduce context window size

## Examples

### Example 1: Question Answering

```bash
ollama run llama3.2:3b "What is the capital of France?"
```

### Example 2: Code Generation

```bash
ollama run llama3.2:3b "Write a Python function to calculate factorial"
```

### Example 3: API Integration

```python
import requests
import json

def ask_ollama(prompt, model="llama3.2:1b"):
    response = requests.post(
        'http://localhost:11434/api/generate',
        json={
            'model': model,
            'prompt': prompt,
            'stream': False
        }
    )
    return response.json()['response']

result = ask_ollama("Explain Docker containers")
print(result)
```

## Performance Tuning

### Environment Variables

Edit `/etc/systemd/system/ollama.service.d/override.conf`:

```ini
[Service]
# Keep models loaded in memory
Environment="OLLAMA_KEEP_ALIVE=-1"

# Enable flash attention (NVIDIA only)
Environment="OLLAMA_FLASH_ATTENTION=1"

# Increase parallel requests
Environment="OLLAMA_NUM_PARALLEL=4"

# Debug mode
Environment="OLLAMA_DEBUG=1"
```

Then reload:
```bash
systemctl daemon-reload
systemctl restart ollama
```

## Advanced: Building from Source

If you need to build Ollama from source instead of using pre-built binaries:

```bash
# In local.conf
PREFERRED_PROVIDER_ollama = "ollama"
IMAGE_INSTALL:append = " ollama"
```

This uses the `ollama_0.5.7.bb` recipe which builds from source.

## Integration with Existing Images

To add Ollama to your existing `playground-image`:

```bash
# Edit sources/meta-playground/recipes-core/images/playground-image.bb
IMAGE_INSTALL:append = " ollama-bin"
IMAGE_ROOTFS_EXTRA_SPACE = "5242880"
```

## References

- [Ollama Documentation](https://github.com/ollama/ollama)
- [Ollama API Reference](https://github.com/ollama/ollama/blob/main/docs/api.md)
- [Yocto Project Documentation](https://docs.yoctoproject.org/)
- [Your yocto-playground README](../README.md)
