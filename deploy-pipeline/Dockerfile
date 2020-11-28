FROM openjdk:11-jre-slim

RUN groupadd --gid 1000 tsb

RUN useradd --system --no-log-init -u 1000 -g 1000 tsb

COPY build/libs/*.jar /app.jar

RUN chown tsb:tsb /app.jar

USER tsb

ENV http_proxy http://proxy_access.eu-west-2.aws.uk.tsb:3128

ENV https_proxy http://proxy_access.eu-west-2.aws.uk.tsb:3128

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
