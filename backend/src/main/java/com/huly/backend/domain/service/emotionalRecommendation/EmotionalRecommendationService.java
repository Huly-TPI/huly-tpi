package com.huly.backend.domain.service.emotionalRecommendation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates deterministic activity rankings from VAD state and recommendation context.
 */
@Service
public class EmotionalRecommendationService {

    private final EmotionalRecommendationPolicy policy;

    public EmotionalRecommendationService(EmotionalRecommendationPolicy policy) {
        this.policy = policy;
    }

    public EmotionalRecommendationResult recommend(
            EmotionalRecommendation query,
            List<Activity> activities,
            List<EmotionalEvent> userHistory
    ) {
        if (query == null || activities == null || activities.isEmpty()) 
            return new EmotionalRecommendationResult(List.of(), false);
        
        boolean fallbackUsed = !hasRangeMatch(query.vad(), activities);
        List<EmotionalRecommendationItem> recommendations = rankAndSortActivities(query, activities, userHistory, fallbackUsed);
        return new EmotionalRecommendationResult(recommendations, fallbackUsed);
    }

    private boolean hasRangeMatch(Vad vad, List<Activity> activities) {
        return activities.stream().anyMatch(activity -> policy.isInRange(vad, activity));
    }

    private List<EmotionalRecommendationItem> rankAndSortActivities(
            EmotionalRecommendation query,
            List<Activity> activities,
            List<EmotionalEvent> userHistory,
            boolean fallbackUsed
    ) {
        Map<ActivityType, PreferenceTracker> userPreferences = buildUserPreferences(userHistory, activities);
        return activities.stream()
                .map(activity -> toRecommendation(query, activity, fallbackUsed, userPreferences, userHistory))
                .sorted(Comparator.comparingDouble(EmotionalRecommendationItem::score).reversed())
                .toList();
    }

    private EmotionalRecommendationItem toRecommendation(
            EmotionalRecommendation query,
            Activity activity,
            boolean fallbackUsed,
            Map<ActivityType, PreferenceTracker> userPreferences,
            List<EmotionalEvent> userHistory
    ) {
        double baseScore = policy.calculateBaseScore(query, activity, fallbackUsed);
        double adjustedScore = baseScore + preferenceAdjustment(activity.getType(), userPreferences);
        double rejectionAdjustment = rejectionAdjustment(activity.getId(), userHistory);

        return new EmotionalRecommendationItem(
                activity.getId(),
                activity.getType(),
                activity.getTitle(),
                activity.getDescription(),
                roundScore(adjustedScore - rejectionAdjustment),
                reason(query, activity, fallbackUsed),
                activity.getRoutePath()
        );
    }

    private double rejectionAdjustment(Long activityId, List<EmotionalEvent> userHistory) {
        long ignoredCount = countIgnoredEventsForActivity(activityId, userHistory);
        long unhelpfulCount = countUnhelpfulEventsForActivity(activityId, userHistory);
        return (ignoredCount * 0.25) + (unhelpfulCount * 0.25);
    }

    private long countIgnoredEventsForActivity(Long activityId, List<EmotionalEvent> userHistory) {
        if (userHistory == null || activityId == null)
            return 0L;

        return userHistory.stream()
                .filter(e -> e != null
                        && e.getRecommendationDecision() == RecommendationDecision.IGNORED
                        && activityId.equals(e.getRecommendedActivityId()))
                .count();
    }

    private long countUnhelpfulEventsForActivity(Long activityId, List<EmotionalEvent> userHistory) {
        if (userHistory == null || activityId == null)
            return 0L;

        long count = 0;
        for (int index = 0; index < userHistory.size(); index++) {
            EmotionalEvent event = userHistory.get(index);
            if (event != null && isActivityCompleted(event, activityId)
                    && isUnhelpfulInteraction(event, index, userHistory)) {
                count++;
            }
        }
        return count;
    }

