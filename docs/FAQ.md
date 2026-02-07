# Frequently Asked Questions (FAQ)

## General Questions

### Q: What is meta-ollama?
A: meta-ollama is a Yocto/OpenEmbedded layer that adds support for Ollama, a framework for running large language models (LLMs) locally on embedded Linux systems.

### Q: What Yocto versions are supported?
A: meta-ollama is compatible with walnascar, scarthgap, nanbield, and mickledore releases.

### Q: What architectures are supported?
A: Currently x86_64 (amd64) and aarch64 (arm64) are fully supported.

### Q: What is the difference between ollama and ollama-bin recipes?
A: 
- `ollama-bin`: Downloads pre-built binaries (faster, recommended for most users)
- `ollama`: Builds from source (requires Go, longer build time, more customizable)

## Installation Questions

### Q: How much storage space do I need?
A: Minimum recommendations:
- Base Ollama installation: ~500MB
- Small models (1-3B): 2-4GB
- Medium models (7B): 8-10GB
- Large models (13B+): 16GB+

Set `IMAGE_ROOTFS_EXTRA_SPACE = "5242880"` for ~5GB extra space.

### Q: How much RAM do I need?
A:
- 1-3B models: 4-8GB RAM
- 7B models: 8GB+ RAM
- 13B models: 16GB+ RAM
- 33B models: 32GB+ RAM

### Q: Can I use Ollama without systemd?
A: No, the current implementation requires systemd for service management. SysVinit support is not currently available.

### Q: Why is my image so large?
A: Ollama binary and dependencies add ~500MB. Additional space is allocated for models. You can reduce `IMAGE_ROOTFS_EXTRA_SPACE` if you plan to download smaller models.

## Configuration Questions

### Q: How do I change the default port?
A: Edit `/etc/systemd/system/ollama.service.d/override.conf`:
```ini
[Service]
Environment="OLLAMA_HOST=0.0.0.0:8080"
```
Then: `systemctl daemon-reload && systemctl restart ollama`

### Q: How do I change the model storage location?
A: Edit the override configuration:
```ini
[Service]
Environment="OLLAMA_MODELS=/custom/path/to/models"
```
Ensure the directory exists and is owned by the ollama user.

### Q: Can I pre-install models in the image?
A: Not in the current version. Models must be downloaded at runtime. This feature is planned for future releases.

### Q: How do I enable debug mode?
A: Add to override configuration:
```ini
[Service]
Environment="OLLAMA_DEBUG=1"
```
View logs with: `journalctl -u ollama -f`

## Runtime Questions

### Q: How do I download models?
A:
```bash
ollama pull llama3.2:1b
ollama pull phi3:mini
```

### Q: How do I list installed models?
A:
```bash
ollama list
```

### Q: How do I remove a model?
A:
```bash
ollama rm <model-name>
```

### Q: Can I run multiple models simultaneously?
A: Yes, but ensure you have sufficient RAM. The default configuration allows one loaded model at a time. Increase with:
```ini
Environment="OLLAMA_MAX_LOADED_MODELS=2"
```

### Q: Why is inference slow?
A: Possible reasons:
1. Running on CPU instead of GPU
2. Insufficient RAM causing swapping
3. Model too large for your hardware
4. Try smaller models (1B-3B) or enable GPU acceleration

## Troubleshooting

### Q: Service fails to start, what should I check?
A:
1. Check logs: `journalctl -u ollama -n 50`
2. Verify binary: `which ollama && ollama --version`
3. Check user: `id ollama`
4. Verify permissions: `ls -la /var/lib/ollama`
5. Check port availability: `netstat -tlnp | grep 11434`

### Q: Cannot download models, what to do?
A:
1. Check network connectivity: `ping ollama.com`
2. Verify DNS: `nslookup ollama.com`
3. Check disk space: `df -h`
4. View logs: `journalctl -u ollama -f` while pulling

