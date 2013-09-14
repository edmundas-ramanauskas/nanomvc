package com.nanomvc;

public class ModelFactory {

    public static Model loadModel(Class modelClass) {
        return new Model(modelClass);
    }

    public static Model loadModel(String modelClass) {
        Model model = null;
        try {
            ClassLoader classLoader = Model.class.getClassLoader();
            Class clas = classLoader.loadClass("com.shop.mvc.models." + modelClass);
            model = new Model(clas);
        } catch (ClassNotFoundException | IllegalArgumentException | SecurityException ex) {
        }
        return model;
    }
}