FROM maven:3.8.5-openjdk-11

WORKDIR /usr/src/app

COPY . .

RUN mvn clean test allure:report

CMD ["mvn", "allure:serve"]