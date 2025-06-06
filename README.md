# Java MapReduce Word Count
Repozytorium zawiera rozwiązanie problemu liczenia słów w tekście za pomocą MapReduce w języku Java.
## Aplikacja serwerowa
Aplikację najłatwiej uruchomić za pomocą narzędzia Docker.
W celu uruchomienia aplikacji serwerowej należy utworzyć w obecnym katalogu folder `tmp`, a następnie wywołać komendę:
```sh
docker run -d \
    --network host \
    -v $PWD/tmp/$NAME:/app/node \
    -v $PWD/tmp/public:/app/public \
    -e RMI_PORT=$RMI_PORT \
    -e NODE_ADDRESS=rmi://$IP:$RMI_PORT/node \
    -e KNOWN_NODES=$KNOWN_NODES \
    -e COOKIE=cookie \
    --name $NAME \
    public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce:latest
```
gdzie:
  - `RMI_PORT` - port na którym ma zostać uruchomiona aplikacja serwerowa, musi być unikalny dla każdej instancji na danym węźle,
  - `IP` - adres IP instancji,
  - `KNOWN_NODES` - lista instancji aplikacji dla przetwarzania na więcej niż jednym węźle w postaci `rmi://IP_1:RMI_PORT_1/node,rmi://IP_2:RMI_PORT_2/node`,
  - `NAME` - nazwa instancji.

Testowanie przetwarzania na więcej niż jednym węźle może być wykonane na tej samej maszynie i z wykorzystaniem adresu `localhost`.

## Aplikacja kliencka
W tym celu również należy wykorzystać narzędzie Docker:
```sh
docker run -it --rm \
    --network host \
    -v $PWD/tmp/public:/app/public \
    -e RMI_PORT=$RMI_PORT \
    --name mapreduce_cli \
    public.ecr.aws/v4e3t3o3/mapreduce/javamapreduce-cli:latest
```
gdzie:
  - `RMI_PORT` - port do którego ma się podłączyć aplikacja kliencka.

Następnie można wydawać polecenia:
  - `start "examples" "output" xyz.stasiak.javamapreduce.wordcount.WordCountMapper xyz.stasiak.javamapreduce.wordcount.WordCountReducer`,
  - `status ID`.

Ścieżki do plików należy podawać relatywnie do katalogu `$PWD/tmp/public`.