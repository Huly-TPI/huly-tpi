package com.huly.backend.domain.model.user;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserEmotionalState {
   private Long id;
   private Long userId;
   private double valence;
   private double arousal;
   private double dominance;
   private double intensity;
   private String source;
   private Instant timestamp;  
}
