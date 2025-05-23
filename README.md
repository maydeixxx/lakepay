# LakePay

## Launching project

Replace `token.signing.key` in userService/src/main/resources/application.yml with an encryption key for JWT tokens  
Replace `token.telegram.bot` in userService/src/main/resources/application.yml with the token from @BotFather  
#### Run the following commands:
```bash
docker-compose up -d
```

## Hard reset
If you encounter any configuration issues, it is recommended to first try to build the project from group up.
#### Run the following commands:
```bash
docker-compose down # Shut down containers
docker volume ls
# Delete lakepay_... volumes, for example:
docker volume rm lakepay_kafka_data
docker volume rm lakepay_postgres_data
# Rebuld the project
docker-compose up -d --build
```