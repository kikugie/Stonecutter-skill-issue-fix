package dev.kikugie.stonecutter.ide

import dev.kikugie.stonecutter.controller.StonecutterController

/**Defines, which run configurations are created by the plugin.*/
public enum class RunConfigType {
    /**Creates configurations for tasks registered in [StonecutterController.configureProject].*/
    SWITCH,
    /**Creates configurations for tasks registered with [StonecutterController.registerChiseled].*/
    CHISEL
}