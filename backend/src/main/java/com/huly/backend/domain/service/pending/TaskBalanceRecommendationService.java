package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.pending.PendingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskBalanceRecommendationService {

    public static final double DAILY_CAPACITY_BUDGET = 1.6;
    private static final int MAX_HIGH_PER_DAY = 1;

    public TaskBalanceRecommendationResult recommend(List<PendingTask> pendingTasks, double dailyCapacityBudget, LocalDate today) {
        if (pendingTasks == null || pendingTasks.size() < 2) {
            return new TaskBalanceRecommendationResult(List.of(), 0.0, dailyCapacityBudget);
        }

        List<PendingTask> eligibleTasks = capHighBucketByUrgency(pendingTasks, MAX_HIGH_PER_DAY, today);

        List<PendingTask> sortedAscending = eligibleTasks.stream()
                .sorted(TaskBalanceRecommendationService::compareTasks)
                .toList();

        Selection selection = new Selection(dailyCapacityBudget);

        sortedAscending.stream()
                .filter(task -> task.getMentalLoadBucket() == MentalLoadBucket.LOW)
                .findFirst()
                .ifPresent(selection::tryAdd);

        List<PendingTask> remaining = sortedAscending.stream()
                .filter(task -> !selection.contains(task.getId()))
                .toList();
        for (PendingTask task : interleave(remaining)) {
            selection.tryAdd(task);
        }

        if (selection.isEmpty()) {
            selection.forceAdd(sortedAscending.get(0));
        }

        List<Long> recommendedIds = selection.selectedIds().stream().sorted().toList();
        return new TaskBalanceRecommendationResult(recommendedIds, selection.roundedUsedBudget(), dailyCapacityBudget);
    }

    private static List<PendingTask> interleave(List<PendingTask> ascending) {
        List<PendingTask> descending = new ArrayList<>(ascending);
        Collections.reverse(descending);
        List<PendingTask> zigzag = new ArrayList<>(ascending.size());
        Set<Long> added = new LinkedHashSet<>();
        int i = 0;
        int j = 0;
        boolean takeAscending = true;
        while (added.size() < ascending.size()) {
            if (takeAscending) {
                i = advancePast(ascending, added, i);
                if (i < ascending.size()) {
                    zigzag.add(ascending.get(i));
                    added.add(ascending.get(i).getId());
                    i++;
                }
            } else {
                j = advancePast(descending, added, j);
                if (j < descending.size()) {
                    zigzag.add(descending.get(j));
                    added.add(descending.get(j).getId());
                    j++;
                }
            }
            takeAscending = !takeAscending;
        }
        return zigzag;
    }

    private static List<PendingTask> capHighBucketByUrgency(List<PendingTask> pendingTasks, int maxHighPerDay, LocalDate today) {
        List<PendingTask> highTasks = pendingTasks.stream()
                .filter(task -> task.getMentalLoadBucket() == MentalLoadBucket.HIGH)
                .toList();
        if (highTasks.size() <= maxHighPerDay) {
            return pendingTasks;
        }

        List<PendingTask> dueToday = highTasks.stream()
                .filter(task -> today != null && today.equals(task.getDueDate()))
                .toList();

        Set<Long> eligibleIds;
        if (dueToday.size() > 1) {
            log.info("daily_recommendation_high_slot_today_exception dueToday={}", describeHighTasks(dueToday));
            eligibleIds = dueToday.stream().map(PendingTask::getId).collect(Collectors.toSet());
        } else {
            List<PendingTask> sortedByUrgency = highTasks.stream()
                    .sorted(TaskBalanceRecommendationService::compareHighUrgency)
                    .toList();
            List<PendingTask> winners = sortedByUrgency.subList(0, maxHighPerDay);
            List<PendingTask> losers = sortedByUrgency.subList(maxHighPerDay, sortedByUrgency.size());
            log.info("daily_recommendation_high_slot_contention winners={} losers={}",
                    describeHighTasks(winners), describeHighTasks(losers));
            eligibleIds = winners.stream().map(PendingTask::getId).collect(Collectors.toSet());
        }

        Set<Long> excludedIds = highTasks.stream()
                .map(PendingTask::getId)
                .filter(id -> !eligibleIds.contains(id))
                .collect(Collectors.toSet());
        return pendingTasks.stream().filter(task -> !excludedIds.contains(task.getId())).toList();
    }

    private static String describeHighTasks(List<PendingTask> tasks) {
        return tasks.stream()
                .map(task -> task.getId() + "(dueDate=" + task.getDueDate() + ",score=" + scoreOf(task) + ")")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static int compareHighUrgency(PendingTask a, PendingTask b) {
        int byDueDate = compareDueDate(a.getDueDate(), b.getDueDate());
        if (byDueDate != 0) {
            return byDueDate;
        }
        int byScore = Double.compare(scoreOf(b), scoreOf(a));
        if (byScore != 0) {
            return byScore;
        }
        return Long.compare(a.getId(), b.getId());
    }

    private static int advancePast(List<PendingTask> list, Set<Long> added, int index) {
        while (index < list.size() && added.contains(list.get(index).getId())) {
            index++;
        }
        return index;
    }

    private static int compareTasks(PendingTask a, PendingTask b) {
        int byScore = Double.compare(scoreOf(a), scoreOf(b));
        if (byScore != 0) {
            return byScore;
        }
        int byDueDate = compareDueDate(a.getDueDate(), b.getDueDate());
        if (byDueDate != 0) {
            return byDueDate;
        }
        return Long.compare(a.getId(), b.getId());
    }

    private static int compareDueDate(LocalDate a, LocalDate b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    private static double scoreOf(PendingTask task) {
        return task.getMentalLoadScore() == null ? 0.0 : task.getMentalLoadScore();
    }

    private static final class Selection {
        private final double budget;
        private final Set<Long> selectedIds = new LinkedHashSet<>();
        private double usedBudget = 0.0;

        Selection(double budget) {
            this.budget = budget;
        }

        boolean contains(Long id) {
            return selectedIds.contains(id);
        }

        boolean isEmpty() {
            return selectedIds.isEmpty();
        }

        void tryAdd(PendingTask task) {
            if (selectedIds.contains(task.getId())) {
                return;
            }
            double taskScore = scoreOf(task);
            double projected = usedBudget + taskScore;
            if (projected > budget) {
                return;
            }
            selectedIds.add(task.getId());
            usedBudget = projected;
        }

        void forceAdd(PendingTask task) {
            selectedIds.add(task.getId());
            usedBudget += scoreOf(task);
        }

        Set<Long> selectedIds() {
            return selectedIds;
        }

        double roundedUsedBudget() {
            return Math.round(usedBudget * 100.0) / 100.0;
        }
    }
}
