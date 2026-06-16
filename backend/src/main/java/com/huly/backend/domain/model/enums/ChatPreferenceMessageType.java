package com.huly.backend.domain.model.enums;

/**
 * Indicates whether a message only supplies preferences or also contains
 * conversation content that must be answered.
 */
public enum ChatPreferenceMessageType {
    PREFERENCE_ONLY,
    MIXED,
    UNRELATED
}
