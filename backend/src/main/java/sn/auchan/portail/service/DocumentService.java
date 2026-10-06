package sn.auchan.portail.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sn.auchan.portail.domain.BusinessApp;
import sn.auchan.portail.dto.GeneratedDocument;

@Service
public class DocumentService {

    public GeneratedDocument generate(BusinessApp app, String kind, String format) {
        DocumentKind documentKind = DocumentKind.from(kind);
        DocumentFormat documentFormat = DocumentFormat.from(format);
        String html = renderHtml(app, documentKind);
        String basename = slug(app.getName()) + "-" + documentKind.slug();
        return switch (documentFormat) {
            case HTML -> new GeneratedDocument(
                    html.getBytes(StandardCharsets.UTF_8),
                    "text/html; charset=UTF-8",
                    basename + ".html"
            );
            case PDF -> new GeneratedDocument(
                    toPdf(html),
                    "application/pdf",
                    basename + ".pdf"
            );
        };
    }

    private byte[] toPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de générer le PDF");
        }
    }

    private String renderHtml(BusinessApp app, DocumentKind kind) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
        String shell = """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="fr">
                <head>
                  <meta charset="UTF-8" />
                  <title>%s — %s</title>
                  <style>
                    @page { margin: 28px 32px; }
                    body { font-family: 'Segoe UI', Arial, sans-serif; color: #1f2937; margin: 0; }
                    .brand { color: #C81E1E; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; font-size: 12px; }
                    h1 { margin: 8px 0 6px; font-size: 26px; }
                    .lead { color: #4b5563; margin: 0 0 24px; line-height: 1.5; }
                    .meta { width: 100%%; border-collapse: collapse; margin-bottom: 24px; }
                    .meta th, .meta td { border: 1px solid #e5e7eb; padding: 10px 12px; text-align: left; vertical-align: top; }
                    .meta th { width: 32%%; background: #f9f4f0; color: #6b7280; font-size: 12px; text-transform: uppercase; }
                    h2 { font-size: 16px; margin: 22px 0 8px; color: #C81E1E; }
                    p, li { line-height: 1.55; }
                    ul { padding-left: 18px; }
                    .footer { margin-top: 32px; padding-top: 12px; border-top: 2px solid #C81E1E; font-size: 12px; color: #6b7280; }
                  </style>
                </head>
                <body>
                  <p class="brand">Auchan Sénégal · Portail interne</p>
                  <h1>%s</h1>
                  <p class="lead">%s</p>
                  <table class="meta">
                    <tr><th>Application</th><td>%s</td></tr>
                    <tr><th>Catégorie</th><td>%s</td></tr>
                    <tr><th>Direction</th><td>%s</td></tr>
                    <tr><th>Version</th><td>%s</td></tr>
                    <tr><th>Statut</th><td>%s</td></tr>
                    <tr><th>Public concerné</th><td>%s</td></tr>
                    <tr><th>URL</th><td>%s</td></tr>
                    <tr><th>Support</th><td>%s</td></tr>
                  </table>
                  __DOCUMENT_BODY__
                  <p class="footer">Document généré le %s par le portail Auchan Sénégal. Usage interne uniquement.</p>
                </body>
                </html>
                """.formatted(
                fmt(kind.title()),
                fmt(app.getName()),
                fmt(kind.title() + " — " + app.getName()),
                fmt(app.getDescription()),
                fmt(app.getName()),
                fmt(app.getCategory() != null ? app.getCategory().getName() : "—"),
                fmt(app.getOwnerDepartment()),
                fmt(blank(app.getVersion())),
                fmt(blank(app.getStatus())),
                fmt(blank(app.getAudience())),
                fmt(blank(app.getUrl())),
                fmt(blank(app.getSupportContact())),
                fmt(today)
        );
        return shell.replace("__DOCUMENT_BODY__", kind.body(app));
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String fmt(String value) {
        return escape(value).replace("%", "%%");
    }

    private static String slug(String value) {
        String slug = DepartmentService.slugify(value);
        return slug.isBlank() ? "document" : slug;
    }

    private enum DocumentFormat {
        HTML, PDF;

        static DocumentFormat from(String value) {
            if (value == null || value.isBlank() || value.equalsIgnoreCase("html")) {
                return HTML;
            }
            if (value.equalsIgnoreCase("pdf")) {
                return PDF;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format non pris en charge. Utilisez html ou pdf.");
        }
    }

    private enum DocumentKind {
        DOCUMENTATION("documentation", "Mode opératoire (MODOP)"),
        FICHE_TECHNIQUE("fiche-technique", "Fiche technique"),
        GUIDE("guide", "Guide utilisateur");

        private final String slug;
        private final String title;

        DocumentKind(String slug, String title) {
            this.slug = slug;
            this.title = title;
        }

        String slug() {
            return slug;
        }

        String title() {
            return title;
        }

        String body(BusinessApp app) {
            return switch (this) {
                case DOCUMENTATION -> customOrDefault(
                        app.getModop(),
                        """
                        <h2>Présentation</h2>
                        <p>%s</p>
                        <h2>Mode opératoire</h2>
                        <ul>
                          <li>Ouvrir l’application depuis le portail interne Auchan.</li>
                          <li>Se connecter avec le compte Bird du collaborateur.</li>
                          <li>Réaliser l’action métier prévue, puis quitter proprement la session.</li>
                        </ul>
                        <h2>Incidents</h2>
                        <ul>
                          <li>Ne jamais partager vos identifiants.</li>
                          <li>Signaler tout incident au Service Desk DSI (%s).</li>
                        </ul>
                        """.formatted(
                                fmt(blank(app.getLongDescription() != null ? app.getLongDescription() : app.getDescription())),
                                fmt(blank(app.getSupportContact()))
                        )
                );
                case FICHE_TECHNIQUE -> customOrDefault(
                        app.getTechnicalSheetContent(),
                        """
                        <h2>Fiche d'identité</h2>
                        <p>%s</p>
                        <h2>Prérequis</h2>
                        <ul>
                          <li>Compte collaborateur actif dans Bird.</li>
                          <li>Droit d'accès attribué par le responsable ou la DSI.</li>
                          <li>Navigateur à jour et connexion au réseau Auchan ou VPN.</li>
                        </ul>
                        <h2>Contacts techniques</h2>
                        <ul>
                          <li>Métier / direction : %s</li>
                          <li>Support : %s</li>
                        </ul>
                        """.formatted(
                                fmt(blank(app.getLongDescription() != null ? app.getLongDescription() : app.getDescription())),
                                fmt(blank(app.getOwnerDepartment())),
                                fmt(blank(app.getSupportContact()))
                        )
                );
                case GUIDE -> customOrDefault(
                        app.getUserGuideContent(),
                        """
                        <h2>Parcours utilisateur</h2>
                        <p>%s</p>
                        <h2>Étapes recommandées</h2>
                        <ul>
                          <li>Ouvrir l'application depuis le portail interne.</li>
                          <li>Vérifier votre profil et vos habilitations.</li>
                          <li>Réaliser l'action métier prévue, puis quitter proprement la session.</li>
                        </ul>
                        <h2>En cas de besoin</h2>
                        <p>Contactez %s ou ouvrez un ticket auprès du Service Desk DSI.</p>
                        """.formatted(
                                fmt(blank(app.getLongDescription() != null ? app.getLongDescription() : app.getDescription())),
                                fmt(blank(app.getSupportContact()))
                        )
                );
            };
        }

        private String customOrDefault(String custom, String fallback) {
            if (custom == null || custom.isBlank()) {
                return fallback;
            }
            return toHtml(custom);
        }

        private static String toHtml(String text) {
            StringBuilder html = new StringBuilder();
            boolean inList = false;
            for (String raw : text.replace("\r\n", "\n").split("\n")) {
                String line = raw.trim();
                if (line.isEmpty()) {
                    if (inList) {
                        html.append("</ul>");
                        inList = false;
                    }
                    continue;
                }
                if (line.matches("^\\d+\\.\\s+.+$")) {
                    if (inList) {
                        html.append("</ul>");
                        inList = false;
                    }
                    html.append("<h2>").append(escape(line)).append("</h2>");
                } else if (line.startsWith("- ")) {
                    if (!inList) {
                        html.append("<ul>");
                        inList = true;
                    }
                    html.append("<li>").append(escape(line.substring(2))).append("</li>");
                } else {
                    if (inList) {
                        html.append("</ul>");
                        inList = false;
                    }
                    html.append("<p>").append(escape(line)).append("</p>");
                }
            }
            if (inList) {
                html.append("</ul>");
            }
            return html.toString();
        }

        static DocumentKind from(String value) {
            if (value == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de document manquant");
            }
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "documentation", "doc", "modop", "mode-operatoire" -> DOCUMENTATION;
                case "fiche-technique", "fiche", "technical" -> FICHE_TECHNIQUE;
                case "guide", "guide-utilisateur", "user-guide" -> GUIDE;
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Type de document inconnu. Utilisez documentation, fiche-technique ou guide."
                );
            };
        }
    }
}
