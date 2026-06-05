package com.huly.backend.presentation.dto.onboarding;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GenerateOptionsRequest (
   
    @Min(2) @Max(3) @NotNull Integer step,
    @NotBlank String previousAnswer
    
) {

}
