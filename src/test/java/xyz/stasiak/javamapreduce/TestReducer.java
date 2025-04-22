package xyz.stasiak.javamapreduce;

import java.util.List;

import xyz.stasiak.javamapreduce.reduce.Reducer;

public class TestReducer implements Reducer {

    @Override
    public String reduce(String key, List<String> values) {
        var count = values.stream().mapToInt(Integer::parseInt).sum();
        return String.valueOf(count);
    }
}
