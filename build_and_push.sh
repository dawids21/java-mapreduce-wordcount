#!/bin/bash
mvn clean package

docker build -t mapreduce/javamapreduce -f Dockerfile.server .
docker build -t mapreduce/javamapreduce-cli -f Dockerfile.cli .

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/v4e3t3o3
docker tag mapreduce/javamapreduce:latest public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce:err-det50
docker tag mapreduce/javamapreduce-cli:latest public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce-cli:latest
docker push public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce:err-det50
docker push public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce-cli:latest