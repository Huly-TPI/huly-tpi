package com.huly.backend.infrastructure.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.huly.backend.domain.port.EmailPort;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailPortJavaMailImpl implements EmailPort {

  private final JavaMailSender mailSender;

  @Override
  public void sendWelcomeLead(String to, String nickname) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setFrom("hulycomunicaciones@gmail.com", "Huly");
      helper.setSubject("¡Bienvenido/a a Huly, " + nickname + "!");
      helper.setText(buildWelcomeHtml(nickname), true);
      helper.addInline("logo", new ClassPathResource("email/logo.png"));
      mailSender.send(message);
      log.info("[MAIL] Email de bienvenida enviado → to={}", to);
    } catch (Exception e) {
      log.error("[MAIL] Error enviando email a {} — {}", to, e.getMessage());
    }
  }

  private String buildWelcomeHtml(String nickname) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
        </head>
        <body style="margin:0;padding:0;background-color:#f0eeff;font-family:Arial,Helvetica,sans-serif;">

          <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                 style="background-color:#f0eeff;padding:40px 16px;">
            <tr>
              <td align="center">

                <!-- Card -->
                <table width="600" cellpadding="0" cellspacing="0" border="0"
                       style="max-width:600px;width:100%%;background-color:#ffffff;
                              border-radius:20px;overflow:hidden;
                              box-shadow:0 8px 32px rgba(108,71,255,0.14);">

                  <!-- Header -->
                  <tr>
                    <td align="center"
                        style="background:linear-gradient(135deg,#5c34ef 0%%,#9b7aff 100%%);
                               padding:36px 40px 28px;">
                      <img src="cid:logo" alt="Huly" width="150"
                           style="display:block;max-width:150px;height:auto;"/>
                    </td>
                  </tr>

                  <!-- Greeting band -->
                  <tr>
                    <td style="background:#6c47ff;padding:14px 48px;">
                      <p style="margin:0;font-size:13px;color:#ddd4ff;letter-spacing:1.5px;
                                 text-transform:uppercase;font-weight:600;">
                        Bienvenido/a a la comunidad
                      </p>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:40px 48px 32px;">
                      <h1 style="margin:0 0 16px;font-size:28px;color:#1a1040;font-weight:700;
                                  line-height:1.3;">
                        ¡Hola, %s!
                      </h1>
                      <p style="margin:0 0 16px;font-size:16px;color:#4a4a6a;line-height:1.7;">
                        Gracias por unirte a <strong style="color:#6c47ff;">Huly</strong>.
                        Estamos muy contentos de tenerte con nosotros.
                      </p>
                      <p style="margin:0 0 36px;font-size:16px;color:#4a4a6a;line-height:1.7;">
                        Pronto tendrás acceso a todas las herramientas de bienestar
                        que preparamos especialmente para vos.
                      </p>


                              <!-- CTA -->
                              <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td align="center"
                                      style="background-color:#5c34ef;border-radius:10px;">
                                    <a href="https://huly-tpi-frontend.onrender.com"
                                       style="display:inline-block;padding:14px 36px;
                                              color:#ffffff;text-decoration:none;
                                              font-size:15px;font-weight:700;
                                              letter-spacing:0.5px;">
                                      Explorar Huly
                                    </a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                  <!-- Divider -->
                  <tr>
                    <td style="padding:0 48px;">
                      <div style="border-top:1px solid #eeebff;"></div>
                    </td>
                  </tr>
                          <!-- Footer / Firma -->
                          <tr>
                            <td style="padding:28px 48px 36px;text-align:center;">
                              <p style="margin:0 0 2px;font-size:13px;color:#9090b0;">
                                Con cariño,
                              </p>
                              <p style="margin:0 0 18px;font-size:16px;font-weight:700;color:#6c47ff;">
                                El equipo de Huly
                              </p>
                              <p style="margin:0 0 6px;font-size:12px;color:#ababc8;">
                                hulycomunicaciones@gmail.com
                              </p>
                              <p style="margin:0 0 18px;font-size:12px;color:#ababc8;">
                                https://huly-tpi-frontend.onrender.com/
                              </p>
                              <p style="margin:0;font-size:11px;color:#c8c8dc;">
                                &copy; 2026 Huly &mdash; Todos los derechos reservados.
                              </p>
                            </td>
                          </tr>
                </table>
                <!-- /Card -->

              </td>
            </tr>
          </table>

        </body>
        </html>
        """.formatted(nickname);
  }

  @Override
  public void sendReEngagement(String to) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setFrom("hulycomunicaciones@gmail.com", "Huly");
      helper.setSubject("¡Te extrañamos en Huly!");
      helper.setText(EMAIL_BODY, true);
      helper.addInline("logo", new ClassPathResource("email/logo.png"));
      mailSender.send(message);
      log.info("[MAIL] Email de re-engagement enviado → to={}", to);
    } catch (Exception e) {
      log.error("[MAIL] Error enviando re-engagement a {} — {}", to, e.getMessage());
    }
  }

  private static final String EMAIL_BODY = """
          <!DOCTYPE html>
          <html lang="es">
          <head>
            <meta charset="utf-8"/>
            <style>
              @import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;800&display=swap');
            </style>
          </head>
          <body style="margin:0;padding:0;background-color:#E9F1EA;
                       font-family:'Nunito',Roboto,-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;">
            <table width="100%" cellpadding="0" cellspacing="0" border="0"
                  style="background-color:#E9F1EA;padding:40px 16px;">
              <tr><td align="center">
                <table width="600" cellpadding="0" cellspacing="0" border="0"
                      style="max-width:600px;background-color:#ffffff;border-radius:20px;
                              box-shadow:0 4px 24px rgba(136,105,172,0.12);">
                  <tr>
                    <td align="center" style="background-color:#8869AC;padding:34px 40px;">
                      <img src="cid:logo" alt="Huly" width="150" style="display:block;height:auto;"/>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:38px 48px 28px;">
                      <h1 style="color:#2D2D2D;font-size:26px;font-weight:800;margin:0 0 16px;">
                        Hace unos días que no pasás...
                      </h1>
                      <p style="color:#6B6B6B;font-size:16px;line-height:1.75;font-weight:400;margin:0 0 28px;">
                        Cuando necesites una pausa, acá estamos.
                        Sin apuro, sin nada pendiente.
                        El jardín te espera.
                      </p>
                      <table cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td style="background-color:#8869AC;border-radius:10px;">
                            <a href="https://huly-tpi-frontend.onrender.com"
                              style="display:inline-block;padding:14px 36px;color:#ffffff;
                                      text-decoration:none;font-size:15px;font-weight:600;">
                              Volver al jardín
                            </a>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:20px 48px 36px;border-top:1px solid #E9F1EA;">
                      <p style="font-size:13px;color:#6B6B6B;margin:0;">Con cariño, el equipo de Huly 🌿</p>
                    </td>
                  </tr>
                </table>
              </td></tr>
            </table>
          </body>
          </html>
      """;
}