### Q: Out of memory errors during inference?
A:
1. Use smaller models (llama3.2:1b instead of :7b)
2. Increase system swap
3. Reduce concurrent requests
4. Set model unload timeout: `Environment="OLLAMA_KEEP_ALIVE=5m"`

### Q: Model downloaded but not appearing in ollama list?
A:
1. Check ownership: `ls -la /var/lib/ollama/models/`
2. Ensure directory is writable by ollama user
3. Restart service: `systemctl restart ollama`

### Q: API returns connection refused?
A:
1. Check service: `systemctl status ollama`
2. Verify listening: `netstat -tlnp | grep 11434`
3. Check firewall rules
4. Verify OLLAMA_HOST setting

## GPU Questions

### Q: How do I enable NVIDIA GPU support?
A: Add the CUDA drivers to your image and install the ollama-cuda package:
```
IMAGE_INSTALL:append = " ollama-bin ollama-cuda nvidia-drivers cuda-toolkit"
```
Note: nvidia-drivers and cuda-toolkit recipes are not included in meta-ollama.

### Q: How do I enable AMD GPU support?
A: Install ROCm drivers and the ollama-rocm package:
```
IMAGE_INSTALL:append = " ollama-bin rocm-drivers"
```
Note: ROCm support requires AMD GPU with compatible GFX version.

### Q: How can I check if GPU is being used?
A:
- NVIDIA: `nvidia-smi` while running inference
- AMD: `rocm-smi`
- Check logs: `journalctl -u ollama -f` (shows GPU initialization)

## Performance Questions

### Q: What models should I use for my hardware?
A:
- 4-8GB RAM: llama3.2:1b, phi3:mini, qwen2.5:1.5b
- 8-16GB RAM: llama3.2:3b, phi3:3.8b
- 16GB+ RAM: llama3.2:7b, llama3:8b

### Q: How do I optimize for speed?
A:
1. Use GPU acceleration
2. Enable flash attention (NVIDIA): `Environment="OLLAMA_FLASH_ATTENTION=1"`
3. Keep models loaded: `Environment="OLLAMA_KEEP_ALIVE=-1"`
4. Use quantized models (smaller bit precision)

### Q: How do I optimize for memory?
A:
1. Use smaller models
2. Set unload timeout: `Environment="OLLAMA_KEEP_ALIVE=5m"`
3. Limit concurrent requests: `Environment="OLLAMA_NUM_PARALLEL=1"`

## Development Questions

### Q: How do I contribute to meta-ollama?
A: 
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly (see TESTING.md)
5. Submit a pull request

### Q: How do I update Ollama version?
A:
1. Update PV in the recipe
2. Update SRC_SHA256 checksums (get from ollama.com/download)
3. Test build and runtime
4. Update CHANGELOG.md

### Q: Can I use this in production?
A: Yes, but consider:
- Ensure adequate testing for your use case
- Plan for model storage and updates
- Consider security implications
- Have monitoring in place

## Integration Questions

### Q: How do I add Ollama to my existing image?
A: Add to your image recipe:
```
IMAGE_INSTALL:append = " ollama-bin"
IMAGE_ROOTFS_EXTRA_SPACE = "5242880"
```
Ensure systemd is enabled.

### Q: Can I use Ollama with Docker?
A: While Yocto can build Docker images, Ollama in this layer is designed for native deployment. For Docker, use the official ollama/ollama container.

### Q: How do I integrate with my application?
A: Use the Ollama API:
- HTTP API: http://localhost:11434/api/*
- See: https://github.com/ollama/ollama/blob/main/docs/api.md
- Libraries available for Python, JavaScript, Go, etc.

## Still Have Questions?

Check:
1. [README.md](README.md) - General overview
2. [INTEGRATION.md](docs/INTEGRATION.md) - Integration guide
3. [TESTING.md](docs/TESTING.md) - Testing procedures
4. [Ollama Documentation](https://github.com/ollama/ollama)
5. [Yocto Project Documentation](https://docs.yoctoproject.org/)

Or open an issue on the repository.
