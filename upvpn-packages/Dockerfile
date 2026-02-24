# Copyright (C) 2023 upvpn LLC, GPL-3.0
# Copyright (C) 2022 Mullvad VPN AB, GPL-3.0

# To build the image (executed from the repository root):
# podman build -f building/Dockerfile . -t upvpn-app-build
#
# To run the image and build the app you need to mount the app's source directory into the
# container. You also probably want to mount in a directory for CARGO_HOME, so each container
# does not need to start from scratch with cloning the crates.io index, download all
# dependencies and building everything.
#
# podman run --rm \
#     -v /path/to/container_cache/target:/root/.cargo/target:Z \
#     -v /path/to/container_cache/registry:/root/.cargo/registry:Z \
#     -v .:/build:Z \
#     upvpn-app-build cargo make ...
#
# And add -e TARGETS="aarch64-unknown-linux-gnu" to build for ARM64

# === Stage 1: arm64 WebKit sysroot ===
# libwebkit2gtk-4.1-dev and its :arm64 variant conflict in Debian 12 multi-arch (they cannot
# be co-installed). Work around by installing webkit natively on arm64 and copying the
# compiled libraries into the final amd64 image.
FROM --platform=linux/arm64 debian@sha256:98f4b71de414932439ac6ac690d7060df1f27161073c5036a7553723881bffbe AS arm64-webkit-sysroot

RUN apt-get update -y && apt-get install -y --no-install-recommends \
    libwebkit2gtk-4.1-dev \
    libjavascriptcoregtk-4.1-dev \
    && rm -rf /var/lib/apt/lists/*

# === Stage 2: main builder image (amd64) ===
# Debian 12 (bookworm-slim) - provides GLib >= 2.70 and libwebkit2gtk-4.1 required by Tauri v2
FROM debian@sha256:98f4b71de414932439ac6ac690d7060df1f27161073c5036a7553723881bffbe

LABEL org.opencontainers.image.source=https://github.com/upvpn/upvpn-app
LABEL org.opencontainers.image.description="upvpn app Linux build container"
LABEL org.opencontainers.image.licenses=GPL-3.0

# === Define toolchain versions and paths ===

ENV CARGO_TARGET_DIR=/root/.cargo/target

ENV GOLANG_VERSION=1.18.5 \
    GOLANG_HASH=9e5de37f9c49942c601b191ac5fba404b868bfc21d446d6960acc12283d6e5f2

# === Install/set up the image ===

RUN dpkg --add-architecture arm64 && apt-get update -y && apt-get install -y \
    git \
    curl \
    gcc gcc-aarch64-linux-gnu \
    libdbus-1-dev libdbus-1-dev:arm64 \
    rpm \
    protobuf-compiler \
    # Tauri / TLS / GTK
    libgtk-3-dev \
    libssl-dev \
    libsoup-3.0-dev \
    libjavascriptcoregtk-4.1-dev \
    libwebkit2gtk-4.1-dev \
    libnftnl-dev \
    zip \
    libmnl-dev \
    libssl-dev:arm64 \
    libgtk-3-dev:arm64 \
    # For cross-compiling/linting towards Windows
    gcc-mingw-w64-x86-64 \
    && rm -rf /var/lib/apt/lists/*

# arm64 webkit libs cannot be co-installed with the amd64 variant (Debian 12 multi-arch
# conflict). Copy them from the arm64-webkit-sysroot stage instead.
COPY --from=arm64-webkit-sysroot /usr/lib/aarch64-linux-gnu/ /usr/lib/aarch64-linux-gnu/

RUN curl -Lo protoc-3.19.1-linux-x86_64.zip \
    https://github.com/protocolbuffers/protobuf/releases/download/v3.19.1/protoc-3.19.1-linux-x86_64.zip  && \
    unzip protoc-3.19.1-linux-x86_64.zip -d /tmp && \
    mv /tmp/bin/protoc /usr/bin/protoc && \
    rm protoc-3.19.1-linux-x86_64.zip

# === Rust ===

# Install latest stable Rust toolchain for both x86_64-unknown-linux-gnu and aarch64-unknown-linux-gnu,
# plus x86_64-pc-windows-gnu for Windows cross-compilation/linting
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | \
    sh -s -- -y \
    --default-toolchain 1.88 \
    --profile minimal \
    --component clippy \
    --target aarch64-unknown-linux-gnu \
    --target x86_64-pc-windows-gnu

ENV PATH=/root/.cargo/bin:$PATH
ENV CARGO_TARGET_AARCH64_UNKNOWN_LINUX_GNU_LINKER="aarch64-linux-gnu-gcc" \
    PKG_CONFIG_SYSROOT_DIR_aarch64_unknown_linux_gnu=/usr/lib/aarch64-linux-gnu

# === Install cargo tools ===
RUN cargo install cargo-make && \
    cargo install cargo-deb && \
    cargo install sd && \
    cargo install ripgrep && \
    cargo install cargo-about && \
    cargo install cargo-generate-rpm && \
    mv /root/.cargo/bin/cargo-make /usr/local/bin && \
    mv /root/.cargo/bin/cargo-deb /usr/local/bin && \
    mv /root/.cargo/bin/cargo-generate-rpm /usr/local/bin && \
    mv /root/.cargo/bin/sd /usr/local/bin && \
    mv /root/.cargo/bin/rg /usr/local/bin && \
    mv /root/.cargo/bin/cargo-about /usr/local/bin && \
    rm -rf /root/.cargo/git && \
    rm -rf /root/.cargo/registry && \
    rm -rf /root/.cargo/target

# === Volta for npm + node ===

ENV PATH=/root/.volta/bin:$PATH
ENV NODE_VERSION=20.18.0
ENV NPM_VERSION=10.8.2

RUN curl https://get.volta.sh | bash && \
    volta install node@${NODE_VERSION} && \
    volta install npm@${NPM_VERSION} && \
    npm config set update-notifier false

# === Golang ===

# Install golang
# Checksum from: https://go.dev/dl/
RUN curl -Lo go.tgz https://go.dev/dl/go${GOLANG_VERSION}.linux-amd64.tar.gz && \
    echo "${GOLANG_HASH} go.tgz" | sha256sum -c - && \
    tar -C /usr/local -xzf go.tgz && \
    rm go.tgz
ENV PATH=/usr/local/go/bin:$PATH


# === Git configuration ===
RUN git config --global url."https://github.com/openssl/openssl.git".insteadOf "git://git.openssl.org/openssl.git"

WORKDIR /build
