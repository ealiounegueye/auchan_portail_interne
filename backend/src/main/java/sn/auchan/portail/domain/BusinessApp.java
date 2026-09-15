package sn.auchan.portail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class BusinessApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 600)
    private String description;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String icon;

    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String ownerDepartment;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(length = 2500)
    private String longDescription;

    @Column(length = 8000)
    private String modop;

    @Column(length = 8000)
    private String technicalSheetContent;

    @Column(length = 8000)
    private String userGuideContent;

    private String documentationUrl;

    private String documentationStoredFile;

    private String documentationFileName;

    private String documentationContentType;

    private String technicalSheetUrl;

    private String technicalSheetStoredFile;

    private String technicalSheetFileName;

    private String technicalSheetContentType;

    private String userGuideUrl;

    private String userGuideStoredFile;

    private String userGuideFileName;

    private String userGuideContentType;

    private String supportUrl;

    private String supportContact;

    private String version;

    private String audience;

    private boolean featured;

    private int sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getOwnerDepartment() {
        return ownerDepartment;
    }

    public void setOwnerDepartment(String ownerDepartment) {
        this.ownerDepartment = ownerDepartment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getModop() {
        return modop;
    }

    public void setModop(String modop) {
        this.modop = modop;
    }

    public String getTechnicalSheetContent() {
        return technicalSheetContent;
    }

    public void setTechnicalSheetContent(String technicalSheetContent) {
        this.technicalSheetContent = technicalSheetContent;
    }

    public String getUserGuideContent() {
        return userGuideContent;
    }

    public void setUserGuideContent(String userGuideContent) {
        this.userGuideContent = userGuideContent;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getDocumentationStoredFile() {
        return documentationStoredFile;
    }

    public void setDocumentationStoredFile(String documentationStoredFile) {
        this.documentationStoredFile = documentationStoredFile;
    }

    public String getDocumentationFileName() {
        return documentationFileName;
    }

    public void setDocumentationFileName(String documentationFileName) {
        this.documentationFileName = documentationFileName;
    }

    public String getDocumentationContentType() {
        return documentationContentType;
    }

    public void setDocumentationContentType(String documentationContentType) {
        this.documentationContentType = documentationContentType;
    }

    public String getTechnicalSheetUrl() {
        return technicalSheetUrl;
    }

    public void setTechnicalSheetUrl(String technicalSheetUrl) {
        this.technicalSheetUrl = technicalSheetUrl;
    }

    public String getTechnicalSheetStoredFile() {
        return technicalSheetStoredFile;
    }

    public void setTechnicalSheetStoredFile(String technicalSheetStoredFile) {
        this.technicalSheetStoredFile = technicalSheetStoredFile;
    }

    public String getTechnicalSheetFileName() {
        return technicalSheetFileName;
    }

    public void setTechnicalSheetFileName(String technicalSheetFileName) {
        this.technicalSheetFileName = technicalSheetFileName;
    }

    public String getTechnicalSheetContentType() {
        return technicalSheetContentType;
    }

    public void setTechnicalSheetContentType(String technicalSheetContentType) {
        this.technicalSheetContentType = technicalSheetContentType;
    }

    public String getUserGuideUrl() {
        return userGuideUrl;
    }

    public void setUserGuideUrl(String userGuideUrl) {
        this.userGuideUrl = userGuideUrl;
    }

    public String getUserGuideStoredFile() {
        return userGuideStoredFile;
    }

    public void setUserGuideStoredFile(String userGuideStoredFile) {
        this.userGuideStoredFile = userGuideStoredFile;
    }

    public String getUserGuideFileName() {
        return userGuideFileName;
    }

    public void setUserGuideFileName(String userGuideFileName) {
        this.userGuideFileName = userGuideFileName;
    }

    public String getUserGuideContentType() {
        return userGuideContentType;
    }

    public void setUserGuideContentType(String userGuideContentType) {
        this.userGuideContentType = userGuideContentType;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getSupportContact() {
        return supportContact;
    }

    public void setSupportContact(String supportContact) {
        this.supportContact = supportContact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
