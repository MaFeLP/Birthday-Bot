package com.github.mafelp.utils.Enums;

/**
 * The State the present builder can be in.
 */
public enum PresentBuilderState{
    /**
     * Start indicates it is initialising the wizard.
     */
    START,
    /**
     * It indicates that it is waiting for input on the title.
     */
    TITLE,
    /**
     * It indicates that it is waiting for input on the content.
     */
    CONTENT,
    /**
     * It indicates that it is waiting for input on an image link.
     */
    IMAGE,
    /**
     * It indicates that it is finished and now cleaning up.
     */
    FINISHED,
    /**
     * It indicates that the user cancelled the wizard and it is now cleaning up.
     */
    CANCELLED,
}
