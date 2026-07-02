package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationExistenceResponse {
    private boolean existeDejaDansRegistre;
    private UUID ficheExistanteId;
    private String message;
}