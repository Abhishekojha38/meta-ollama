# Testing Guide for meta-ollama

This guide provides comprehensive testing procedures for the meta-ollama layer.

## Pre-Build Testing

### 1. Layer Configuration Validation

```bash
# Navigate to build directory
cd /path/to/yocto-playground
cqfd shell
source sources/poky/oe-init-build-env

# Add the layer
bitbake-layers add-layer ../sources/meta-ollama

# Verify layer is added
bitbake-layers show-layers | grep meta-ollama
```

Expected output:
```
 bitbake-layers show-layers | grep meta-ollama
meta-ollama           /home/aojha/yocto/yocto-playground/sources/meta-ollama         10
```

### 2. Recipe Validation

```bash
# Check if ollama-bin recipe is found
bitbake-layers show-recipes | grep ollama

# Show recipe details
bitbake -e ollama | grep ^PV=
bitbake -e ollama | grep ^LICENSE=
```

### 3. Dependency Check

```bash
# Check recipe dependencies
bitbake -g ollama
cat pn-buildlist | grep ollama
```

## Build Testing

### 1. Build ollama Package

```bash
# Clean build
bitbake -c cleansstate ollama

# Build
bitbake ollama
```

Expected: Build should complete without errors.

### 2. Build Ollama Image

```bash
# Build the complete image
bitbake ollama-image
```

### 3. Verify Package Contents

```bash
# List files in the package
oe-pkgdata-util list-pkg-files ollama

# Check package info
oe-pkgdata-util lookup-recipe ollama
```

## Runtime Testing

### 1. Boot the Image in QEMU

For x86_64:
```bash
runqemu qemux86-64 nographic slirp
```

For ARM64:
```bash
runqemu qemuarm64 nographic slirp
```

### 2. Verify Installation

After boot, login (root/root) and run:

```bash
# Check if ollama is installed
which ollama
ollama --version

# Check systemd service
systemctl status ollama

# Verify user exists
id ollama

# Check directories
ls -la /var/lib/ollama
ls -la /usr/bin/ollama
```

### 3. Service Testing

```bash
# Test service start/stop
systemctl stop ollama
systemctl start ollama
systemctl status ollama

# Check if service is listening
netstat -tlnp | grep 11434
# or
ss -tlnp | grep 11434

# Check logs
journalctl -u ollama -n 50
```

```bash
# Service status
root@playground-arm64:~# systemctl status ollama
* ollama.service - Ollama Service - Local Large Language Model Runtime
     Loaded: loaded (/usr/lib/systemd/system/ollama.service; enabled; preset: enabled)
    Drop-In: /etc/systemd/system/ollama.service.d
             `-override.conf
     Active: active (running) since Sat 2026-02-07 21:21:39 UTC; 51min ago
 Invocation: a1b1a4b9f7aa4cf8a833d9c7f4ba1372
       Docs: https://github.com/ollama/ollama/tree/main/docs
   Main PID: 465 (ollama)
      Tasks: 29 (limit: 2317)
     Memory: 1.7G (peak: 1.7G)
        CPU: 16min 15.085s
     CGroup: /system.slice/ollama.service
             |-465 /usr/bin/ollama serve
             `-940 /usr/bin/ollama runner --model /var/lib/ollama/models/blobs/sha256-74701a8c35f6c8d9a4b91f3f3497643001d63e0c7a84e085bed452548fa88d45 --port 38091

Feb 07 22:06:37 playground-arm64 ollama[465]: llama_context: graph nodes  = 503
Feb 07 22:06:37 playground-arm64 ollama[465]: llama_context: graph splits = 1
Feb 07 22:06:37 playground-arm64 ollama[465]: time=2026-02-07T22:06:37.407Z level=INFO source=server.go:1388 msg="llama runner started in 14.19 seconds"
Feb 07 22:06:37 playground-arm64 ollama[465]: time=2026-02-07T22:06:37.409Z level=INFO source=sched.go:537 msg="loaded runners" count=1
Feb 07 22:06:37 playground-arm64 ollama[465]: time=2026-02-07T22:06:37.410Z level=INFO source=server.go:1350 msg="waiting for llama runner to start responding"
Feb 07 22:06:37 playground-arm64 ollama[465]: time=2026-02-07T22:06:37.413Z level=INFO source=server.go:1388 msg="llama runner started in 14.20 seconds"
Feb 07 22:07:39 playground-arm64 ollama[465]: [GIN] 2026/02/07 - 22:07:39 | 200 |         1m22s |       127.0.0.1 | POST     "/api/generate"
Feb 07 22:08:08 playground-arm64 ollama[465]: [GIN] 2026/02/07 - 22:08:08 | 200 |     793.443µs |       127.0.0.1 | HEAD     "/"
Feb 07 22:08:10 playground-arm64 ollama[465]: [GIN] 2026/02/07 - 22:08:10 | 200 |  1.379909944s |       127.0.0.1 | POST     "/api/show"
Feb 07 22:08:59 playground-arm64 ollama[465]: [GIN] 2026/02/07 - 22:08:59 | 200 | 48.940214429s |       127.0.0.1 | POST     "/api/generate"
root@playground-arm64:~#

# Check port
root@playground-arm64:~# netstat -tlnp | grep 11434
tcp6       0      0 :::11434                :::*                    LISTEN      465/ollama          
root@playground-arm64:~# 

```

