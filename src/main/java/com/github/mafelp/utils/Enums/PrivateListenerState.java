package com.github.mafelp.utils.Enums;

/**
 * The Wizards a user can have running in its private chat.
 */
public enum PrivateListenerState {
    /**
     * None indicates there is no wizard running and the user can start one.
     */
    NONE,
    /**
     * Present indicates that there is a {@link com.github.mafelp.Builders.PresentBuilder} wizard running.
     */
    PRESENT,
}
