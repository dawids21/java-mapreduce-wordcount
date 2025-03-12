# Java MapReduce WordCount application
## Building Docker images
### Server
```sh
docker build -t mapreduce/javamapreduce -f Dockerfile.server .
```
### CLI
```sh
docker build -t mapreduce/javamapreduce-cli -f Dockerfile.cli .
```
## Running Docker images
### Server
```sh
docker run --rm --network host -v $PWD/tmp/node:/app/node -v $PWD/tmp/public:/app/public mapreduce/javamapreduce
```
### CLI
```sh
docker run --rm --network host -it mapreduce/javamapreduce-cli
```