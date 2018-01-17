FROM maven:3.3-jdk-8 AS jax-build
EXPOSE 5984 8080
COPY ./jaxrs /opt/maven
WORKDIR /opt/maven
RUN ["mvn", "clean", "install"]

FROM jax-build
ENTRYPOINT mvn jetty:run

