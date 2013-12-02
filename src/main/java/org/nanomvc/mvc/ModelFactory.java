package org.nanomvc.mvc;

public class ModelFactory {

    public static Model loadModel(Class modelClass) {
        return new Model(modelClass);
    }
}