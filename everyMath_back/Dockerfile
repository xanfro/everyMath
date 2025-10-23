FROM quay.io/openjdk/openjdk:17-jre

MAINTAINER xanfro7@gmail.com

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

# 创建应用目录
RUN mkdir -p /everyMath

WORKDIR /everyMath

# 暴露端口
EXPOSE 8080

# 复制jar文件
COPY ./everyMath.jar ./app.jar

# 启动应用
CMD ["java", "-Xms128m", "-Xmx256m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
