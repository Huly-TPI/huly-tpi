-- Plan Huly (PREMIUM): otorgar 1000 monedas al activar el plan.
-- HandleWebhookUseCase ya credita coins_amount cuando es > 0; solo faltaba el valor en la DB.
UPDATE product
SET coins_amount = 1000
WHERE plan_code = 'PREMIUM';
