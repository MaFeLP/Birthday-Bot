FROM openjdk:16-jdk
MAINTAINER Max Ove Fehlinger
RUN mkdir /data
ADD target/Birthday-Bot-1.2.jar /usr/bin/Birthday-Bot.jar
#ADD ./config.yml /opt/Birthday-Bot/config.yml
WORKDIR /data
CMD ["java","-jar","/usr/bin/Birthday-Bot.jar"]
