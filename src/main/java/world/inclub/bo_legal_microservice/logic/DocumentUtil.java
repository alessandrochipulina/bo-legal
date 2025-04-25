package world.inclub.bo_legal_microservice.logic;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import world.inclub.bo_legal_microservice.model.Document;
import world.inclub.bo_legal_microservice.model.DocumentHistory;

@Component
public class DocumentUtil {

    public DocumentHistory saveSystemHistory(
        String key, 
        String reason, 
        Integer status, 
        Integer user) {
        DocumentHistory dh = new DocumentHistory();
        dh.setDocumentKey(key);
        dh.setStatus(status);
        dh.setReasonText(reason);
        dh.setReasonType(0);
        dh.setCreatedAt(LocalDateTime.now());
        dh.setUserPanelId(user);
        return dh;
    }

    public Document newDocument(Document doc) {
        // Crear nuevo documento en estado pendiente        
        return doc.toBuilder()
        .status(1) // nuevo estado        
        .createdAt(LocalDateTime.now()) // fecha de modificación actual
        .modifiedAt(LocalDateTime.now()) // fecha de modificación actual
        .id(null)
        .build();
    }
}