    private boolean isActivityCompleted(EmotionalEvent event, Long activityId) {
        RecommendationDecision decision = event.getRecommendationDecision();
        return (decision == RecommendationDecision.ACCEPTED && activityId.equals(event.getRecommendedActivityId()))
                || (decision == RecommendationDecision.CHOSE_OTHER && activityId.equals(event.getChosenActivityId()));
    }

    private boolean isUnhelpfulInteraction(EmotionalEvent event, int index, List<EmotionalEvent> userHistory) {
        boolean hasLowFeedback = event.getFeedbackScore() != null && event.getFeedbackScore() <= 2;
        return hasLowFeedback || hasNoEmotionalImprovement(event, index, userHistory);
    }

    private boolean hasNoEmotionalImprovement(EmotionalEvent event, int index, List<EmotionalEvent> userHistory) {
        if (index <= 0)
            return false;

        EmotionalEvent nextEvent = userHistory.get(index - 1);
        if (nextEvent == null || nextEvent.getValence() == null || event.getValence() == null)
            return false;

        double valenceDelta = nextEvent.getValence() - event.getValence();
        return valenceDelta <= 0.0;
    }

    private double preferenceAdjustment(ActivityType type, Map<ActivityType, PreferenceTracker> userPreferences) {
        if (type == null || userPreferences == null || userPreferences.isEmpty()) 
            return 0.0;
        
        PreferenceTracker tracker = userPreferences.get(type);
        if (tracker == null) 
            return 0.0;
        
        return clamp(tracker.score() * EmotionalRecommendationPolicy.MAX_TREND_ADJUSTMENT,
                -EmotionalRecommendationPolicy.MAX_TREND_ADJUSTMENT,
                EmotionalRecommendationPolicy.MAX_TREND_ADJUSTMENT);
    }

    private String reason(EmotionalRecommendation query, Activity activity, boolean fallbackUsed) {
        return policy.reason(query, activity, fallbackUsed);
    }

    private double roundScore(double score) {
        return Math.round(Math.min(1.0, Math.max(0.0, score)) * 100.0) / 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }   

    private Map<ActivityType, PreferenceTracker> buildUserPreferences(
            List<EmotionalEvent> userHistory,
            List<Activity> activities
    ) {
        if (userHistory == null || userHistory.isEmpty()) {
            return Map.of();
        }

        Map<Long, ActivityType> activityTypesById = activities == null ? Map.of() : activities.stream()
                .filter(activity -> activity.getId() != null && activity.getType() != null)
                .collect(Collectors.toMap(Activity::getId, Activity::getType));

        Map<ActivityType, PreferenceTracker> userPreferences = new HashMap<>();
        for (int index = 0; index < userHistory.size(); index++) {
            EmotionalEvent event = userHistory.get(index);
            if (event == null) 
                continue;

            double recentFactor = recentFactor(index);
            processEventPreferences(event, index, userHistory, activityTypesById, userPreferences, recentFactor);
        }
        return userPreferences;
    }

    private void processEventPreferences(
            EmotionalEvent event,
            int index,
            List<EmotionalEvent> userHistory,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double recentFactor
    ) {
        RecommendationDecision decision = event.getRecommendationDecision();
        if (decision == null) {
            handleFeedbackOnlyEvent(event, activityTypesById, userPreferences, recentFactor);
            return;
        }

        switch (decision) {
            case IGNORED -> handleIgnoredRecommendation(event, activityTypesById, userPreferences, recentFactor);
            case ACCEPTED -> handleAcceptedRecommendation(event, index, userHistory, activityTypesById, userPreferences, recentFactor);
            case CHOSE_OTHER -> handleChoseOtherRecommendation(event, index, userHistory, activityTypesById, userPreferences, recentFactor);
        }
    }

    private void handleFeedbackOnlyEvent(
            EmotionalEvent event,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double recentFactor
    ) {
        if (event.getFeedbackScore() != null) {
            ActivityType preferredType = preferredActivityType(event, activityTypesById);
            if (preferredType != null) {
                double netWeight = feedbackWeight(event);
                recordInteraction(preferredType, userPreferences, netWeight, recentFactor);
            }
        }
    }

