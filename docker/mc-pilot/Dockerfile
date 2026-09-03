FROM node:24-trixie-slim

ARG MC_PILOT_COMMIT

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        git \
        libasound2t64 \
        libfontconfig1 \
        libfreetype6 \
        libgl1 \
        libgl1-mesa-dri \
        libgtk-3-0 \
        libx11-6 \
        libxcursor1 \
        libxext6 \
        libxi6 \
        libxinerama1 \
        libxrandr2 \
        libxrender1 \
        libxtst6 \
        openjdk-21-jdk-headless \
        xauth \
        xvfb \
    && rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/kzheart/mc-pilot.git /opt/mc-pilot \
    && cd /opt/mc-pilot \
    && git checkout --detach "$MC_PILOT_COMMIT" \
    && test "$(git rev-parse HEAD)" = "$MC_PILOT_COMMIT" \
    && npm ci --ignore-scripts \
    && npm run build \
    && rm -rf /root/.gradle /root/.npm /opt/mc-pilot/.git

RUN ln -s /opt/mc-pilot/cli/bin/mct /usr/local/bin/mct \
    && chmod +x /opt/mc-pilot/cli/bin/mct \
    && mkdir -p /home/node/.mct \
    && chown node:node /home/node/.mct

COPY bootstrap.sh entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/bootstrap.sh /usr/local/bin/entrypoint.sh \
    && ln -s /usr/local/bin/bootstrap.sh /usr/local/bin/mc-pilot-bootstrap \
    && ln -s /usr/local/bin/entrypoint.sh /usr/local/bin/mc-pilot-entrypoint

USER node
WORKDIR /workspace
ENV DISPLAY=:99 \
    MCT_SKILL_TARGETS=none

ENTRYPOINT ["mc-pilot-entrypoint"]
CMD ["sleep", "infinity"]
