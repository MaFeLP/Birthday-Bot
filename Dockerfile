# You can use the latest JDK, but I tested this configuration with Java 15.
FROM maven:3.8.1-openjdk-15
#FROM maven:3.8.1-openjdk-17

MAINTAINER MaFeLP <mafelp@protonmail.ch>

# ---BEGIN COMPILING THE SOURCES---
# Create the build directory
RUN mkdir /tmp/build/
WORKDIR /tmp/build

# Add the source files to the build directory
ADD . /tmp/build/

# Removes the logging configuration which enables debug logging.
RUN rm /tmp/build/src/main/resources/log4j2-test.xml
# Compile the Bot
RUN mvn clean verify

# Copy the built bot to /usr/bin/
RUN mv /tmp/build/target/Birthday-Bot-1.3.jar /usr/bin/Birthday-Bot.jar

# Clean up the build and source files
RUN rm -rf /tmp/build/ ${HOME}/.m2/
# ---END COMPILING THE SOURCES---

# If you have the container build on your local machine, you can comment the build instructions
# above and uncomment the next line, to include the pre-built bot file.
#ADD target/Birthday-Bot-1.2.jar /usr/bin/Birthday-Bot.jar

# Set the working directory, to which the config file should be remapped.
RUN mkdir /data
WORKDIR /data

# Execute the bot.
CMD ["java","-jar","/usr/bin/Birthday-Bot.jar"]