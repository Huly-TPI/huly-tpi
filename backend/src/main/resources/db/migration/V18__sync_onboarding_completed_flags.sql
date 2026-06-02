UPDATE user_detail
SET on_boarding_completed = TRUE
WHERE on_boarding_completed IS DISTINCT FROM TRUE
  AND profile_on_boarding_completed = TRUE;
