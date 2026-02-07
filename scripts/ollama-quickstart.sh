#!/bin/bash
# Ollama Quick Start Script
# This script helps test Ollama installation on your Yocto system

set -e

echo "==================================="
echo "Ollama Quick Start Script"
echo "==================================="
echo ""

# Check if ollama is installed
if ! command -v ollama &> /dev/null; then
    echo "❌ Error: Ollama is not installed or not in PATH"
    echo "Please ensure ollama-bin package is installed in your image"
    exit 1
fi

echo "✓ Ollama binary found: $(which ollama)"
echo "✓ Ollama version: $(ollama --version)"
echo ""

# Check if service is running
echo "Checking Ollama service status..."
if systemctl is-active --quiet ollama; then
    echo "✓ Ollama service is running"
else
    echo "⚠ Ollama service is not running"
    echo "Starting Ollama service..."
    systemctl start ollama
    sleep 3
    if systemctl is-active --quiet ollama; then
        echo "✓ Ollama service started successfully"
    else
        echo "❌ Failed to start Ollama service"
        echo "Check logs with: journalctl -u ollama -f"
        exit 1
    fi
fi
echo ""

# Test API connectivity
echo "Testing API connectivity..."
if curl -s http://localhost:11434/api/version > /dev/null; then
    echo "✓ Ollama API is responding"
    VERSION=$(curl -s http://localhost:11434/api/version | grep -o '"version":"[^"]*' | cut -d'"' -f4)
    echo "  API Version: $VERSION"
else
    echo "❌ Cannot connect to Ollama API"
    echo "Check if service is listening: netstat -tlnp | grep 11434"
    exit 1
fi
echo ""

# List available models
echo "Checking for installed models..."
MODELS=$(ollama list 2>/dev/null || echo "")
if [ -z "$MODELS" ]; then
    echo "⚠ No models installed yet"
    echo ""
    echo "Recommended starter models:"
    echo "  - llama3.2:1b  (smallest, fastest, ~1GB)"
    echo "  - llama3.2:3b  (good balance, ~2GB)"
    echo "  - llama3.2     (best quality, ~4.7GB)"
    echo ""
    echo "To install a model, run:"
    echo "  ollama pull llama3.2:1b"
    echo ""
    read -p "Would you like to pull llama3.2:1b now? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Downloading llama3.2:1b (this may take a few minutes)..."
        ollama pull llama3.2:1b
        echo "✓ Model downloaded successfully"
    else
        echo "Skipping model download"
        exit 0
    fi
else
    echo "✓ Installed models:"
    echo "$MODELS"
fi
echo ""

# Quick test
echo "Running quick test..."
RESPONSE=$(curl -s http://localhost:11434/api/generate -d '{
  "model": "llama3.2:1b",
  "prompt": "Say hello in exactly 5 words",
  "stream": false
}' | grep -o '"response":"[^"]*' | cut -d'"' -f4)

if [ -n "$RESPONSE" ]; then
    echo "✓ Test successful!"
    echo "  Response: $RESPONSE"
else
    echo "⚠ Test response was empty"
fi
echo ""

echo "==================================="
echo "Setup Complete!"
echo "==================================="
echo ""
echo "Next steps:"
echo "1. Interactive chat:"
echo "   ollama run llama3.2:1b"
echo ""
echo "2. API usage:"
echo "   curl http://localhost:11434/api/generate -d '{"
echo '     "model": "llama3.2:1b",'
echo '     "prompt": "Your question here"'
echo "   }'"
echo ""
echo "3. List models:"
echo "   ollama list"
echo ""
echo "4. Remove a model:"
echo "   ollama rm <model-name>"
echo ""
echo "For more information, visit: https://ollama.com/library"
