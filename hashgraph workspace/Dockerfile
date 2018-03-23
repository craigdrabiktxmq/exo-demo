FROM maven:3.3-jdk-8 AS hg-build


#ADD . /opt/maven
#WORKDIR /opt/maven/
#ENTRYPOINT ./build.sh

ADD ./hashgraph /opt/maven
copy ./hashgraph/exo-config.hashgraph.json /opt/maven/exo-config.json
WORKDIR /opt/maven/source/socketdemo
RUN mvn install -e

FROM hg-build
EXPOSE 51200-51299
EXPOSE 50200-50299
EXPOSE 52200-52299
WORKDIR /opt/maven
#ENTRYPOINT ["java", "-jar", "swirlds.jar"]
ENTRYPOINT ["java", "-jar", "data/apps/SocketDemo.jar"]