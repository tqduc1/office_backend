FROM openjdk:8-jre-alpine

MAINTAINER BQ AN <bqan@cmcglobal.vn>

ADD build/libs/office-management-backend*.jar app.jar

RUN /bin/sh -c 'touch /app.jar'

RUN apk add --upgrade ttf-dejavu

ENTRYPOINT ["java","-jar","/app.jar"]
