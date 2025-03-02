package xyz.stasiak.javamapreduce.wordcount;

import java.util.Arrays;
import java.util.List;

import xyz.stasiak.javamapreduce.map.Mapper;
import xyz.stasiak.javamapreduce.util.KeyValue;

public class WordCountMapper implements Mapper {

    @Override
    public List<KeyValue> map(String input) {
        return Arrays.stream(input.split(" "))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> new KeyValue(word, "1"))
                .toList();
    }
}
