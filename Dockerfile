FROM jplu/java

MAINTAINER Julien Plu <julien.plu@eurecom.fr>

WORKDIR /adel

COPY target/adel-1.0-SNAPSHOT.jar adel-1.0-SNAPSHOT.jar

EXPOSE 7002 7003

VOLUME ["/adel/mappings", "/adel/profiles", "/adel/conf", "/adel/queries"]

CMD ["java", "--add-modules", "java.xml.bind", "-jar", "adel-1.0-SNAPSHOT.jar", "server", "conf/config.yaml"]
