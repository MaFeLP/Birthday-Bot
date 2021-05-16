# Birthday-Bot
A simple and small discord bot, used on birthdays. <br>
⚠️This README is not finished and does not contain any information about the bot! It is currently worked on! ⚠️

## System requirements
- Docker installed

## Start the container
⚠️This docker tag is currently not on the docker hub ⚠️ <br>
Run the following command with Administrator (sudo) permissions!

```bash
docker run -d --rm \
      -v /opt/Birthday-Bot/:/data \
      --name birthdaybot \
      mafelp/birthdaybot:latest
```

## Stop the container
Run the following command with Administrator (sudo) permissions!

```bash
docker stop birthdaybot
```

## Building from source
### System Requirements
- Docker installed
- Maven 3.8 installed

```bash
mvn clean package
docker build -t birthdaybot .
docker run -d --rm \
      -v /opt/Birthday-Bot/:/data \
      --name birthdaybot \
      birthdaybot:latest
```