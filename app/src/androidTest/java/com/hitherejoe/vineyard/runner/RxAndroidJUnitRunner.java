package com.hitherejoe.vineyard.runner;


import android.os.Bundle;
import androidx.test.espresso.Espresso;

import com.hitherejoe.vineyard.util.RxIdlingExecutionHook;
import com.hitherejoe.vineyard.util.RxIdlingResource;

import rx.plugins.RxJavaPlugins;

/**
 * Runner that registers a Espresso Indling resource that handles waiting for
 * RxJava Observables to finish.
 * WARNING - Using this runner will block the tests if the application uses long-lived hot
 * Observables such us event buses, etc.
 */
public class RxAndroidJUnitRunner extends UnlockDeviceAndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        RxIdlingResource rxIdlingResource = new RxIdlingResource();
        RxJavaPlugins.getInstance()
                .registerObservableExecutionHook(new RxIdlingExecutionHook(rxIdlingResource));
        Espresso.registerIdlingResources(rxIdlingResource);
    }
}