### 4. API Testing

```bash
# Test version endpoint
curl http://localhost:11434/api/version

# Expected output:
# {"version":"0.5.7"}

# Test tags endpoint (list models)
curl http://localhost:11434/api/tags
```

### 5. Model Download Test

```bash
# Download a small test model
root@playground-arm64:~# ollama pull llama3.2:1b
pulling manifest
pulling 74701a8c35f6: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 1.3 GB
pulling 966de95ca8a6: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 1.4 KB
pulling fcc5a6bec9da: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 7.7 KB
pulling a70ff7e570d9: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 6.0 KB
pulling 4f659a1e86d7: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏  485 B
verifying sha256 digest  
writing manifest
success
root@playground-arm64:~#
```

```bash
# Verify download
root@playground-arm64:~# ollama list
NAME           ID              SIZE      MODIFIED
llama3.2:1b    baf6a787fdff    1.3 GB    9 seconds ago
root@playground-arm64:~#
```

### 6. Inference Test

* Here is the simple prompt test.

```bash
# Simple prompt test
root@playground-arm64:~# echo "What is 2+2?" | ollama run llama3.2:1b
2 + 2 is 4.
root@playground-arm64:~#
```

* Here is the streaming test.

```bash
# Streaming test
root@playground-arm64:~# curl http://localhost:11434/api/generate -d '{
>   "model": "llama3.2:1b",
>   "prompt": "Count to 5",
>   "stream": true
> }'
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:10.143928072Z","response":"Here","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:10.864957098Z","response":"'s","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:11.583503103Z","response":" the","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:12.304001069Z","response":" count","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:13.046205974Z","response":" to","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:13.764689786Z","response":" ","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:14.513284189Z","response":"5","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:15.217384414Z","response":":\n\n","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:15.918574853Z","response":"1","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:16.624956003Z","response":".","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:17.340946189Z","response":" One","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:18.053286729Z","response":"\n","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:18.768059718Z","response":"2","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:19.475344534Z","response":".","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:20.194553556Z","response":" Two","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:20.905329056Z","response":"\n","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:21.612487954Z","response":"3","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:22.315506403Z","response":".","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:23.033080866Z","response":" Three","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:23.753806703Z","response":"\n","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:24.475330383Z","response":"4","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:25.199644738Z","response":".","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:25.923132746Z","response":" Four","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:26.634969049Z","response":"\n","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:27.345091974Z","response":"5","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:28.059101643Z","response":".","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:28.773653674Z","response":" Five","done":false}
{"model":"llama3.2:1b","created_at":"2026-02-07T21:29:29.486619162Z","response":"","done":true,"done_reason":"stop","context":[128006,9125,128007,271,38766,1303,33025,2696,25,679
0,220,2366,18,271,128009,128006,882,128007,271,2568,311,220,20,128009,128006,78191,128007,271,8586,596,279,1797,311,220,20,1473,16,13,3861,198,17,13,9220,198,18,13,14853,198,19,1
3,13625,198,20,13,21594],"total_duration":25995741937,"load_duration":1863492276,"prompt_eval_count":29,"prompt_eval_duration":4750372707,"eval_count":28,"eval_duration":19245981
481}
root@playground-arm64:~#
```

```log
root@playground-arm64:~# ollama run llama3.2:1b "Tell me a story in 50 words"  
As the sun set over the ocean, a young girl named Luna sat on the beach, watching the waves wash away her memories. She closed her eyes and let the salty air fill her 
lungs, remembering the laughter and adventures she'd shared with her grandfather by the sea. Time stood still in this moment of peace.

root@playground-arm64:~# 
```

### TODO

- [ ] Add more models
- [ ] Add more tests
- [ ] Add more features
