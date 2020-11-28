FROM openjdk:11.0.2-jre-slim
USER root
WORKDIR /
COPY build/libs/tsb-onboarding-restproxy-otp-verification-1.0.jar /
COPY src/main/resources/application-kubernetes.yml /
EXPOSE 8085
ENV STORE_ENABLED=true
ENV WORKER_ENABLED=true
# Use this line if you need to keep the container alive while you investigate the connection issues between containers
# CMD ["tail", "-f", "/dev/null"]
# When you're done, comment the previous CMD line and uncomment this one
CMD ["java","-jar", "/tsb-onboarding-restproxy-otp-verification-1.0.jar", "--spring.config.name=/application-kubernetes.yml", "--spring.config.location=/application-kubernetes.yml"]
