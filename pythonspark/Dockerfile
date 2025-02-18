FROM bitnami/spark:3.4.3

RUN pip install --no-cache-dir pyspark kafka-python

WORKDIR /app

USER root

COPY spark_kafka_app.py /app/spark_kafka_app.py

RUN mkdir -p /var/temp_dir && chmod -R 777 /var/temp_dir

# Копируем файлы из папки data на хосте в /var/temp_dir контейнера для проверки работоспособности
# COPY data /var/temp_dir

RUN mkdir -p /var/temp_dir/checkpoint && chmod -R 777 /var/temp_dir/checkpoint

ENV SPARK_USER=root
ENV KAFKA_BROKER=kafka:29092

CMD ["spark-submit", "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.4.3", "/app/spark_kafka_app.py"]
