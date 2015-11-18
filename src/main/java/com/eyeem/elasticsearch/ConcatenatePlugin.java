package com.eyeem.elasticsearch;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class ConcatenatePlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "analysis-concatenate";
    }

    @Override
    public String description() {
        return "Plugin that provides a Token Filter that recombines all of the tokens in a token stream back into one.";
    }

    @Override
    public void processModule(Module module) {
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
            analysisModule.addTokenFilter("concatenate", ConcatenateTokenFilterFactory.class);
        }
    }
}
