# Upgraded to Python 3.12 Slim based on local development environment
FROM python:3.12-slim

# Set environment variables
# PYTHONDONTWRITEBYTECODE: Prevents Python from writing .pyc files to disk
# PYTHONUNBUFFERED: Ensures stdout/stderr logs are sent straight to terminal (critical for Docker logs)
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Set the working directory
WORKDIR /app

# Install system dependencies required for Kafka C-extensions
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    librdkafka-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first to leverage Docker layer caching
COPY requirements.txt .

# Upgrade pip and install dependencies directly from the updated requirements.txt
# CoolProp 7.2.0 natively supports Python 3.12 wheels, so we no longer need the --only-binary hack
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy the application source code
COPY src/ /app/src/

# Create a non-root user for security best practices
RUN useradd -m workeruser
USER workeruser

# Run the Kafka consumer daemon
CMD ["python", "-m", "src.main"]