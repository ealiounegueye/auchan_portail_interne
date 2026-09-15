package sn.auchan.portail;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void internSeesOnlyGrantedApplications() throws Exception {
        String token = login("stagiaire@auchan.sn");
        String json = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode apps = mapper.readTree(json);
        org.assertj.core.api.Assertions.assertThat(apps).isNotEmpty();
        for (JsonNode app : apps) {
            org.assertj.core.api.Assertions.assertThat(app.get("name").asText())
                    .isNotEqualTo("Comptabilité")
                    .isNotEqualTo("Paie & Bulletins")
                    .isNotEqualTo("SIRH Auchan");
        }
        org.assertj.core.api.Assertions.assertThat(
                java.util.stream.StreamSupport.stream(apps.spliterator(), false)
                        .map(app -> app.get("name").asText())
                        .toList()
        ).contains("Caisse / POS", "Formation interne");
    }

    @Test
    void internCannotOpenRestrictedApplication() throws Exception {
        String adminToken = login("admin@auchan.sn");
        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long financeId = java.util.stream.StreamSupport.stream(mapper.readTree(catalog).spliterator(), false)
                .filter(app -> "Comptabilité".equals(app.get("name").asText()))
                .map(app -> app.get("id").asLong())
                .findFirst()
                .orElseThrow();

        String internToken = login("stagiaire@auchan.sn");
        mvc.perform(get("/api/applications/" + financeId).header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void internCannotDownloadRestrictedDocument() throws Exception {
        String adminToken = login("admin@auchan.sn");
        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long financeId = java.util.stream.StreamSupport.stream(mapper.readTree(catalog).spliterator(), false)
                .filter(app -> "Comptabilité".equals(app.get("name").asText()))
                .map(app -> app.get("id").asLong())
                .findFirst()
                .orElseThrow();

        String internToken = login("stagiaire@auchan.sn");
        mvc.perform(get("/api/applications/" + financeId + "/documents/documentation")
                        .param("format", "html")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void internCannotDownloadTechnicalSheetWhenNotGranted() throws Exception {
        String internToken = login("stagiaire@auchan.sn");
        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + internToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long caisseId = java.util.stream.StreamSupport.stream(mapper.readTree(catalog).spliterator(), false)
                .filter(app -> "Caisse / POS".equals(app.get("name").asText()))
                .map(app -> app.get("id").asLong())
                .findFirst()
                .orElseThrow();

        JsonNode caisse = mapper.readTree(
                mvc.perform(get("/api/applications/" + caisseId).header("Authorization", "Bearer " + internToken))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );
        org.assertj.core.api.Assertions.assertThat(caisse.get("canViewDocumentation").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(caisse.get("canViewTechnicalSheet").asBoolean()).isFalse();

        mvc.perform(get("/api/applications/" + caisseId + "/documents/documentation")
                        .param("format", "html")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/applications/" + caisseId + "/documents/fiche-technique")
                        .param("format", "html")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanGenerateApplicationDocuments() throws Exception {
        String token = login("admin@auchan.sn");
        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long appId = mapper.readTree(catalog).get(0).get("id").asLong();

        mvc.perform(get("/api/applications/" + appId + "/documents/fiche-technique")
                        .param("format", "html")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/applications/" + appId + "/documents/documentation")
                        .param("format", "pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String html = mvc.perform(get("/api/applications/" + appId + "/documents/modop")
                        .param("format", "html")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(html).contains("Mode opératoire (MODOP)");
    }

    @Test
    void internCanDownloadUploadedModopButNotTechnicalSheetFile() throws Exception {
        String adminToken = login("admin@auchan.sn");
        String uploadedJson = mvc.perform(multipart("/api/admin/uploads/documents")
                        .file(new MockMultipartFile(
                                "file",
                                "MODOP-Caisse.pdf",
                                "application/pdf",
                                "%PDF-1.4 test".getBytes()
                        ))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode uploaded = mapper.readTree(uploadedJson);

        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode caisse = java.util.stream.StreamSupport.stream(mapper.readTree(catalog).spliterator(), false)
                .filter(app -> "Caisse / POS".equals(app.get("name").asText()))
                .findFirst()
                .orElseThrow();
        long caisseId = caisse.get("id").asLong();

        ObjectNode payload = mapper.createObjectNode();
        payload.put("name", caisse.get("name").asText());
        payload.put("description", caisse.get("description").asText());
        payload.put("url", caisse.get("url").asText());
        payload.put("icon", caisse.get("icon").asText());
        payload.put("categoryId", caisse.get("category").get("id").asLong());
        payload.put("ownerDepartment", caisse.get("ownerDepartment").asText());
        payload.put("status", caisse.get("status").asText());
        payload.put("featured", caisse.get("featured").asBoolean());
        payload.put("modop", caisse.path("modop").asText(""));
        payload.put("documentationStoredFile", uploaded.get("storedFile").asText());
        payload.put("documentationFileName", uploaded.get("originalName").asText());
        payload.put("documentationContentType", uploaded.get("contentType").asText());

        mvc.perform(put("/api/admin/applications/" + caisseId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        String internToken = login("stagiaire@auchan.sn");
        mvc.perform(get("/api/applications/" + caisseId + "/documents/documentation/file")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/applications/" + caisseId + "/documents/fiche-technique/file")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void internCannotDownloadUploadedFileOfRestrictedApp() throws Exception {
        String adminToken = login("admin@auchan.sn");
        String catalog = mvc.perform(get("/api/applications").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long financeId = java.util.stream.StreamSupport.stream(mapper.readTree(catalog).spliterator(), false)
                .filter(app -> "Comptabilité".equals(app.get("name").asText()))
                .map(app -> app.get("id").asLong())
                .findFirst()
                .orElseThrow();

        String internToken = login("stagiaire@auchan.sn");
        mvc.perform(get("/api/applications/" + financeId + "/documents/documentation/file")
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotCreateUsersBecauseBirdOwnsIdentity() throws Exception {
        String token = login("responsable@auchan.sn");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String body = """
                {
                  "firstName": "Nouveau",
                  "lastName": "Stagiaire",
                  "email": "nouveau.stagiaire@auchan.sn",
                  "password": "Auchan@2026",
                  "role": "USER",
                  "department": "Magasin Dakar Plateau",
                  "active": true,
                  "restrictedAccess": true,
                  "allowedApplicationIds": []
                }
                """;
        mvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private String login(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Auchan@2026\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readTree(json).get("token").asText();
    }
}
