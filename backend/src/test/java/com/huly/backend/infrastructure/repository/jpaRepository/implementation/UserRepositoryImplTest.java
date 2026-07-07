package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@huly.com";
    private static final String NEW_EMAIL = "new@huly.com";
    private static final String ENCODED = "encoded";
    private static final Instant SINCE = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private AppUserRepository jpaRepository;
    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    @Test
    @DisplayName("Mapea la entidad a dominio al buscar por email cuando existe")
    void findByEmailShouldReturnMappedDomainWhenEntityExists() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, null));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenUserMatches(result, 1L, EMAIL);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por email cuando no existe")
    void findByEmailShouldReturnEmptyWhenEntityDoesNotExist() {
        givenUserByEmail("missing@huly.com", null);

        Optional<AppUser> result = findByEmail("missing@huly.com");

        thenEmpty(result);
    }

    @Test
    @DisplayName("Hidrata el nombre cuando el detalle de usuario lo tiene")
    void findByEmailShouldHydrateNameWhenUserDetailHasName() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, List.of(detail("Mili", null))));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenNameIs(result, "Mili");
    }

    @Test
    @DisplayName("Devuelve nombre nulo cuando la lista de detalles es nula")
    void findByEmailShouldReturnNullNameWhenUserDetailsIsNull() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, null));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenNameIsNull(result);
    }

    @Test
    @DisplayName("Devuelve nombre nulo cuando la lista de detalles está vacía")
    void findByEmailShouldReturnNullNameWhenUserDetailsIsEmpty() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, List.of()));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenNameIsNull(result);
    }

    @Test
    @DisplayName("Toma el primer nombre no nulo cuando hay varios detalles")
    void findByEmailShouldTakeFirstNonNullNameWhenMultipleDetailsExist() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, List.of(
                detail(null, null), detail("Mili", null), detail("Otro", null))));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenNameIs(result, "Mili");
    }

    @Test
    @DisplayName("Hidrata la fecha de nacimiento cuando el detalle de usuario la tiene")
    void findByEmailShouldHydrateBirthDateWhenUserDetailHasBirthDate() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, List.of(detail(null, LocalDate.of(1995, 5, 5)))));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenBirthDateIs(result, LocalDate.of(1995, 5, 5));
    }

    @Test
    @DisplayName("Devuelve fecha de nacimiento nula cuando la lista de detalles es nula")
    void findByEmailShouldReturnNullBirthDateWhenUserDetailsIsNull() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, null));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenBirthDateIsNull(result);
    }

    @Test
    @DisplayName("Devuelve fecha de nacimiento nula cuando la lista de detalles está vacía")
    void findByEmailShouldReturnNullBirthDateWhenUserDetailsIsEmpty() {
        givenUserByEmail(EMAIL, appUserEntity(1L, EMAIL, List.of()));

        Optional<AppUser> result = findByEmail(EMAIL);

        thenBirthDateIsNull(result);
    }

    @Test
    @DisplayName("Delega la verificación de existencia por email")
    void existsByEmailShouldDelegateToJpa() {
        givenEmailExists(EMAIL, true);

        boolean result = existsByEmail(EMAIL);

        thenTrue(result);
    }

    @Test
    @DisplayName("Mapea la entidad a dominio al buscar por id cuando existe")
    void findByIdShouldReturnMappedDomainWhenEntityExists() {
        givenUserById(USER_ID, appUserEntity(1L, EMAIL, null));

        Optional<AppUser> result = findById(USER_ID);

        thenUserPresentWithId(result, 1L);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por id cuando no existe")
    void findByIdShouldReturnEmptyWhenEntityDoesNotExist() {
        givenUserById(99L, null);

        Optional<AppUser> result = findById(99L);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea los campos y devuelve el dominio al guardar")
    void saveShouldMapEntityFieldsAndReturnMappedDomain() {
        givenSavedUser(appUserEntity(5L, NEW_EMAIL, null));

        AppUser result = save(domainUser(null, null));

        thenSavedUserMatches(result, 5L);
    }

    @Test
    @DisplayName("Crea el detalle de usuario cuando el nombre está presente")
    void saveShouldCreateUserDetailEntityWhenNameIsPresent() {
        AppUserEntity savedEntity = appUserEntity(5L, NEW_EMAIL, null);
        givenSavedUser(savedEntity);

        save(domainUser("Juan", LocalDate.of(2000, 1, 1)));

        thenUserDetailPersisted("Juan", LocalDate.of(2000, 1, 1), savedEntity);
    }

    @Test
    @DisplayName("No crea el detalle de usuario cuando el nombre es nulo")
    void saveShouldNotCreateUserDetailEntityWhenNameIsNull() {
        givenSavedUser(appUserEntity(5L, NEW_EMAIL, null));

        save(domainUser(null, null));

        thenUserDetailNeverPersisted();
    }

    @Test
    @DisplayName("Mapea los campos del dominio a la entidad antes de persistir")
    void saveShouldMapDomainFieldsToEntityBeforePersisting() {
        givenSavedUser(appUserEntity(1L, NEW_EMAIL, null));

        save(domainUser(null, null));

        thenPersistedEntityFieldsMatch();
    }

    @Test
    @DisplayName("Guarda el detalle del lead con nickname y acción de origen")
    void saveLeadDetailShouldSaveUserDetailWithNicknameAndSourceAction() {
        saveLeadDetail(10L, "hulyuser", SourceAction.LANDING);

        thenLeadDetailPersisted("hulyuser", SourceAction.LANDING, 10L);
    }

    @Test
    @DisplayName("Guarda el detalle del lead estableciendo la fecha de creación")
    void saveLeadDetailShouldSetCreatedAt() {
        saveLeadDetail(1L, "hulyuser", SourceAction.GOALS);

        thenLeadDetailHasCreatedAt();
    }

    @Test
    @DisplayName("Delega el incremento de monedas")
    void addCoinsShouldDelegateToJpa() {
        addCoins(USER_ID, 10);

        thenAddCoinsDelegated(USER_ID, 10);
    }

    @Test
    @DisplayName("Delega el débito de monedas y devuelve las filas afectadas")
    void debitCoinsShouldDelegateToJpaAndReturnRowsAffected() {
        givenDebitCoins(USER_ID, 10, 1);

        int result = debitCoins(USER_ID, 10);

        thenDebitDelegatedReturning(result, USER_ID, 10, 1);
    }

    @Test
    @DisplayName("Devuelve las monedas cuando existen")
    void getCoinsShouldReturnCoinsWhenFound() {
        givenCoins(USER_ID, 100);

        int result = getCoins(USER_ID);

        thenCoinsAre(result, 100);
    }

    @Test
    @DisplayName("Devuelve cero cuando no hay monedas")
    void getCoinsShouldReturnZeroWhenNotFound() {
        givenCoins(USER_ID, null);

        int result = getCoins(USER_ID);

        thenCoinsAre(result, 0);
    }

    @Test
    @DisplayName("Mapea la lista de dominios no administradores")
    void findAllNonAdminsShouldReturnMappedDomainList() {
        givenNonAdmins(appUserEntity(1L, EMAIL, null));

        List<AppUser> result = findAllNonAdmins();

        thenUsersHaveIds(result, 1L);
    }

    @Test
    @DisplayName("Delega la actualización del último login con el instante actual")
    void updateLastLoginShouldDelegateToJpaWithCurrentInstant() {
        updateLastLogin(5L);

        thenUpdateLastLoginDelegated(5L);
    }

    @Test
    @DisplayName("Mapea los dominios inactivos con nombre y fecha de nacimiento")
    void findUsersInactiveSinceShouldReturnMappedDomainsWithNameAndBirth() {
        givenInactiveUsers(appUserEntity(7L, "inactive@huly.com",
                List.of(detail("Test", LocalDate.of(2000, 1, 1)))));

        List<AppUser> result = findUsersInactiveSince(SINCE);

        thenInactiveUserMatches(result, 7L, "inactive@huly.com", "Test", LocalDate.of(2000, 1, 1));
    }

    @Test
    @DisplayName("Mapea nombre y fecha de nacimiento como nulos cuando el detalle no tiene valores")
    void findUsersInactiveSinceShouldMapNameAndBirthAsNullWhenUserDetailsHaveNoValues() {
        givenInactiveUsers(appUserEntity(8L, "sindatos@huly.com", List.of(detail(null, null))));

        List<AppUser> result = findUsersInactiveSince(SINCE);

        thenInactiveUserHasNullNameAndBirth(result);
    }

    @Test
    @DisplayName("Mapea el dominio al buscar por token de baja cuando existe")
    void findByUnsubscribeTokenShouldReturnMappedDomainWhenTokenExists() {
        UUID token = UUID.randomUUID();
        givenUserByToken(token, appUserEntityWithToken(3L, token));

        Optional<AppUser> result = findByUnsubscribeToken(token.toString());

        thenUserTokenMatches(result, 3L, token);
    }

    @Test
    @DisplayName("Devuelve vacío cuando el token de baja no es un UUID válido")
    void findByUnsubscribeTokenShouldReturnEmptyWhenTokenIsNotValidUuid() {
        Optional<AppUser> result = findByUnsubscribeToken("no-es-uuid");

        thenEmpty(result);
        thenTokenLookupNeverCalled();
    }

    @Test
    @DisplayName("Devuelve vacío cuando el token de baja no se encuentra")
    void findByUnsubscribeTokenShouldReturnEmptyWhenTokenNotFound() {
        UUID token = UUID.randomUUID();
        givenUserByToken(token, null);

        Optional<AppUser> result = findByUnsubscribeToken(token.toString());

        thenEmpty(result);
    }

    @Test
    @DisplayName("Delega la desactivación de emails de reenganche")
    void disableReengagementEmailsShouldDelegateToJpa() {
        disableReengagementEmails(5L);

        thenDisableReengagementDelegated(5L);
    }

    @Test
    @DisplayName("Delega la actualización de la contraseña")
    void updatePasswordShouldDelegateToJpa() {
        updatePassword(USER_ID, "nuevaEncoded");

        thenUpdatePasswordDelegated(USER_ID, "nuevaEncoded");
    }

    // --- arrange ---
    private void givenUserByEmail(String email, AppUserEntity entity) {
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenEmailExists(String email, boolean exists) {
        when(jpaRepository.existsByEmail(email)).thenReturn(exists);
    }

    private void givenUserById(Long id, AppUserEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenSavedUser(AppUserEntity entity) {
        when(jpaRepository.save(any(AppUserEntity.class))).thenReturn(entity);
    }

    private void givenCoins(Long userId, Integer coins) {
        when(jpaRepository.findCoinsById(userId)).thenReturn(Optional.ofNullable(coins));
    }

    private void givenNonAdmins(AppUserEntity... entities) {
        when(jpaRepository.findByRoleNot(UserRole.ADMIN)).thenReturn(List.of(entities));
    }

    private void givenInactiveUsers(AppUserEntity... entities) {
        when(jpaRepository.findByLastLoginAtBefore(any(Instant.class))).thenReturn(List.of(entities));
    }

    private void givenDebitCoins(Long userId, int amount, int rows) {
        when(jpaRepository.debitCoins(userId, amount)).thenReturn(rows);
    }

    private void givenUserByToken(UUID token, AppUserEntity entity) {
        when(jpaRepository.findByUnsubscribeToken(token)).thenReturn(Optional.ofNullable(entity));
    }

    private AppUserEntity appUserEntity(Long id, String email, List<UserDetailEntity> details) {
        return AppUserEntity.builder()
                .id(id).email(email).password(ENCODED)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .userDetails(details)
                .build();
    }

    private AppUserEntity appUserEntityWithToken(Long id, UUID token) {
        return AppUserEntity.builder()
                .id(id).email(EMAIL)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .unsubscribeToken(token)
                .build();
    }

    private UserDetailEntity detail(String name, LocalDate birth) {
        return UserDetailEntity.builder().name(name).birth(birth).build();
    }

    private AppUser domainUser(String name, LocalDate birth) {
        return AppUser.builder()
                .name(name).email(NEW_EMAIL).password(ENCODED)
                .birthDate(birth)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    // --- act ---
    private Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    private AppUser save(AppUser user) {
        return userRepository.save(user);
    }

    private void saveLeadDetail(Long userId, String nickname, SourceAction sourceAction) {
        userRepository.saveLeadDetail(userId, nickname, sourceAction);
    }

    private void addCoins(Long userId, int amount) {
        userRepository.addCoins(userId, amount);
    }

    private int debitCoins(Long userId, int amount) {
        return userRepository.debitCoins(userId, amount);
    }

    private int getCoins(Long userId) {
        return userRepository.getCoins(userId);
    }

    private List<AppUser> findAllNonAdmins() {
        return userRepository.findAllNonAdmins();
    }

    private void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId);
    }

    private List<AppUser> findUsersInactiveSince(Instant since) {
        return userRepository.findUsersInactiveSince(since);
    }

    private Optional<AppUser> findByUnsubscribeToken(String token) {
        return userRepository.findByUnsubscribeToken(token);
    }

    private void disableReengagementEmails(Long userId) {
        userRepository.disableReengagementEmails(userId);
    }

    private void updatePassword(Long userId, String encodedPassword) {
        userRepository.updatePassword(userId, encodedPassword);
    }

    // --- assert ---
    private void thenUserMatches(Optional<AppUser> result, Long id, String email) {
        assertThat(result).isPresent();
        AppUser user = result.get();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(ENCODED);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private void thenUserPresentWithId(Optional<AppUser> result, Long id) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    private void thenEmpty(Optional<AppUser> result) {
        assertThat(result).isEmpty();
    }

    private void thenNameIs(Optional<AppUser> result, String name) {
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
    }

    private void thenNameIsNull(Optional<AppUser> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isNull();
    }

    private void thenBirthDateIs(Optional<AppUser> result, LocalDate birth) {
        assertThat(result).isPresent();
        assertThat(result.get().getBirthDate()).isEqualTo(birth);
    }

    private void thenBirthDateIsNull(Optional<AppUser> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getBirthDate()).isNull();
    }

    private void thenTrue(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenSavedUserMatches(AppUser result, Long id) {
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private void thenUserDetailPersisted(String name, LocalDate birth, AppUserEntity expectedUser) {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(name);
        assertThat(captor.getValue().getBirth()).isEqualTo(birth);
        assertThat(captor.getValue().getAppUser()).isEqualTo(expectedUser);
    }

    private void thenUserDetailNeverPersisted() {
        verify(userDetailRepository, never()).save(any());
    }

    private void thenPersistedEntityFieldsMatch() {
        ArgumentCaptor<AppUserEntity> captor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(jpaRepository).save(captor.capture());
        AppUserEntity captured = captor.getValue();
        assertThat(captured.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(captured.getPassword()).isEqualTo(ENCODED);
        assertThat(captured.getRole()).isEqualTo(UserRole.USER);
        assertThat(captured.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private void thenLeadDetailPersisted(String nickname, SourceAction sourceAction, Long userId) {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        UserDetailEntity saved = captor.getValue();
        assertThat(saved.getNickname()).isEqualTo(nickname);
        assertThat(saved.getSourceAction()).isEqualTo(sourceAction);
        assertThat(saved.getAppUser().getId()).isEqualTo(userId);
    }

    private void thenLeadDetailHasCreatedAt() {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    private void thenAddCoinsDelegated(Long userId, int amount) {
        verify(jpaRepository).addCoins(userId, amount);
    }

    private void thenDebitDelegatedReturning(int result, Long userId, int amount, int expected) {
        assertThat(result).isEqualTo(expected);
        verify(jpaRepository).debitCoins(userId, amount);
    }

    private void thenCoinsAre(int result, int expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenUsersHaveIds(List<AppUser> result, Long... ids) {
        assertThat(result).extracting(AppUser::getId).containsExactly(ids);
    }

    private void thenUpdateLastLoginDelegated(Long userId) {
        verify(jpaRepository).updateLastLogin(eq(userId), any(Instant.class));
    }

    private void thenInactiveUserMatches(List<AppUser> result, Long id, String email, String name, LocalDate birth) {
        assertThat(result).hasSize(1);
        AppUser user = result.get(0);
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getBirthDate()).isEqualTo(birth);
    }

    private void thenInactiveUserHasNullNameAndBirth(List<AppUser> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isNull();
        assertThat(result.get(0).getBirthDate()).isNull();
    }

    private void thenUserTokenMatches(Optional<AppUser> result, Long id, UUID token) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getUnsubscribeToken()).isEqualTo(token.toString());
    }

    private void thenTokenLookupNeverCalled() {
        verify(jpaRepository, never()).findByUnsubscribeToken(any());
    }

    private void thenDisableReengagementDelegated(Long userId) {
        verify(jpaRepository).disableReengagementEmails(userId);
    }

    private void thenUpdatePasswordDelegated(Long userId, String encodedPassword) {
        verify(jpaRepository).updatePassword(userId, encodedPassword);
    }
}
