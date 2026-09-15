package sn.auchan.portail.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_access", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "application_id"}))
public class AppAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private BusinessApp application;

    private boolean canViewDocumentation = true;

    private boolean canViewTechnicalSheet = false;

    private boolean canViewUserGuide = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public BusinessApp getApplication() {
        return application;
    }

    public void setApplication(BusinessApp application) {
        this.application = application;
    }

    public boolean isCanViewDocumentation() {
        return canViewDocumentation;
    }

    public void setCanViewDocumentation(boolean canViewDocumentation) {
        this.canViewDocumentation = canViewDocumentation;
    }

    public boolean isCanViewTechnicalSheet() {
        return canViewTechnicalSheet;
    }

    public void setCanViewTechnicalSheet(boolean canViewTechnicalSheet) {
        this.canViewTechnicalSheet = canViewTechnicalSheet;
    }

    public boolean isCanViewUserGuide() {
        return canViewUserGuide;
    }

    public void setCanViewUserGuide(boolean canViewUserGuide) {
        this.canViewUserGuide = canViewUserGuide;
    }
}
