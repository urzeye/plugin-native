package com.janetfilter.plugins.native_wrapper;

import com.janetfilter.core.Environment;
import com.janetfilter.core.plugin.MyTransformer;
import com.janetfilter.core.plugin.PluginConfig;
import com.janetfilter.core.plugin.PluginEntry;

import java.util.ArrayList;
import java.util.List;

public class NativePlugin implements PluginEntry {
    private final List<MyTransformer> transformers = new ArrayList<>();

    @Override
    public void init(Environment environment, PluginConfig config) {
        transformers.add(new WrapperTransformer(environment, config.getBySection("Class")));
    }

    @Override
    public String getName() {
        return "Native";
    }

    @Override
    public String getAuthor() {
        return "neo";
    }

    @Override
    public String getVersion() {
        return "v1.0.0";
    }

    @Override
    public String getDescription() {
        return "A plugin for the ja-netfilter, it is a wrapper for native methods.";
    }

    @Override
    public List<MyTransformer> getTransformers() {
        return transformers;
    }
}