    private void handleIgnoredRecommendation(
            EmotionalEvent event,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double recentFactor
    ) {
        ActivityType recommendedType = activityTypesById.get(event.getRecommendedActivityId());
        if (recommendedType != null) {
            recordInteraction(recommendedType, userPreferences, EmotionalRecommendationPolicy.IGNORED_SIGNAL, recentFactor);
        }
    }

    private void handleAcceptedRecommendation(
            EmotionalEvent event,
            int index,
            List<EmotionalEvent> userHistory,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double recentFactor
    ) {
        ActivityType recommendedType = activityTypesById.get(event.getRecommendedActivityId());
        if (recommendedType != null) {
            double netWeight = EmotionalRecommendationPolicy.ACCEPTED_SIGNAL;
            netWeight += emotionalImprovementWeight(event, index, userHistory);
            netWeight += feedbackWeight(event);
            recordInteraction(recommendedType, userPreferences, netWeight, recentFactor);
        }
    }

    private void handleChoseOtherRecommendation(
            EmotionalEvent event,
            int index,
            List<EmotionalEvent> userHistory,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double recentFactor
    ) {
        ActivityType recommendedType = activityTypesById.get(event.getRecommendedActivityId());
        if (recommendedType != null) {
            recordInteraction(recommendedType, userPreferences, EmotionalRecommendationPolicy.CHOSE_OTHER_RECOMMENDED_SIGNAL, recentFactor);
        }

        ActivityType chosenType = activityTypesById.get(event.getChosenActivityId());
        if (chosenType != null) {
            double netWeight = EmotionalRecommendationPolicy.CHOSE_OTHER_CHOSEN_SIGNAL;
            netWeight += emotionalImprovementWeight(event, index, userHistory);
            netWeight += feedbackWeight(event);
            recordInteraction(chosenType, userPreferences, netWeight, recentFactor);
        }
    }

    private ActivityType preferredActivityType(EmotionalEvent event, Map<Long, ActivityType> activityTypesById) {
        if (event.getChosenActivityId() != null) {
            return activityTypesById.get(event.getChosenActivityId());
        }
        return activityTypesById.get(event.getRecommendedActivityId());
    }

    private double emotionalImprovementWeight(EmotionalEvent event, int index, List<EmotionalEvent> userHistory) {
        if (index > 0) {
            EmotionalEvent nextEvent = userHistory.get(index - 1);
            if (nextEvent != null && nextEvent.getValence() != null && event.getValence() != null) {
                double delta = nextEvent.getValence() - event.getValence();
                return delta > 0.0 ? delta * 0.5 : delta * 0.2;
            }
        }
        return 0.0;
    }

    private double feedbackWeight(EmotionalEvent event) {
        if (event.getFeedbackScore() != null) {
            Double scoreSignal = EmotionalRecommendationPolicy.FEEDBACK_SIGNALS.get(event.getFeedbackScore());
            return scoreSignal != null ? scoreSignal : 0.0;
        }
        return 0.0;
    }

    private double recentFactor(int index) {
        return Math.max(EmotionalRecommendationPolicy.MIN_RECENCY_WEIGHT, 1.0 - (index * EmotionalRecommendationPolicy.RECENT_EVENT_DECAY));
    }

    private void recordInteraction(
            ActivityType type,
            Map<ActivityType, PreferenceTracker> userPreferences,
            double weight,
            double recentFactor
    ) {
        userPreferences.computeIfAbsent(type, key -> new PreferenceTracker()).addInteraction(weight, recentFactor);
    }

    private static class PreferenceTracker {
        private double weightSum;
        private double factorSum;

        void addInteraction(double preferenceWeight, double recentFactor) {
            weightSum += preferenceWeight * recentFactor;
            factorSum += recentFactor;
        }

        double score() {
            return factorSum == 0.0 ? 0.0 : weightSum / factorSum;
        }
    }
}
