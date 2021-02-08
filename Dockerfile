# The app is installed at /app
# It assumes the project is mounted as volume /workspace
FROM openjdk:11.0.10-jre

RUN apt-get update && apt-get install -y git

RUN git config --global user.name "ByteLegendBot" && git config --global user.email "bot@bytelegend.com"

RUN mkdir /app

WORKDIR /app

COPY build/install/remember-brave-people /app

ENTRYPOINT ["/app/bin/remember-brave-people"]

