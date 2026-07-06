package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserGoalsEntity;
import com.huly.backend.infrastructure.repository.entity.UserPlantEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserGoalJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlantJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGoalRepositoryImplTest {

    private static final Long USER_ID = 10L;
    private static final Long OTHER_USER_ID = 5L;
    private static final Long MISSING_ID = 99L;
    private static final Long GOAL_ID = 1L;
    private static final Long ACTIVITY_ID = 2L;
    private static final Long PLANT_ID = 4L;
    private static final String IMAGE_URL = "/api/user-goals/images/photo.jpg";
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Pageable PAGEABLE = PageRequest.of(0, 5);

    @Mock
    private IUserGoalJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private IActivityJpaRepository activityJpaRepository;
    @Mock
    private IUserPlantJpaRepository userPlantJpaRepository;

    @InjectMocks
    private UserGoalRepositoryImpl repositoryImpl;

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir con save")
    void saveShouldMapDomainToEntityBeforePersisting() {
        givenReferencedUser(USER_ID);
        givenReferencedActivity(ACTIVITY_ID);
        givenSaved(savedEntity(GOAL_ID, USER_ID, ACTIVITY_ID));

        save(domainWithActivity());

        thenPersistedTitleStatusAndActivity();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio con save")
    void saveShouldMapPersistedEntityToDomain() {
        givenReferencedUser(USER_ID);
        givenReferencedActivity(ACTIVITY_ID);
        givenSaved(savedEntity(GOAL_ID, USER_ID, ACTIVITY_ID));

        UserGoal result = save(domainWithActivity());

        thenGoalMapped(result, GOAL_ID, USER_ID, ACTIVITY_ID);
    }

    @Test
    @DisplayName("No referencia actividad cuando el objetivo no tiene actividad")
    void saveShouldHandleNullActivityId() {
        givenReferencedUser(USER_ID);
        givenSaved(savedEntity(GOAL_ID, USER_ID, null));

        UserGoal result = save(domainWithoutActivity());

        thenActivityNeverReferenced();
        thenGoalActivityIdIsNull(result);
    }

    @Test
    @DisplayName("Referencia y mapea la planta cuando el objetivo tiene planta asociada")
    void saveShouldReferenceUserPlantWhenPlantIdPresent() {
        givenReferencedUser(USER_ID);
        givenReferencedPlant(PLANT_ID);
        givenSaved(savedEntityWithPlant(GOAL_ID, USER_ID, PLANT_ID));

        UserGoal result = save(domainWithPlant());

        thenPersistedReferencesPlant(PLANT_ID);
        thenGoalUserPlantIdIs(result, PLANT_ID);
    }

    @Test
    @DisplayName("Devuelve la página mapeada de objetivos por usuario y estado")
    void findByUserIdAndStatusShouldReturnMappedPage() {
        givenPageForUserAndStatus(OTHER_USER_ID, GoalStatus.PENDING, savedEntity(GOAL_ID, OTHER_USER_ID, null));

        Page<UserGoal> result = findByUserIdAndStatus(OTHER_USER_ID, GoalStatus.PENDING);

        thenPageMappedTo(result, OTHER_USER_ID, GoalStatus.PENDING);
    }

    @Test
    @DisplayName("Devuelve una página vacía cuando no hay objetivos por usuario y estado")
    void findByUserIdAndStatusShouldReturnEmptyPageWhenNoneFound() {
        givenEmptyPageForUserAndStatus(MISSING_ID, GoalStatus.COMPLETED);

        Page<UserGoal> result = findByUserIdAndStatus(MISSING_ID, GoalStatus.COMPLETED);

        thenPageEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el objetivo mapeado por id cuando existe")
    void findByIdShouldReturnMappedGoalWhenExists() {
        givenGoalById(GOAL_ID, savedEntity(GOAL_ID, USER_ID, ACTIVITY_ID));

        Optional<UserGoal> result = findById(GOAL_ID);

        thenGoalPresentWithActivity(result, GOAL_ID, ACTIVITY_ID);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por id cuando no existe")
    void findByIdShouldReturnEmptyWhenNotFound() {
        givenGoalMissing(MISSING_ID);

        Optional<UserGoal> result = findById(MISSING_ID);

        thenAbsent(result);
    }

    @Test
    @DisplayName("Devuelve true cuando existe el objetivo por id")
    void existsByIdShouldReturnTrueWhenFound() {
        givenExists(GOAL_ID, true);

        boolean result = existsById(GOAL_ID);

        thenExists(result, true);
    }

    @Test
    @DisplayName("Devuelve false cuando no existe el objetivo por id")
    void existsByIdShouldReturnFalseWhenNotFound() {
        givenExists(MISSING_ID, false);

        boolean result = existsById(MISSING_ID);

        thenExists(result, false);
    }

    @Test
    @DisplayName("Delega la eliminación por id al repositorio JPA")
    void deleteByIdShouldDelegateToJpa() {
        deleteById(GOAL_ID);

        thenDeleted(GOAL_ID);
    }

    @Test
    @DisplayName("Mapea la url de imagen y las monedas al guardar")
    void saveShouldMapImageUrlAndCoinFields() {
        givenReferencedUser(USER_ID);
        givenSaved(savedEntityWithImage(GOAL_ID, USER_ID, IMAGE_URL, 15, 30));

        UserGoal result = save(domainWithImage());

        thenPersistedImageAndCoins();
        thenGoalImageAndCoins(result);
    }

    @Test
    @DisplayName("Usa los valores de monedas por defecto cuando el dominio los trae nulos")
    void saveShouldUseDefaultCoinValuesWhenDomainHasNullCoinFields() {
        givenReferencedUser(USER_ID);
        givenSaved(savedEntity(GOAL_ID, USER_ID, null));

        save(domainWithoutCoins());

        thenPersistedDefaultCoins();
    }

    @Test
    @DisplayName("Mapea los objetivos completados de una planta")
    void findCompletedByPlantIdShouldReturnMappedGoals() {
        givenCompletedGoalsByPlant(PLANT_ID, completedGoal(1L), completedGoal(2L));

        List<UserGoal> result = findCompleted(PLANT_ID);

        thenCompletedGoalIdsAre(result, 1L, 2L);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando la planta no tiene objetivos completados")
    void findCompletedByPlantIdShouldReturnEmptyWhenNone() {
        givenCompletedGoalsByPlant(PLANT_ID);

        List<UserGoal> result = findCompleted(PLANT_ID);

        thenGoalsEmpty(result);
    }

    // --- arrange ---
    private void givenReferencedUser(Long userId) {
        when(appUserRepository.getReferenceById(userId)).thenReturn(userEntity(userId));
    }

    private void givenReferencedActivity(Long activityId) {
        when(activityJpaRepository.getReferenceById(activityId)).thenReturn(activityEntity(activityId));
    }

    private void givenReferencedPlant(Long plantId) {
        when(userPlantJpaRepository.getReferenceById(plantId)).thenReturn(plantEntity(plantId));
    }

    private void givenSaved(UserGoalsEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenPageForUserAndStatus(Long userId, GoalStatus status, UserGoalsEntity entity) {
        when(jpaRepository.findByAppUser_IdAndStatus(userId, status, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(entity)));
    }

    private void givenEmptyPageForUserAndStatus(Long userId, GoalStatus status) {
        when(jpaRepository.findByAppUser_IdAndStatus(userId, status, PAGEABLE))
                .thenReturn(Page.empty(PAGEABLE));
    }

    private void givenGoalById(Long id, UserGoalsEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenGoalMissing(Long id) {
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenExists(Long id, boolean exists) {
        when(jpaRepository.existsById(id)).thenReturn(exists);
    }

    private void givenCompletedGoalsByPlant(Long plantId, UserGoalsEntity... entities) {
        when(jpaRepository.findByUserPlant_IdAndStatus(plantId, GoalStatus.COMPLETED))
                .thenReturn(List.of(entities));
    }

    private UserGoal domainWithActivity() {
        return UserGoal.builder()
                .userId(USER_ID).title("T").description("D").activityId(ACTIVITY_ID)
                .status(GoalStatus.PENDING).createdAt(CREATED_AT).build();
    }

    private UserGoal domainWithoutActivity() {
        return UserGoal.builder()
                .userId(USER_ID).title("T").activityId(null)
                .status(GoalStatus.PENDING).createdAt(CREATED_AT).build();
    }

    private UserGoal domainWithPlant() {
        return UserGoal.builder()
                .userId(USER_ID).title("T").activityId(null).userPlantId(PLANT_ID)
                .status(GoalStatus.PENDING).createdAt(CREATED_AT).build();
    }

    private UserGoal domainWithImage() {
        return UserGoal.builder()
                .userId(USER_ID).title("T").status(GoalStatus.PENDING).createdAt(CREATED_AT)
                .imageUrl(IMAGE_URL).coinsReward(15).coinsRewardWithImage(30).build();
    }

    private UserGoal domainWithoutCoins() {
        return UserGoal.builder()
                .userId(USER_ID).title("T").status(GoalStatus.PENDING).createdAt(CREATED_AT).build();
    }

    private UserGoalsEntity savedEntity(Long id, Long userId, Long activityId) {
        ActivityEntity activity = activityId != null ? activityEntity(activityId) : null;
        return UserGoalsEntity.builder()
                .id(id).appUser(userEntity(userId)).title("T").description("D")
                .status(GoalStatus.PENDING).createdAt(CREATED_AT).activity(activity)
                .coinsReward(10).coinsRewardWithImage(25)
                .build();
    }

    private UserGoalsEntity savedEntityWithPlant(Long id, Long userId, Long plantId) {
        return UserGoalsEntity.builder()
                .id(id).appUser(userEntity(userId)).title("T").description("D")
                .status(GoalStatus.PENDING).createdAt(CREATED_AT).userPlant(plantEntity(plantId))
                .coinsReward(10).coinsRewardWithImage(25)
                .build();
    }

    private UserGoalsEntity savedEntityWithImage(Long id, Long userId, String imageUrl,
                                                 int coinsReward, int coinsRewardWithImage) {
        return UserGoalsEntity.builder()
                .id(id).appUser(userEntity(userId)).title("T").description("D")
                .status(GoalStatus.PENDING).createdAt(CREATED_AT)
                .imageUrl(imageUrl).coinsReward(coinsReward).coinsRewardWithImage(coinsRewardWithImage)
                .build();
    }

    private UserGoalsEntity completedGoal(Long id) {
        return UserGoalsEntity.builder()
                .id(id).appUser(userEntity(USER_ID)).title("T").description("D")
                .status(GoalStatus.COMPLETED).createdAt(CREATED_AT)
                .coinsReward(10).coinsRewardWithImage(25)
                .build();
    }

    private AppUserEntity userEntity(Long id) {
        AppUserEntity e = new AppUserEntity();
        e.setId(id);
        return e;
    }

    private ActivityEntity activityEntity(Long id) {
        ActivityEntity e = new ActivityEntity();
        e.setId(id);
        return e;
    }

    private UserPlantEntity plantEntity(Long id) {
        return UserPlantEntity.builder().id(id).build();
    }

    // --- act ---
    private UserGoal save(UserGoal domain) {
        return repositoryImpl.save(domain);
    }

    private Page<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status) {
        return repositoryImpl.findByUserIdAndStatus(userId, status, PAGEABLE);
    }

    private Optional<UserGoal> findById(Long id) {
        return repositoryImpl.findById(id);
    }

    private boolean existsById(Long id) {
        return repositoryImpl.existsById(id);
    }

    private void deleteById(Long id) {
        repositoryImpl.deleteById(id);
    }

    private List<UserGoal> findCompleted(Long plantId) {
        return repositoryImpl.findCompletedByPlantId(plantId);
    }

    // --- assert ---
    private void thenPersistedTitleStatusAndActivity() {
        UserGoalsEntity persisted = capturePersisted();
        assertThat(persisted.getTitle()).isEqualTo("T");
        assertThat(persisted.getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(persisted.getActivity()).isNotNull();
    }

    private void thenPersistedReferencesPlant(Long plantId) {
        UserGoalsEntity persisted = capturePersisted();
        assertThat(persisted.getUserPlant()).isNotNull();
        assertThat(persisted.getUserPlant().getId()).isEqualTo(plantId);
    }

    private void thenPersistedImageAndCoins() {
        UserGoalsEntity persisted = capturePersisted();
        assertThat(persisted.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(persisted.getCoinsReward()).isEqualTo(15);
        assertThat(persisted.getCoinsRewardWithImage()).isEqualTo(30);
    }

    private void thenPersistedDefaultCoins() {
        UserGoalsEntity persisted = capturePersisted();
        assertThat(persisted.getCoinsReward()).isEqualTo(10);
        assertThat(persisted.getCoinsRewardWithImage()).isEqualTo(25);
    }

    private UserGoalsEntity capturePersisted() {
        ArgumentCaptor<UserGoalsEntity> captor = ArgumentCaptor.forClass(UserGoalsEntity.class);
        verify(jpaRepository).save(captor.capture());
        return captor.getValue();
    }

    private void thenGoalMapped(UserGoal result, Long id, Long userId, Long activityId) {
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityId()).isEqualTo(activityId);
    }

    private void thenActivityNeverReferenced() {
        verify(activityJpaRepository, never()).getReferenceById(any());
    }

    private void thenGoalActivityIdIsNull(UserGoal result) {
        assertThat(result.getActivityId()).isNull();
    }

    private void thenGoalUserPlantIdIs(UserGoal result, Long plantId) {
        assertThat(result.getUserPlantId()).isEqualTo(plantId);
    }

    private void thenGoalImageAndCoins(UserGoal result) {
        assertThat(result.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(result.getCoinsReward()).isEqualTo(15);
        assertThat(result.getCoinsRewardWithImage()).isEqualTo(30);
    }

    private void thenPageMappedTo(Page<UserGoal> result, Long userId, GoalStatus status) {
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
    }

    private void thenPageEmpty(Page<UserGoal> result) {
        assertThat(result.getContent()).isEmpty();
    }

    private void thenGoalPresentWithActivity(Optional<UserGoal> result, Long id, Long activityId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getActivityId()).isEqualTo(activityId);
    }

    private void thenAbsent(Optional<UserGoal> result) {
        assertThat(result).isEmpty();
    }

    private void thenExists(boolean result, boolean expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenDeleted(Long id) {
        verify(jpaRepository).deleteById(id);
    }

    private void thenCompletedGoalIdsAre(List<UserGoal> result, Long... ids) {
        assertThat(result).extracting(UserGoal::getId).containsExactly(ids);
    }

    private void thenGoalsEmpty(List<UserGoal> result) {
        assertThat(result).isEmpty();
    }
}
