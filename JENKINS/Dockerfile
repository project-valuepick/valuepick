FROM jenkins/jenkins:lts

USER root

# 필수 유틸리티 설치 (software-properties-common 제거)
RUN apt-get update && apt-get -y install --no-install-recommends \
    curl \
    unzip \
    gnupg \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Docker CLI 설치 (공식 스크립트 이용 - docker-ce 포함)
RUN curl -fsSL https://get.docker.com | sh

# AWS CLI 설치
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws

USER jenkins