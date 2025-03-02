# Java MapReduce WordCount application
## Building Docker images
### Server
```sh
docker build -t javamapreduce -f Dockerfile.server .
```
### CLI
```sh
docker build -t javamapreduce-cli -f Dockerfile.cli .
```
## Running Docker images
### Server
```sh
docker run --rm --network host -v $PWD/tmp/node:/app/tmp/node -v $PWD/tmp/public:/app/tmp/public javamapreduce
```
### CLI
```sh
docker run --rm --network host -it javamapreduce-cli
```