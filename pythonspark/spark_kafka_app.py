from pyspark.sql import SparkSession
from pyspark.sql.functions import expr
from pyspark.sql.types import StringType
import os

spark = SparkSession.builder \
    .appName("FileToKafka") \
    .getOrCreate()

# Kafka параметры
kafka_bootstrap_servers = "kafka:29092"
kafka_topic = "customtopic"

# Чтение новых файлов из директории
input_directory = "/var/temp_dir"


file_stream = spark.readStream \
    .format("text") \
    .load(input_directory)

kafka_stream = file_stream.selectExpr("CAST(value AS STRING) AS value", "CAST(value AS STRING) AS key")


query = kafka_stream.writeStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", kafka_bootstrap_servers) \
    .option("topic", kafka_topic) \
    .option("checkpointLocation", "/var/temp_dir/checkpoint") \
    .trigger(processingTime="10 seconds")\
    .start()

query.awaitTermination()