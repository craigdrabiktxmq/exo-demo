FROM maven:3.3-jdk-8 AS jax-build
EXPOSE 5984 8080
COPY ./jaxrs /opt/maven
COPY ./jaxrs/exo-config.docker.json /opt/maven/exo-config.json
WORKDIR /opt/maven
RUN ["mvn", "clean", "install"]

FROM jax-build
ENTRYPOINT mvn jetty:run

