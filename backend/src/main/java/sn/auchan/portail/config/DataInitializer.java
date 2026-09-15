package sn.auchan.portail.config;

import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.auchan.portail.domain.AppAccess;
import sn.auchan.portail.domain.BusinessApp;
import sn.auchan.portail.domain.Category;
import sn.auchan.portail.domain.Department;
import sn.auchan.portail.domain.Role;
import sn.auchan.portail.domain.UserAccount;
import sn.auchan.portail.repository.AppAccessRepository;
import sn.auchan.portail.repository.BusinessAppRepository;
import sn.auchan.portail.repository.CategoryRepository;
import sn.auchan.portail.repository.DepartmentRepository;
import sn.auchan.portail.repository.UserAccountRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository users;
    private final CategoryRepository categories;
    private final DepartmentRepository departments;
    private final BusinessAppRepository applications;
    private final AppAccessRepository accesses;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserAccountRepository users,
            CategoryRepository categories,
            DepartmentRepository departments,
            BusinessAppRepository applications,
            AppAccessRepository accesses,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.categories = categories;
        this.departments = departments;
        this.applications = applications;
        this.accesses = accesses;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }

        departments.saveAll(List.of(
                department("DSI", "DSI", "Direction des systèmes d'information", 1),
                department("RH", "RH", "Ressources humaines et social", 2),
                department("Finance", "FIN", "Direction financière et contrôle de gestion", 3),
                department("Supply Chain", "SUP", "Logistique, entrepôt et approvisionnements", 4),
                department("Achats", "ACH", "Achats et relations fournisseurs", 5),
                department("Exploitation", "EXP", "Pilotage des magasins et de la caisse", 6),
                department("E-commerce", "ECO", "Drive, site marchand et commandes en ligne", 7),
                department("Marketing", "MKT", "Offre commerciale, fidélité et campagnes", 8),
                department("Communication", "COM", "Communication interne et intranet", 9),
                department("Magasin Dakar Plateau", "DKR-PLT", "Site magasin Dakar Plateau", 10)
        ));

        UserAccount manager = users.save(user("Ibrahima", "Fall", "responsable@auchan.sn", "Auchan@2026", Role.MANAGER, "Magasin Dakar Plateau"));
        UserAccount intern = user("Awa", "Ba", "stagiaire@auchan.sn", "Auchan@2026", Role.USER, "Magasin Dakar Plateau");
        intern.setManager(manager);
        intern.setRestrictedAccess(true);
        users.saveAll(List.of(
                user("Aminata", "Diallo", "admin@auchan.sn", "Auchan@2026", Role.ADMIN, "DSI"),
                user("Moussa", "Ndiaye", "collaborateur@auchan.sn", "Auchan@2026", Role.USER, "Magasin Dakar Plateau"),
                user("Fatou", "Sarr", "rh@auchan.sn", "Auchan@2026", Role.USER, "RH"),
                intern
        ));

        Category rh = category("RH & Social", "rh", "users", "#C81E1E", 1);
        Category finance = category("Finance", "finance", "wallet", "#B45309", 2);
        Category supply = category("Supply & Logistique", "supply", "truck", "#1D4ED8", 3);
        Category magasin = category("Magasin & Caisse", "magasin", "store", "#047857", 4);
        Category commercial = category("Commercial & Marketing", "commercial", "megaphone", "#7C3AED", 5);
        Category it = category("IT & Outils", "it", "monitor", "#334155", 6);
        categories.saveAll(List.of(rh, finance, supply, magasin, commercial, it));

        applications.saveAll(List.of(
                app("SIRH Auchan", "Gestion des collaborateurs, congés, contrats et dossiers RH.", "https://sirh.auchan.sn", "badge", rh, "RH", true, 1),
                app("Paie & Bulletins", "Consultation des bulletins de paie et variables de rémunération.", "https://paie.auchan.sn", "receipt", rh, "RH", true, 2),
                app("Formation interne", "Catalogue e-learning, parcours métier et attestations.", "https://formation.auchan.sn", "graduation", rh, "RH", false, 3),
                app("Comptabilité", "Écritures, clôtures et reporting financier des magasins.", "https://finance.auchan.sn", "calculator", finance, "Finance", true, 4),
                app("Contrôle de gestion", "Budgets, écarts et tableaux de bord de performance.", "https://cdg.auchan.sn", "chart", finance, "Finance", false, 5),
                app("Notes de frais", "Saisie et validation des notes de frais collaborateurs.", "https://frais.auchan.sn", "card", finance, "Finance", false, 6),
                app("WMS Entrepôt", "Réception, picking, expédition et stocks entrepôt Dakar.", "https://wms.auchan.sn", "warehouse", supply, "Supply Chain", true, 7),
                app("Supply Chain", "Approvisionnements, prévisions et suivi des commandes fournisseurs.", "https://supply.auchan.sn", "boxes", supply, "Supply Chain", true, 8),
                app("Achats", "Appels d'offres, contrats fournisseurs et suivi des livraisons.", "https://achats.auchan.sn", "cart", supply, "Achats", false, 9),
                app("Caisse / POS", "Supervision des caisses, écarts et ouverture / fermeture de journée.", "https://pos.auchan.sn", "cash", magasin, "Exploitation", true, 10),
                app("Planning magasins", "Plannings équipes, horaires et remplacements en magasin.", "https://planning.auchan.sn", "calendar", magasin, "Exploitation", true, 11),
                app("Stocks magasin", "Inventaires, ruptures et transferts inter-magasins.", "https://stocks.auchan.sn", "package", magasin, "Exploitation", false, 12),
                app("Drive & Click Collect", "Pilotage des commandes Drive et Click & Collect.", "https://drive.auchan.sn", "car", magasin, "E-commerce", false, 13),
                app("Fidélité clients", "Programme de fidélité, cartes et campagnes promotionnelles.", "https://fidelite.auchan.sn", "heart", commercial, "Marketing", false, 14),
                app("E-commerce", "Back-office du site et des commandes en ligne Auchan Sénégal.", "https://shop.auchan.sn/admin", "globe", commercial, "E-commerce", true, 15),
                app("Business Intelligence", "Tableaux de bord ventes, marge et fréquentation magasins.", "https://bi.auchan.sn", "pie", commercial, "DSI", true, 16),
                app("Service Desk IT", "Incidents, demandes et suivi des tickets informatiques.", "https://helpdesk.auchan.sn", "headset", it, "DSI", true, 17),
                app("Messagerie", "Accès web à la messagerie professionnelle.", "https://mail.auchan.sn", "mail", it, "DSI", false, 18),
                app("Intranet", "Actualités internes, procédures et documents officiels.", "https://intranet.auchan.sn", "building", it, "Communication", false, 19),
                app("Annuaire interne", "Recherche des collaborateurs, services et sites Auchan Sénégal.", "https://annuaire.auchan.sn", "book", it, "DSI", false, 20),
                app("Sage X3", "ERP Sage X3 : finance, stocks, achats, production et reporting.", "https://x3.auchan.sn", "calculator", finance, "Finance", true, 21)
        ));

        Set<String> internApps = Set.of(
                "Formation interne",
                "Caisse / POS",
                "Planning magasins",
                "Messagerie",
                "Intranet",
                "Annuaire interne"
        );
        UserAccount savedIntern = intern;
        applications.findAll().stream()
                .filter(application -> internApps.contains(application.getName()))
                .forEach(application -> {
                    AppAccess access = new AppAccess();
                    access.setUser(savedIntern);
                    access.setApplication(application);
                    access.setCanViewDocumentation(true);
                    access.setCanViewTechnicalSheet(false);
                    access.setCanViewUserGuide(true);
                    accesses.save(access);
                });
    }

    private UserAccount user(String first, String last, String email, String password, Role role, String department) {
        UserAccount user = new UserAccount();
        user.setFirstName(first);
        user.setLastName(last);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);
        return user;
    }

    private Department department(String name, String code, String description, int order) {
        Department department = new Department();
        department.setName(name);
        department.setCode(code);
        department.setDescription(description);
        department.setSortOrder(order);
        return department;
    }

    private Category category(String name, String slug, String icon, String color, int order) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setIcon(icon);
        category.setColor(color);
        category.setSortOrder(order);
        return category;
    }

    private BusinessApp app(
            String name,
            String description,
            String url,
            String icon,
            Category category,
            String department,
            boolean featured,
            int order
    ) {
        BusinessApp app = new BusinessApp();
        app.setName(name);
        app.setDescription(description);
        app.setUrl(url);
        app.setIcon(icon);
        app.setCategory(category);
        app.setOwnerDepartment(department);
        app.setStatus("ACTIVE");
        app.setFeatured(featured);
        app.setSortOrder(order);
        app.setLongDescription(description
                + " Cette application est mise à disposition des équipes " + department
                + " d’Auchan Sénégal. Consultez la documentation, la fiche technique et le guide utilisateur avant toute demande d’évolution ou d’accès complémentaire.");
        app.setSupportUrl("https://helpdesk.auchan.sn");
        app.setSupportContact("Service Desk DSI — helpdesk@auchan.sn");
        app.setVersion("2026.1");
        app.setAudience("Collaborateurs Auchan Sénégal — " + department);
        app.setModop(modopFor(name, department));
        app.setTechnicalSheetContent("""
                Application : %s
                Direction : %s
                Version : 2026.1
                URL : %s

                Prérequis : navigateur à jour, compte Bird, droit d’accès portail.
                Support : Service Desk DSI — helpdesk@auchan.sn
                """.formatted(name, department, url));
        app.setUserGuideContent("""
                Pour utiliser %s :
                - Connectez-vous au portail.
                - Ouvrez la fiche application puis le bouton Ouvrir.
                - Suivez le parcours métier de votre service.
                - En cas de doute, consultez votre responsable ou le Service Desk.
                """.formatted(name));
        return app;
    }

    private String modopFor(String name, String department) {
        if ("Caisse / POS".equals(name)) {
            return """
                    1. Objet
                    Ce mode opératoire décrit l’ouverture, le suivi et la clôture de journée caisse Auchan Sénégal (POS).

                    2. Conditions préalables
                    - Disposer d’un compte Bird actif et d’un accès Caisse / POS attribué par le responsable magasin.
                    - Être habilité caissier, chef de caisse ou responsable d’exploitation.
                    - Être connecté au réseau magasin. Ne jamais utiliser un poste caisse hors caisse.

                    3. Ouverture de journée
                    - Ouvrir le portail interne, rechercher « Caisse / POS » puis cliquer sur Ouvrir.
                    - Contrôler le fond de caisse théorique et le comparer au fond physique.
                    - Saisir le montant d’ouverture et valider l’ouverture de journée.
                    - En cas d’écart, le consigner et alerter le chef de caisse avant le premier encaissement.

                    4. Encaissement
                    - Identifier le mode de paiement (espèces, carte, fidélité, avoir).
                    - Vérifier le ticket avant validation définitive.
                    - Ne jamais partager la session POS. En pause, verrouiller le poste.

                    5. Clôture de journée
                    - Arrêter les encaissements, compter le fond et les recettes.
                    - Saisir les montants de clôture et joindre les éventuels écarts.
                    - Imprimer ou archiver le Z de caisse, puis quitter la session.

                    6. Incidents
                    - Ticket bloqué, écart de caisse ou panne : ouvrir un ticket Service Desk DSI (helpdesk@auchan.sn) en précisant le magasin, le n° de caisse, l’heure et le message d’erreur.
                    """;
        }
        return """
                1. Objet
                Ce mode opératoire décrit l’utilisation de %s par les équipes %s d’Auchan Sénégal.

                2. Conditions préalables
                - Disposer d’un compte Bird actif.
                - Avoir l’accès à l’application attribué par le responsable ou la DSI.
                - Être connecté au réseau Auchan ou au VPN.

                3. Déroulement
                - Ouvrir le portail interne Auchan Sénégal.
                - Rechercher « %s » puis cliquer sur Ouvrir.
                - S’authentifier si demandé.
                - Réaliser l’opération métier prévue, puis quitter la session.

                4. En cas d’incident
                Contacter le Service Desk DSI (helpdesk@auchan.sn) en précisant l’application, l’heure et le message d’erreur.
                """.formatted(name, department, name);
    }
}
