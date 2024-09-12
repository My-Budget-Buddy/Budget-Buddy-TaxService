package com.skillstorm.taxservice.models;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    public void testAllArgsConstructor() {
        // Given
        int id = 1;
        String testState = "TestState";
        String testStateCode = "TestStateCode";

        // When
        State state = new State(id, testState, testStateCode);

        // Then
        assertThat(state.getId()).isEqualTo(id);
        assertThat(state.getStateName()).isEqualTo(testState);
        assertThat(state.getStateCode()).isEqualTo(testStateCode);
    }
}