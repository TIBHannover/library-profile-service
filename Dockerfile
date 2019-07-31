FROM maven:3-jdk-11-slim as build
WORKDIR /tmp/profileservice
COPY pom.xml .
COPY src .
RUN mvn --batch-mode --update-snapshots -DskipTests clean dependency:list package
RUN mv target/library-profile-service-*.war target/library-profile-service.war


FROM tomcat:9

# Configure database via env variables:
# MARIADB_HOST		(default: mariadb)
# MARIADB_PORT		(default: 3306)
# MARIADB_DATABASE	(default: test)
# MARIADB_USER		(default: test)
# MARIADB_PASSWORD	(default: test)

# Configure import:
# IMPORT_DNB_TOKEN
# IMPORT_DNB_SCHEDULE
# IMPORT_BL_SCHEDULE

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

ADD envConf/docker/logback.xml /etc/profileservice/logback.xml
ADD envConf/docker/profileservice.properties /etc/profileservice/profileservice.properties
ADD envConf/docker/context.xml.default /usr/local/tomcat/conf/Catalina/localhost/context.xml.default

COPY --from=build /tmp/profileservice/target/library-profile-service.war /usr/local/tomcat/webapps/library-profile-service.war

EXPOSE 8080
ENTRYPOINT ["catalina.sh", "run"]
