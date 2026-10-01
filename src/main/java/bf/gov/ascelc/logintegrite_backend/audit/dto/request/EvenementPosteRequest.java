package bf.gov.ascelc.logintegrite_backend.audit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EvenementPosteRequest(
        @NotBlank @Size(max = 40) String type,
        @Size(max = 255) String page,
        @Size(max = 500) String detail) { }
